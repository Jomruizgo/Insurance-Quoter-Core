package com.sofka.insurancequoter.core.agent.infrastructure.adapter.out.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sofka.insurancequoter.core.agent.domain.model.Agent;
import com.sofka.insurancequoter.core.agent.domain.port.out.AgentRepository;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

public class AgentJsonAdapter implements AgentRepository {

    private final List<Agent> agents;

    public AgentJsonAdapter(ObjectMapper objectMapper, Resource resource) {
        try {
            this.agents = objectMapper.readValue(
                    resource.getInputStream(),
                    new TypeReference<>() {}
            );
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load agents from " + resource.getDescription(), e);
        }
    }

    @Override
    public List<Agent> findAll() {
        return List.copyOf(agents);
    }

    @Override
    public boolean existsByCode(String code) {
        return agents.stream().anyMatch(a -> a.code().equals(code));
    }
}
