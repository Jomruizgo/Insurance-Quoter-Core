package com.sofka.insurancequoter.core.tariff.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.Positive;

/**
 * Request body for PUT /v1/tariffs.
 * All fields must be strictly positive (> 0); @Positive rejects 0 and negatives.
 */
public record UpdateTariffsRequest(
        @Positive double fireRate,
        @Positive double cattevFactor,
        @Positive double catfhmFactor,
        @Positive double theftRate,
        @Positive double electronicEquipmentRate
) {}
