package com.sofka.insurancequoter.core.tariff.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class TariffIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configurePostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        // Flyway runs the migrations; Hibernate must not attempt DDL
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
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

    // ---- GET /v1/tariffs ----

    @Test
    void getTariffs_returnsSeededValues() {
        @SuppressWarnings("unchecked")
        Map<String, Object> body = client.get()
                .uri("/v1/tariffs")
                .retrieve()
                .body(Map.class);

        assertThat(body).isNotNull();

        @SuppressWarnings("unchecked")
        Map<String, Object> tariffs = (Map<String, Object>) body.get("tariffs");
        assertThat(tariffs).isNotNull();
        assertThat(tariffs.get("fireRate")).isEqualTo(0.0015);
        assertThat(tariffs.get("cattevFactor")).isEqualTo(0.0008);
        assertThat(tariffs.get("catfhmFactor")).isEqualTo(0.0005);
        assertThat(tariffs.get("theftRate")).isEqualTo(0.003);
        assertThat(tariffs.get("electronicEquipmentRate")).isEqualTo(0.002);
    }

    // ---- PUT /v1/tariffs ----

    @Test
    void updateTariffs_persistsNewValues() {
        // Update tariffs
        Map<String, Double> requestBody = Map.of(
                "fireRate", 0.002,
                "cattevFactor", 0.001,
                "catfhmFactor", 0.0007,
                "theftRate", 0.004,
                "electronicEquipmentRate", 0.003
        );

        @SuppressWarnings("unchecked")
        Map<String, Object> putBody = client.put()
                .uri("/v1/tariffs")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        assertThat(putBody).isNotNull();

        @SuppressWarnings("unchecked")
        Map<String, Object> updatedTariffs = (Map<String, Object>) putBody.get("tariffs");
        assertThat(updatedTariffs.get("fireRate")).isEqualTo(0.002);
        assertThat(updatedTariffs.get("cattevFactor")).isEqualTo(0.001);
        assertThat(updatedTariffs.get("catfhmFactor")).isEqualTo(0.0007);
        assertThat(updatedTariffs.get("theftRate")).isEqualTo(0.004);
        assertThat(updatedTariffs.get("electronicEquipmentRate")).isEqualTo(0.003);

        // Verify persistence with a subsequent GET
        @SuppressWarnings("unchecked")
        Map<String, Object> getBody = client.get()
                .uri("/v1/tariffs")
                .retrieve()
                .body(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, Object> persistedTariffs = (Map<String, Object>) getBody.get("tariffs");
        assertThat(persistedTariffs.get("fireRate")).isEqualTo(0.002);
    }

    // ---- Validation: @Positive rejects values <= 0 ----

    @Test
    void updateTariffs_withZeroValue_returns400() {
        Map<String, Double> requestBody = Map.of(
                "fireRate", 0.0,
                "cattevFactor", 0.001,
                "catfhmFactor", 0.0007,
                "theftRate", 0.004,
                "electronicEquipmentRate", 0.003
        );

        assertThatThrownBy(() ->
                client.put()
                        .uri("/v1/tariffs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(requestBody)
                        .retrieve()
                        .body(Map.class)
        ).isInstanceOfSatisfying(HttpClientErrorException.class, ex ->
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
        );
    }

    // ---- 404 when tariff row deleted would be edge case — skip in integration (seed ensures row exists) ----
}
