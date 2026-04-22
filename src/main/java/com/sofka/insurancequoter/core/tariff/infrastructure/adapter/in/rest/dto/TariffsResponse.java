package com.sofka.insurancequoter.core.tariff.infrastructure.adapter.in.rest.dto;

/**
 * Wrapper response for GET and PUT /v1/tariffs endpoints.
 */
public record TariffsResponse(TariffsDto tariffs) {}
