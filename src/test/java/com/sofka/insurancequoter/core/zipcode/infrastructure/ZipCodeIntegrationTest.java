package com.sofka.insurancequoter.core.zipcode.infrastructure;

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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ZipCodeIntegrationTest {

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

    // ---- GET /v1/zip-codes/{zipCode} ----

    @Test
    void getZipCode_whenExists_returns200WithFullData() {
        @SuppressWarnings("unchecked")
        Map<String, Object> body = client.get()
                .uri("/v1/zip-codes/06600")
                .retrieve()
                .body(Map.class);

        assertThat(body).isNotNull();
        assertThat(body.get("zipCode")).isEqualTo("06600");
        assertThat(body.get("state")).isNotNull();
        assertThat(body.get("municipality")).isNotNull();
        assertThat(body.get("city")).isNotNull();
        assertThat(body.get("valid")).isEqualTo(true);

        @SuppressWarnings("unchecked")
        List<String> neighborhoods = (List<String>) body.get("neighborhoods");
        assertThat(neighborhoods).contains("Juárez");
    }

    @Test
    void getZipCode_whenNotFound_returns404() {
        assertThatThrownBy(() ->
                client.get()
                        .uri("/v1/zip-codes/99999")
                        .retrieve()
                        .body(Map.class)
        ).isInstanceOfSatisfying(HttpClientErrorException.class, ex -> {
            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(ex.getResponseBodyAsString()).contains("ZIP_CODE_NOT_FOUND");
        });
    }

    // ---- POST /v1/zip-codes/validate ----

    @Test
    void validate_whenZipCodeExists_returns200WithValidTrue() {
        @SuppressWarnings("unchecked")
        Map<String, Object> body = client.post()
                .uri("/v1/zip-codes/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("zipCode", "06600"))
                .retrieve()
                .body(Map.class);

        assertThat(body).isNotNull();
        assertThat(body.get("valid")).isEqualTo(true);
        assertThat(body.get("zipCode")).isEqualTo("06600");
    }

    @Test
    void validate_whenZipCodeDoesNotExist_returns200WithValidFalse() {
        @SuppressWarnings("unchecked")
        Map<String, Object> body = client.post()
                .uri("/v1/zip-codes/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("zipCode", "99999"))
                .retrieve()
                .body(Map.class);

        assertThat(body).isNotNull();
        assertThat(body.get("valid")).isEqualTo(false);
        assertThat(body.get("zipCode")).isEqualTo("99999");
    }
}
