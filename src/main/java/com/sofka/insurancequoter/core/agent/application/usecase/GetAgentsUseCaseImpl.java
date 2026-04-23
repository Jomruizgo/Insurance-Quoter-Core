package com.sofka.insurancequoter.core.agent.application.usecase;

import com.sofka.insurancequoter.core.agent.domain.model.Agent;
import com.sofka.insurancequoter.core.agent.domain.port.in.GetAgentsUseCase;
import com.sofka.insurancequoter.core.agent.domain.port.out.AgentRepository;

import java.util.List;

public class GetAgentsUseCaseImpl implements GetAgentsUseCase {

    private final AgentRepository agentRepository;

    public GetAgentsUseCaseImpl(AgentRepository agentRepository) {
        this.agentRepository = agentRepository;
    }

    @Override
    public List<Agent> getAll() {
        return agentRepository.findAll();
    }
}
