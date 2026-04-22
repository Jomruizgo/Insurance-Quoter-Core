package com.sofka.insurancequoter.core.tariff.infrastructure.adapter.in.rest.dto;

/**
 * DTO that carries the tariff values in API responses.
 */
public record TariffsDto(
        double fireRate,
        double cattevFactor,
        double catfhmFactor,
        double theftRate,
        double electronicEquipmentRate
) {}
