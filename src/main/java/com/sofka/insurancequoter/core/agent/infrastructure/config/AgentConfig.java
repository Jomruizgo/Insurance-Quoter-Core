package com.sofka.insurancequoter.core.agent.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sofka.insurancequoter.core.agent.application.usecase.GetAgentsUseCaseImpl;
import com.sofka.insurancequoter.core.agent.domain.port.in.GetAgentsUseCase;
import com.sofka.insurancequoter.core.agent.infrastructure.adapter.in.rest.mapper.AgentRestMapper;
import com.sofka.insurancequoter.core.agent.infrastructure.adapter.out.json.AgentJsonAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class AgentConfig {

    @Bean
    public AgentJsonAdapter agentJsonAdapter(ObjectMapper objectMapper) {
        return new AgentJsonAdapter(objectMapper, new ClassPathResource("fixtures/agents.json"));
    }

    @Bean
    public GetAgentsUseCase getAgentsUseCase(AgentJsonAdapter agentJsonAdapter) {
        return new GetAgentsUseCaseImpl(agentJsonAdapter);
    }

    @Bean
    public AgentRestMapper agentRestMapper() {
        return new AgentRestMapper();
    }
}
