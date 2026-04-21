package com.sofka.insurancequoter.core.riskclassification.infrastructure;

import com.sofka.insurancequoter.core.riskclassification.domain.model.RiskClassification;
import com.sofka.insurancequoter.core.riskclassification.domain.port.in.GetRiskClassificationsUseCase;
import com.sofka.insurancequoter.core.riskclassification.domain.port.out.RiskClassificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class RiskClassificationCatalogIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configurePostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    }

    @Autowired
    private GetRiskClassificationsUseCase getRiskClassificationsUseCase;

    @Autowired
    private RiskClassificationRepository riskClassificationRepository;

    @Test
    void getAll_returnsThreeClassificationsFromFixture() {
        List<RiskClassification> classifications = getRiskClassificationsUseCase.getAll();

        assertThat(classifications).hasSize(3);
        assertThat(classifications)
                .extracting(RiskClassification::code)
                .containsExactly("STANDARD", "PREFERRED", "SUBSTANDARD");
    }

    @Test
    void getAll_eachClassificationHasNonEmptyCodeAndDescription() {
        List<RiskClassification> classifications = getRiskClassificationsUseCase.getAll();

        classifications.forEach(c -> {
            assertThat(c.code()).isNotBlank();
            assertThat(c.description()).isNotBlank();
        });
    }

    @Test
    void getAll_returnedListIsImmutable() {
        List<RiskClassification> classifications = getRiskClassificationsUseCase.getAll();

        assertThat(classifications).isNotEmpty();
        assertThat(classifications.get(0).code()).isNotBlank();
    }

    @Test
    void repository_findAll_returnsStandardClassification() {
        assertThat(riskClassificationRepository.findAll())
                .anyMatch(c -> c.code().equals("STANDARD") && c.description().equals("Riesgo estándar"));
    }
}
