package com.sofka.insurancequoter.core.shared.infrastructure.metrics;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FolioMetricsTest {

    @Test
    void recordFolioGenerated_incrementsCounter() {
        // GIVEN
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        FolioMetrics metrics = new FolioMetrics(registry);

        // WHEN
        metrics.recordFolioGenerated();

        // THEN
        assertThat(registry.counter("folios_generated_total").count()).isEqualTo(1.0);
    }

    @Test
    void recordFolioGenerated_calledTwice_counterIsTwo() {
        // GIVEN
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        FolioMetrics metrics = new FolioMetrics(registry);

        // WHEN
        metrics.recordFolioGenerated();
        metrics.recordFolioGenerated();

        // THEN
        assertThat(registry.counter("folios_generated_total").count()).isEqualTo(2.0);
    }
}
