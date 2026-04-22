package com.sofka.insurancequoter.core.tariff.domain.port.out;

import com.sofka.insurancequoter.core.tariff.domain.model.Tariffs;

import java.util.Optional;

/**
 * Output port: contract for persisting and retrieving the single tariff row.
 */
public interface TariffRepository {

    Optional<Tariffs> findCurrent();

    Tariffs save(Tariffs tariffs);
}
