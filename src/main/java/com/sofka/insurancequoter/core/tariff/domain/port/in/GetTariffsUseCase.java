package com.sofka.insurancequoter.core.tariff.domain.port.in;

import com.sofka.insurancequoter.core.tariff.domain.model.Tariffs;

/**
 * Input port: retrieves the current (single-row) tariff catalog.
 */
public interface GetTariffsUseCase {

    Tariffs getCurrent();
}
