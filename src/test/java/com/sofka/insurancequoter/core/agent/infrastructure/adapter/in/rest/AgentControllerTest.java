package com.sofka.insurancequoter.core.agent.infrastructure.adapter.in.rest;

import com.sofka.insurancequoter.core.agent.domain.model.Agent;
import com.sofka.insurancequoter.core.agent.domain.port.in.GetAgentsUseCase;
import com.sofka.insurancequoter.core.agent.infrastructure.adapter.in.rest.dto.AgentDto;
import com.sofka.insurancequoter.core.agent.infrastructure.adapter.in.rest.dto.AgentsResponse;
import com.sofka.insurancequoter.core.agent.infrastructure.adapter.in.rest.mapper.AgentRestMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentControllerTest {

    @Mock
    private GetAgentsUseCase getAgentsUseCase;

    @Mock
    private AgentRestMapper agentRestMapper;

    @InjectMocks
    private AgentController controller;

    @Test
    void getAgents_returnsHttp200WithAgentList() {
        // GIVEN
        List<Agent> agents = List.of(new Agent("AGT-123", "Juan Pérez", "SUB-001"));
        AgentsResponse response = new AgentsResponse(List.of(new AgentDto("AGT-123", "Juan Pérez", "SUB-001")));
        when(getAgentsUseCase.getAll()).thenReturn(agents);
        when(agentRestMapper.toResponse(agents)).thenReturn(response);

        // WHEN
        ResponseEntity<AgentsResponse> result = controller.getAgents();

        // THEN
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().agents()).hasSize(1);
        assertThat(result.getBody().agents().get(0).code()).isEqualTo("AGT-123");
        assertThat(result.getBody().agents().get(0).subscriberId()).isEqualTo("SUB-001");
    }

    @Test
    void getAgents_delegatesToUseCaseAndMapper() {
        // GIVEN
        List<Agent> agents = List.of();
        when(getAgentsUseCase.getAll()).thenReturn(agents);
        when(agentRestMapper.toResponse(agents)).thenReturn(new AgentsResponse(List.of()));

        // WHEN
        controller.getAgents();

        // THEN
        verify(getAgentsUseCase, times(1)).getAll();
        verify(agentRestMapper, times(1)).toResponse(agents);
        verifyNoMoreInteractions(getAgentsUseCase, agentRestMapper);
    }
}
