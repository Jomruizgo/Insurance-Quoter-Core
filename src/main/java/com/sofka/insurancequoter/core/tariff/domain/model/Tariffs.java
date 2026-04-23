package com.sofka.insurancequoter.core.tariff.domain.model;

/**
 * Domain model representing the single-row tariff catalog.
 * Pure Java record — no JPA or Spring annotations.
 * Contains all 15 rate/factor fields required for full premium calculation.
 */
public record Tariffs(
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
