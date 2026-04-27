package com.sofka.insurancequoter.core.shared.infrastructure.metrics;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for TariffMetrics.
 *
 * Domain adaptation note: Tariffs is a single-row catalog with no fireKey field.
 * tariff_lookups_total and tariff_lookup_duration_seconds are implemented without
 * a fireKey tag — see TariffMetrics for full justification.
 */
class TariffMetricsTest {

    @Test
    void recordLookup_incrementsCounter() {
        // GIVEN
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        TariffMetrics metrics = new TariffMetrics(registry);

        // WHEN
        metrics.recordLookup();

        // THEN
        assertThat(registry.counter("tariff_lookups_total").count()).isEqualTo(1.0);
    }

    @Test
    void recordLookup_calledTwice_counterIsTwo() {
        // GIVEN
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        TariffMetrics metrics = new TariffMetrics(registry);

        // WHEN
        metrics.recordLookup();
        metrics.recordLookup();

        // THEN
        assertThat(registry.counter("tariff_lookups_total").count()).isEqualTo(2.0);
    }

    @Test
    void startAndStopTimer_recordsDuration() {
        // GIVEN
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        TariffMetrics metrics = new TariffMetrics(registry);

        // WHEN
        var sample = metrics.startTimer();
        metrics.stopTimer(sample);

        // THEN — timer must have recorded exactly 1 observation
        assertThat(registry.timer("tariff_lookup_duration_seconds").count()).isEqualTo(1L);
    }
}
