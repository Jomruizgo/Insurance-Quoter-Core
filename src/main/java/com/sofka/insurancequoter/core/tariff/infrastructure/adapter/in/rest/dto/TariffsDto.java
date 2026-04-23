package com.sofka.insurancequoter.core.tariff.infrastructure.adapter.in.rest.dto;

/**
 * DTO that carries all 15 tariff values in API responses.
 * Consumed by plataforma-danos-back via GET /v1/tariffs.
 */
public record TariffsDto(
        double fireRate,
        double fireContentsRate,
        double coverageExtensionFactor,
        double cattevFactor,
        double catfhmFactor,
        double debrisRemovalFactor,
        double extraordinaryExpensesFactor,
        double rentalLossRate,
        double businessInterruptionRate,
        double electronicEquipmentRate,
        double theftRate,
        double cashAndValuesRate,
        double glassRate,
        double luminousSignageRate,
        double commercialFactor
) {}
