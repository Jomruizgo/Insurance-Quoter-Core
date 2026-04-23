package com.sofka.insurancequoter.core.tariff.domain.exception;

/**
 * Thrown when no tariff row exists in the catalog (id = 1).
 */
public class TariffNotFoundException extends RuntimeException {

    public TariffNotFoundException() {
        super("Tariffs not found");
    }
}
