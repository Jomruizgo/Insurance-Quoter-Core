package com.sofka.insurancequoter.core.agent.infrastructure.adapter.in.rest;

import com.sofka.insurancequoter.core.agent.domain.port.in.GetAgentsUseCase;
import com.sofka.insurancequoter.core.agent.infrastructure.adapter.in.rest.dto.AgentsResponse;
import com.sofka.insurancequoter.core.agent.infrastructure.adapter.in.rest.mapper.AgentRestMapper;
import com.sofka.insurancequoter.core.agent.infrastructure.adapter.in.rest.swaggerdocs.AgentApi;
import com.sofka.insurancequoter.core.shared.infrastructure.metrics.AgentMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for the Agents resource.
 * Records agent_queries_total metric on every successful response.
 */
@RestController
@RequiredArgsConstructor
public class AgentController implements AgentApi {

    private final GetAgentsUseCase getAgentsUseCase;
    private final AgentRestMapper agentRestMapper;
    private final AgentMetrics agentMetrics;

    @Override
    public ResponseEntity<AgentsResponse> getAgents() {
        ResponseEntity<AgentsResponse> response = ResponseEntity.ok(
                agentRestMapper.toResponse(getAgentsUseCase.getAll()));
        agentMetrics.recordQuery();
        return response;
    }
}
