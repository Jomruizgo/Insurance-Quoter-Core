package com.sofka.insurancequoter.core.shared.infrastructure.metrics;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogErrorMetricsTest {

    @Test
    void recordError_incrementsCounterWithErrorTypeTag() {
        // GIVEN
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        CatalogErrorMetrics metrics = new CatalogErrorMetrics(registry);

        // WHEN
        metrics.recordError("RuntimeException");

        // THEN
        assertThat(registry.counter("catalog_errors_total", "errorType", "RuntimeException").count())
                .isEqualTo(1.0);
    }

    @Test
    void recordError_differentErrorTypes_countersAreIndependent() {
        // GIVEN
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        CatalogErrorMetrics metrics = new CatalogErrorMetrics(registry);

        // WHEN
        metrics.recordError("RuntimeException");
        metrics.recordError("RuntimeException");
        metrics.recordError("IllegalArgumentException");

        // THEN
        assertThat(registry.counter("catalog_errors_total", "errorType", "RuntimeException").count())
                .isEqualTo(2.0);
        assertThat(registry.counter("catalog_errors_total", "errorType", "IllegalArgumentException").count())
                .isEqualTo(1.0);
    }

    @Test
    void recordError_sameType_accumulatesCount() {
        // GIVEN
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        CatalogErrorMetrics metrics = new CatalogErrorMetrics(registry);

        // WHEN
        metrics.recordError("TariffNotFoundException");
        metrics.recordError("TariffNotFoundException");
        metrics.recordError("TariffNotFoundException");

        // THEN
        assertThat(registry.counter("catalog_errors_total", "errorType", "TariffNotFoundException").count())
                .isEqualTo(3.0);
    }
}
