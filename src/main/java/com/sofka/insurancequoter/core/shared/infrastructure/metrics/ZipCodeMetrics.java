package com.sofka.insurancequoter.core.shared.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Custom metrics for the zipcode bounded context.
 * Tracks zip code lookups tagged by whether the code was found or not.
 */
@Component
public class ZipCodeMetrics {

    private final MeterRegistry registry;

    public ZipCodeMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordLookup(boolean found) {
        Counter.builder("zipcode_lookups_total")
                .tag("found", String.valueOf(found))
                .description("Number of zip code lookups, tagged by result")
                .register(registry)
                .increment();
    }
}
