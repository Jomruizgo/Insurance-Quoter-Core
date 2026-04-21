package com.sofka.insurancequoter.core.agent.infrastructure;

import com.sofka.insurancequoter.core.agent.domain.model.Agent;
import com.sofka.insurancequoter.core.agent.domain.port.in.GetAgentsUseCase;
import com.sofka.insurancequoter.core.agent.domain.port.out.AgentRepository;
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
class AgentCatalogIntegrationTest {

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
    private GetAgentsUseCase getAgentsUseCase;

    @Autowired
    private AgentRepository agentRepository;

    @Test
    void getAll_returnsAgentsFromFixture() {
        List<Agent> agents = getAgentsUseCase.getAll();

        assertThat(agents).hasSize(3);
        assertThat(agents).anyMatch(a -> a.code().equals("AGT-123") && a.name().equals("Juan Pérez") && a.subscriberId().equals("SUB-001"));
        assertThat(agents).anyMatch(a -> a.code().equals("AGT-124") && a.subscriberId().equals("SUB-001"));
        assertThat(agents).anyMatch(a -> a.code().equals("AGT-201") && a.subscriberId().equals("SUB-002"));
    }

    @Test
    void getAll_returnedListIsImmutable() {
        List<Agent> agents = getAgentsUseCase.getAll();

        assertThat(agents).isNotEmpty();
        assertThat(agents.get(0).code()).isNotBlank();
        assertThat(agents.get(0).subscriberId()).isNotBlank();
    }

    @Test
    void existsByCode_returnsTrueForExistingAgent() {
        assertThat(agentRepository.existsByCode("AGT-123")).isTrue();
    }

    @Test
    void existsByCode_returnsFalseForUnknownAgent() {
        assertThat(agentRepository.existsByCode("AGT-999")).isFalse();
    }
}
