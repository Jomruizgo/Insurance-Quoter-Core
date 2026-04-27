package com.sofka.insurancequoter.core.shared.infrastructure.metrics;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SubscriberMetricsTest {

    @Test
    void recordQuery_incrementsCounter() {
        // GIVEN
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        SubscriberMetrics metrics = new SubscriberMetrics(registry);

        // WHEN
        metrics.recordQuery();

        // THEN
        assertThat(registry.counter("subscriber_queries_total").count()).isEqualTo(1.0);
    }

    @Test
    void recordQuery_calledTwice_counterIsTwo() {
        // GIVEN
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        SubscriberMetrics metrics = new SubscriberMetrics(registry);

        // WHEN
        metrics.recordQuery();
        metrics.recordQuery();

        // THEN
        assertThat(registry.counter("subscriber_queries_total").count()).isEqualTo(2.0);
    }
}
