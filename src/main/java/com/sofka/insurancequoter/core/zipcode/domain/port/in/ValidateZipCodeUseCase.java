package com.sofka.insurancequoter.core.zipcode.domain.port.in;

import com.sofka.insurancequoter.core.zipcode.domain.model.ZipCodeValidationResult;

/**
 * Input port — validates whether a zip code exists in the catalog.
 * Always returns a result (never throws); valid=false when the zip code is not found.
 */
public interface ValidateZipCodeUseCase {

    ZipCodeValidationResult validate(String zipCode);
}
