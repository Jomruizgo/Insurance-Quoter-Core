package com.sofka.insurancequoter.core.agent.infrastructure.adapter.in.rest.mapper;

import com.sofka.insurancequoter.core.agent.domain.model.Agent;
import com.sofka.insurancequoter.core.agent.infrastructure.adapter.in.rest.dto.AgentsResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AgentRestMapperTest {

    private final AgentRestMapper mapper = new AgentRestMapper();

    @Test
    void toResponse_mapsAllFieldsCorrectly() {
        // GIVEN
        List<Agent> agents = List.of(
                new Agent("AGT-123", "Juan Pérez", "SUB-001"),
                new Agent("AGT-201", "Carlos Ruiz", "SUB-002")
        );

        // WHEN
        AgentsResponse response = mapper.toResponse(agents);

        // THEN
        assertThat(response.agents()).hasSize(2);
        assertThat(response.agents().get(0).code()).isEqualTo("AGT-123");
        assertThat(response.agents().get(0).name()).isEqualTo("Juan Pérez");
        assertThat(response.agents().get(0).subscriberId()).isEqualTo("SUB-001");
        assertThat(response.agents().get(1).code()).isEqualTo("AGT-201");
        assertThat(response.agents().get(1).subscriberId()).isEqualTo("SUB-002");
    }

    @Test
    void toResponse_returnsEmptyAgentsWhenListIsEmpty() {
        // GIVEN / WHEN
        AgentsResponse response = mapper.toResponse(List.of());

        // THEN
        assertThat(response.agents()).isEmpty();
    }
}
