package com.sofka.insurancequoter.core.zipcode.domain.port.in;

import com.sofka.insurancequoter.core.zipcode.domain.model.ZipCode;

/**
 * Input port — retrieves full zip code data by zip code string.
 * Throws ZipCodeNotFoundException when the zip code does not exist.
 */
public interface GetZipCodeUseCase {

    ZipCode getByZipCode(String zipCode);
}
