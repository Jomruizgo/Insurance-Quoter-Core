package com.sofka.insurancequoter.core.agent.infrastructure.adapter.in.rest.mapper;

import com.sofka.insurancequoter.core.agent.domain.model.Agent;
import com.sofka.insurancequoter.core.agent.infrastructure.adapter.in.rest.dto.AgentDto;
import com.sofka.insurancequoter.core.agent.infrastructure.adapter.in.rest.dto.AgentsResponse;

import java.util.List;

public class AgentRestMapper {

    public AgentsResponse toResponse(List<Agent> agents) {
        List<AgentDto> dtos = agents.stream()
                .map(a -> new AgentDto(a.code(), a.name(), a.subscriberId()))
                .toList();
        return new AgentsResponse(dtos);
    }
}
