package com.sofka.insurancequoter.core.shared.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Custom metrics for the agent bounded context.
 * Tracks the number of agent catalog queries.
 */
@Component
public class AgentMetrics {

    private final Counter agentQueries;

    public AgentMetrics(MeterRegistry registry) {
        this.agentQueries = Counter.builder("agent_queries_total")
                .description("Number of agent catalog queries")
                .register(registry);
    }

    public void recordQuery() {
        agentQueries.increment();
    }
}
