package com.sofka.insurancequoter.core.shared.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Custom metrics for the subscriber bounded context.
 * Tracks the number of subscriber catalog queries.
 */
@Component
public class SubscriberMetrics {

    private final Counter subscriberQueries;

    public SubscriberMetrics(MeterRegistry registry) {
        this.subscriberQueries = Counter.builder("subscriber_queries_total")
                .description("Number of subscriber catalog queries")
                .register(registry);
    }

    public void recordQuery() {
        subscriberQueries.increment();
    }
}
