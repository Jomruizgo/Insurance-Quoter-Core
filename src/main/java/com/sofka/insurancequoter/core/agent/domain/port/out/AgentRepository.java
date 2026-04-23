package com.sofka.insurancequoter.core.agent.domain.port.out;

import com.sofka.insurancequoter.core.agent.domain.model.Agent;

import java.util.List;

public interface AgentRepository {
    List<Agent> findAll();
    boolean existsByCode(String code);
}
