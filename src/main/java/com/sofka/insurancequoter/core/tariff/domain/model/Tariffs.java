package com.sofka.insurancequoter.core.tariff.domain.model;

/**
 * Domain model representing the single-row tariff catalog.
 * Pure Java record — no JPA or Spring annotations.
 */
public record Tariffs(
        double fireRate,
        double cattevFactor,
        double catfhmFactor,
        double theftRate,
        double electronicEquipmentRate
) {}
