package com.sofka.insurancequoter.core.tariff.application.usecase;

import com.sofka.insurancequoter.core.tariff.domain.exception.TariffNotFoundException;
import com.sofka.insurancequoter.core.tariff.domain.model.Tariffs;
import com.sofka.insurancequoter.core.tariff.domain.port.in.UpdateTariffsUseCase;
import com.sofka.insurancequoter.core.tariff.domain.port.out.TariffRepository;

/**
 * Use case implementation that updates the tariff catalog.
 * Verifies existence via findCurrent() before persisting the new values.
 */
public class UpdateTariffsUseCaseImpl implements UpdateTariffsUseCase {

    private final TariffRepository tariffRepository;

    public UpdateTariffsUseCaseImpl(TariffRepository tariffRepository) {
        this.tariffRepository = tariffRepository;
    }

    @Override
    public Tariffs update(Tariffs tariffs) {
        tariffRepository.findCurrent()
                .orElseThrow(TariffNotFoundException::new);
        return tariffRepository.save(tariffs);
    }
}
