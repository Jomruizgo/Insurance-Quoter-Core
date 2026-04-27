package com.sofka.insurancequoter.core.shared.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Custom metrics for catalog error tracking.
 * Counts errors by their Java class simple name (e.g. "TariffNotFoundException").
 * The errorType tag comes from exception class names — these are controlled values,
 * not from free user input, so cardinality remains bounded.
 */
@Component
public class CatalogErrorMetrics {

    private final MeterRegistry registry;

    public CatalogErrorMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordError(String errorType) {
        Counter.builder("catalog_errors_total")
                .tag("errorType", errorType)
                .description("Number of catalog errors by type")
                .register(registry)
                .increment();
    }
}
