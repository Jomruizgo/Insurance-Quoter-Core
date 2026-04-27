package com.sofka.insurancequoter.core.shared.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

/**
 * Custom metrics for the tariff bounded context.
 *
 * Domain adaptation: The Tariffs domain model is a single-row catalog record with no fireKey field.
 * The spec originally requested tariff_lookups_total{fireKey} and tariff_lookup_duration_seconds{fireKey},
 * but since fireKey does not exist in the Tariffs record, both metrics are implemented without that tag.
 * This avoids coupling the metrics layer to an artificial concept not present in the domain.
 */
@Component
public class TariffMetrics {

    private final Counter lookupCounter;
    private final MeterRegistry registry;

    public TariffMetrics(MeterRegistry registry) {
        this.registry = registry;
        this.lookupCounter = Counter.builder("tariff_lookups_total")
                .description("Number of tariff catalog lookups")
                .register(registry);
    }

    public void recordLookup() {
        lookupCounter.increment();
    }

    public Timer.Sample startTimer() {
        return Timer.start(registry);
    }

    public void stopTimer(Timer.Sample sample) {
        sample.stop(Timer.builder("tariff_lookup_duration_seconds")
                .description("Duration of tariff catalog lookups")
                .register(registry));
    }
}
