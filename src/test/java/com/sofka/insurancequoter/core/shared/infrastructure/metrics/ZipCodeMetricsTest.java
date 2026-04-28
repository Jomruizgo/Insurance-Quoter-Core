package com.sofka.insurancequoter.core.shared.infrastructure.metrics;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ZipCodeMetricsTest {

    @Test
    void recordLookup_found_incrementsCounterWithTagTrue() {
        // GIVEN
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        ZipCodeMetrics metrics = new ZipCodeMetrics(registry);

        // WHEN
        metrics.recordLookup(true);

        // THEN
        assertThat(registry.counter("zipcode_lookups_total", "found", "true").count()).isEqualTo(1.0);
    }

    @Test
    void recordLookup_notFound_incrementsCounterWithTagFalse() {
        // GIVEN
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        ZipCodeMetrics metrics = new ZipCodeMetrics(registry);

        // WHEN
        metrics.recordLookup(false);

        // THEN
        assertThat(registry.counter("zipcode_lookups_total", "found", "false").count()).isEqualTo(1.0);
    }

    @Test
    void recordLookup_foundAndNotFound_tagsAreIndependent() {
        // GIVEN
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        ZipCodeMetrics metrics = new ZipCodeMetrics(registry);

        // WHEN
        metrics.recordLookup(true);
        metrics.recordLookup(true);
        metrics.recordLookup(false);

        // THEN
        assertThat(registry.counter("zipcode_lookups_total", "found", "true").count()).isEqualTo(2.0);
        assertThat(registry.counter("zipcode_lookups_total", "found", "false").count()).isEqualTo(1.0);
    }
}
