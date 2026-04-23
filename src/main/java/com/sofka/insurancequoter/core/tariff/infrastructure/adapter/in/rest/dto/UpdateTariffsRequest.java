package com.sofka.insurancequoter.core.tariff.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.Positive;

/**
 * Request body for PUT /v1/tariffs.
 * All fields must be strictly positive (> 0); @Positive rejects 0 and negatives.
 */
public record UpdateTariffsRequest(
        @Positive double fireRate,
        @Positive double fireContentsRate,
        @Positive double coverageExtensionFactor,
        @Positive double cattevFactor,
        @Positive double catfhmFactor,
        @Positive double debrisRemovalFactor,
        @Positive double extraordinaryExpensesFactor,
        @Positive double rentalLossRate,
        @Positive double businessInterruptionRate,
        @Positive double electronicEquipmentRate,
        @Positive double theftRate,
        @Positive double cashAndValuesRate,
        @Positive double glassRate,
        @Positive double luminousSignageRate,
        @Positive double commercialFactor
) {}
