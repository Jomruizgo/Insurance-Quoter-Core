package com.sofka.insurancequoter.core.agent.domain.port.in;

import com.sofka.insurancequoter.core.agent.domain.model.Agent;

import java.util.List;

public interface GetAgentsUseCase {
    List<Agent> getAll();
}
