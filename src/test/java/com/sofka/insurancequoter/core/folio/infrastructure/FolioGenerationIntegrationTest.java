package com.sofka.insurancequoter.core.folio.infrastructure;

import com.sofka.insurancequoter.core.folio.domain.model.Folio;
import com.sofka.insurancequoter.core.folio.domain.port.in.GenerateFolioUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class FolioGenerationIntegrationTest {

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
    private GenerateFolioUseCase generateFolioUseCase;

    @Test
    void consecutiveCalls_produceSequentialFolios() {
        Folio first = generateFolioUseCase.generate();
        Folio second = generateFolioUseCase.generate();

        int seq1 = extractSequence(first.folioNumber());
        int seq2 = extractSequence(second.folioNumber());

        assertThat(seq2).isEqualTo(seq1 + 1);
        assertThat(first.generatedAt()).isNotNull();
        assertThat(second.generatedAt()).isNotNull();
    }

    private int extractSequence(String folioNumber) {
        // FOL-2026-00043 → 43
        return Integer.parseInt(folioNumber.substring(folioNumber.lastIndexOf('-') + 1));
    }
}
