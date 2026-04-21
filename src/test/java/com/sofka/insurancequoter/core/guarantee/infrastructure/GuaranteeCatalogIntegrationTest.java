package com.sofka.insurancequoter.core.guarantee.infrastructure;

import com.sofka.insurancequoter.core.guarantee.infrastructure.adapter.in.rest.dto.GuaranteesResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class GuaranteeCatalogIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configurePostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    }

    @LocalServerPort
    private int port;

    private RestClient restClient;

    @BeforeEach
    void setUp() {
        restClient = RestClient.create("http://localhost:" + port);
    }

    @Test
    void getGuarantees_returnsHttp200WithCatalog() {
        // WHEN
        ResponseEntity<GuaranteesResponse> response = restClient.get()
                .uri("/v1/catalogs/guarantees")
                .retrieve()
                .toEntity(GuaranteesResponse.class);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().guarantees()).isNotNull();
        assertThat(response.getBody().guarantees()).isNotEmpty();
    }

    @Test
    void getGuarantees_eachElementHasRequiredFields() {
        // WHEN
        ResponseEntity<GuaranteesResponse> response = restClient.get()
                .uri("/v1/catalogs/guarantees")
                .retrieve()
                .toEntity(GuaranteesResponse.class);

        // THEN
        assertThat(response.getBody()).isNotNull();
        response.getBody().guarantees().forEach(g -> {
            assertThat(g.code()).isNotBlank();
            assertThat(g.description()).isNotBlank();
        });
    }

    @Test
    void getGuarantees_returnsThreeGuaranteesFromFixture() {
        // WHEN
        ResponseEntity<GuaranteesResponse> response = restClient.get()
                .uri("/v1/catalogs/guarantees")
                .retrieve()
                .toEntity(GuaranteesResponse.class);

        // THEN
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().guarantees()).hasSize(3);
        assertThat(response.getBody().guarantees().get(0).code()).isEqualTo("GUA-FIRE");
        assertThat(response.getBody().guarantees().get(1).code()).isEqualTo("GUA-THEFT");
        assertThat(response.getBody().guarantees().get(2).code()).isEqualTo("GUA-GLASS");
    }

    @Test
    void getGuarantees_allFixtureGuaranteesAreTarifable() {
        // WHEN
        ResponseEntity<GuaranteesResponse> response = restClient.get()
                .uri("/v1/catalogs/guarantees")
                .retrieve()
                .toEntity(GuaranteesResponse.class);

        // THEN
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().guarantees()).allMatch(g -> g.tarifable());
    }
}
