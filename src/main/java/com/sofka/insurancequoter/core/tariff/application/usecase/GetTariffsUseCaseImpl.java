package com.sofka.insurancequoter.core.tariff.application.usecase;

import com.sofka.insurancequoter.core.tariff.domain.exception.TariffNotFoundException;
import com.sofka.insurancequoter.core.tariff.domain.model.Tariffs;
import com.sofka.insurancequoter.core.tariff.domain.port.in.GetTariffsUseCase;
import com.sofka.insurancequoter.core.tariff.domain.port.out.TariffRepository;

/**
 * Use case implementation that retrieves the current tariff catalog.
 * Throws TariffNotFoundException if the catalog has not been seeded.
 */
public class GetTariffsUseCaseImpl implements GetTariffsUseCase {

    private final TariffRepository tariffRepository;

    public GetTariffsUseCaseImpl(TariffRepository tariffRepository) {
        this.tariffRepository = tariffRepository;
    }

    @Override
    public Tariffs getCurrent() {
        return tariffRepository.findCurrent()
                .orElseThrow(TariffNotFoundException::new);
    }
}
