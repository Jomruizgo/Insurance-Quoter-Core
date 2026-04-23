package com.sofka.insurancequoter.core.businessline.infrastructure;

import com.sofka.insurancequoter.core.businessline.infrastructure.adapter.in.rest.dto.BusinessLinesResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
class BusinessLineCatalogIntegrationTest {

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
    void getBusinessLines_returnsHttp200WithCatalog() {
        // WHEN
        ResponseEntity<BusinessLinesResponse> response = restClient.get()
                .uri("/v1/business-lines")
                .retrieve()
                .toEntity(BusinessLinesResponse.class);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().businessLines()).isNotNull();
        assertThat(response.getBody().businessLines()).isNotEmpty();
    }

    @Test
    void getBusinessLines_eachElementHasRequiredFields() {
        // WHEN
        ResponseEntity<BusinessLinesResponse> response = restClient.get()
                .uri("/v1/business-lines")
                .retrieve()
                .toEntity(BusinessLinesResponse.class);

        // THEN
        assertThat(response.getBody()).isNotNull();
        response.getBody().businessLines().forEach(bl -> {
            assertThat(bl.code()).isNotBlank();
            assertThat(bl.description()).isNotBlank();
            assertThat(bl.fireKey()).isNotBlank();
        });
    }
}
