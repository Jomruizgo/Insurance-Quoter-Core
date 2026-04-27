package com.sofka.insurancequoter.core.shared.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test — T-029.
 * Verifies that GET /actuator/prometheus returns HTTP 200 and exposes JVM metrics.
 * Jaeger and Prometheus containers are NOT started here — they are external infrastructure.
 * The OTLP exporter will fail silently (no Jaeger running) which does not affect the test.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ActuatorPrometheusIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        // Disable OTLP tracing export so missing Jaeger does not break the test
        registry.add("management.otlp.tracing.endpoint", () -> "http://localhost:14317");
    }

    @LocalServerPort
    int port;

    private RestClient client;

    @BeforeEach
    void setUp() {
        client = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    void actuatorPrometheus_returns200() {
        // WHEN
        ResponseEntity<String> response = client.get()
                .uri("/actuator/prometheus")
                .retrieve()
                .toEntity(String.class);

        // THEN
        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void actuatorPrometheus_containsJvmMemoryMetric() {
        // WHEN
        String body = client.get()
                .uri("/actuator/prometheus")
                .retrieve()
                .body(String.class);

        // THEN
        assertThat(body).contains("jvm_memory_used_bytes");
    }

    @Test
    void actuatorPrometheus_containsHikariMetric() {
        // WHEN
        String body = client.get()
                .uri("/actuator/prometheus")
                .retrieve()
                .body(String.class);

        // THEN — HikariCP metrics are exposed when a DataSource is configured
        assertThat(body).contains("hikaricp_connections");
    }
}
