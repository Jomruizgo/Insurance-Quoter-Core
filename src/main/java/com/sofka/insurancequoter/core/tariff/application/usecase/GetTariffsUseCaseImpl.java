package com.sofka.insurancequoter.core.tariff.application.usecase;

import com.sofka.insurancequoter.core.tariff.domain.exception.TariffNotFoundException;
import com.sofka.insurancequoter.core.tariff.domain.model.Tariffs;
import com.sofka.insurancequoter.core.tariff.domain.port.in.GetTariffsUseCase;
import com.sofka.insurancequoter.core.tariff.domain.port.out.TariffRepository;
import com.sofka.insurancequoter.core.shared.infrastructure.metrics.TariffMetrics;
import io.micrometer.core.instrument.Timer;
import io.micrometer.observation.annotation.Observed;

/**
 * Use case implementation that retrieves the current tariff catalog.
 * Throws TariffNotFoundException if the catalog has not been seeded.
 *
 * @Observed creates a trace span per invocation via ObservedAspect (registered in ObservabilityConfig).
 * TariffMetrics records tariff_lookups_total (counter) and tariff_lookup_duration_seconds (timer).
 * No fireKey tag — the Tariffs domain model is a single-row catalog with no fireKey field.
 */
@Observed(name = "tariff.get-all", contextualName = "get-tariffs")
public class GetTariffsUseCaseImpl implements GetTariffsUseCase {

    private final TariffRepository tariffRepository;
    private final TariffMetrics tariffMetrics;

    public GetTariffsUseCaseImpl(TariffRepository tariffRepository, TariffMetrics tariffMetrics) {
        this.tariffRepository = tariffRepository;
        this.tariffMetrics = tariffMetrics;
    }

    @Override
    public Tariffs getCurrent() {
        Timer.Sample sample = tariffMetrics.startTimer();
        try {
            Tariffs result = tariffRepository.findCurrent()
                    .orElseThrow(TariffNotFoundException::new);
            tariffMetrics.recordLookup();
            return result;
        } finally {
            tariffMetrics.stopTimer(sample);
        }
    }
}
