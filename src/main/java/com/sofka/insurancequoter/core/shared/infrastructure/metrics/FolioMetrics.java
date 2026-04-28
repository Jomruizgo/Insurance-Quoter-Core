package com.sofka.insurancequoter.core.shared.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Custom metrics for the folio bounded context.
 * Tracks the number of folios successfully generated.
 */
@Component
public class FolioMetrics {

    private final Counter foliosGenerated;

    public FolioMetrics(MeterRegistry registry) {
        this.foliosGenerated = Counter.builder("folios_generated_total")
                .description("Number of folios successfully generated")
                .register(registry);
    }

    public void recordFolioGenerated() {
        foliosGenerated.increment();
    }
}
