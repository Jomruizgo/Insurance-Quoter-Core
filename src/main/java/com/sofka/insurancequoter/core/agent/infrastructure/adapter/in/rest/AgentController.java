package com.sofka.insurancequoter.core.agent.infrastructure.adapter.in.rest;

import com.sofka.insurancequoter.core.agent.domain.port.in.GetAgentsUseCase;
import com.sofka.insurancequoter.core.agent.infrastructure.adapter.in.rest.dto.AgentsResponse;
import com.sofka.insurancequoter.core.agent.infrastructure.adapter.in.rest.mapper.AgentRestMapper;
import com.sofka.insurancequoter.core.agent.infrastructure.adapter.in.rest.swaggerdocs.AgentApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AgentController implements AgentApi {

    private final GetAgentsUseCase getAgentsUseCase;
    private final AgentRestMapper agentRestMapper;

    @Override
    public ResponseEntity<AgentsResponse> getAgents() {
        return ResponseEntity.ok(agentRestMapper.toResponse(getAgentsUseCase.getAll()));
    }
}
