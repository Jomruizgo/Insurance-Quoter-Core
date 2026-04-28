package com.sofka.insurancequoter.core.shared.infrastructure.metrics;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AgentMetricsTest {

    @Test
    void recordQuery_incrementsCounter() {
        // GIVEN
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        AgentMetrics metrics = new AgentMetrics(registry);

        // WHEN
        metrics.recordQuery();

        // THEN
        assertThat(registry.counter("agent_queries_total").count()).isEqualTo(1.0);
    }

    @Test
    void recordQuery_calledThreeTimes_counterIsThree() {
        // GIVEN
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        AgentMetrics metrics = new AgentMetrics(registry);

        // WHEN
        metrics.recordQuery();
        metrics.recordQuery();
        metrics.recordQuery();

        // THEN
        assertThat(registry.counter("agent_queries_total").count()).isEqualTo(3.0);
    }
}
