package com.sofka.insurancequoter.core.zipcode.infrastructure.adapter.in.rest.dto;

import java.util.List;

/**
 * REST response DTO for GET /v1/zip-codes/{zipCode}.
 * valid is always true when returned from the GET endpoint (zip code was found).
 */
public record ZipCodeResponse(
        String zipCode,
        String state,
        String municipality,
        String city,
        List<String> neighborhoods,
        String catastrophicZone,
        String tevZone,
        String fhmZone,
        boolean valid
) {}
