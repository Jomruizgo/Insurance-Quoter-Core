package com.sofka.insurancequoter.core.tariff.domain.port.in;

import com.sofka.insurancequoter.core.tariff.domain.model.Tariffs;

/**
 * Input port: updates the current tariff catalog values.
 * Throws TariffNotFoundException if no tariff row exists.
 */
public interface UpdateTariffsUseCase {

    Tariffs update(Tariffs tariffs);
}
