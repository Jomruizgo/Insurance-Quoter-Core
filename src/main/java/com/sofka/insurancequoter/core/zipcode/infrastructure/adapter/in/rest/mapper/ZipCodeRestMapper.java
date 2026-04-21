package com.sofka.insurancequoter.core.zipcode.infrastructure.adapter.in.rest.mapper;

import com.sofka.insurancequoter.core.zipcode.domain.model.ZipCode;
import com.sofka.insurancequoter.core.zipcode.domain.model.ZipCodeValidationResult;
import com.sofka.insurancequoter.core.zipcode.infrastructure.adapter.in.rest.dto.ZipCodeResponse;
import com.sofka.insurancequoter.core.zipcode.infrastructure.adapter.in.rest.dto.ZipCodeValidationResponse;
import org.springframework.stereotype.Component;

/**
 * REST mapper — converts domain models to REST DTOs.
 * No business logic; pure field mapping.
 */
@Component
public class ZipCodeRestMapper {

    /**
     * Maps a ZipCode domain model to ZipCodeResponse.
     * valid is always true here — the zip code was found by the use case.
     */
    public ZipCodeResponse toResponse(ZipCode zipCode) {
        return new ZipCodeResponse(
                zipCode.zipCode(),
                zipCode.state(),
                zipCode.municipality(),
                zipCode.city(),
                zipCode.neighborhoods(),
                zipCode.catastrophicZone(),
                zipCode.tevZone(),
                zipCode.fhmZone(),
                true
        );
    }

    /**
     * Maps a ZipCodeValidationResult domain model to ZipCodeValidationResponse.
     */
    public ZipCodeValidationResponse toValidationResponse(ZipCodeValidationResult result) {
        return new ZipCodeValidationResponse(result.valid(), result.zipCode());
    }
}
