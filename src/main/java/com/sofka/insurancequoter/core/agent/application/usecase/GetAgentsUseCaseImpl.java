package com.sofka.insurancequoter.core.agent.application.usecase;

import com.sofka.insurancequoter.core.agent.domain.model.Agent;
import com.sofka.insurancequoter.core.agent.domain.port.in.GetAgentsUseCase;
import com.sofka.insurancequoter.core.agent.domain.port.out.AgentRepository;
import io.micrometer.observation.annotation.Observed;

import java.util.List;

/**
 * @Observed creates a trace span per invocation via ObservedAspect (registered in ObservabilityConfig).
 * The Spring @Bean proxy in AgentConfig enables AOP interception.
 */
@Observed(name = "agent.get-all", contextualName = "get-agents")
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
