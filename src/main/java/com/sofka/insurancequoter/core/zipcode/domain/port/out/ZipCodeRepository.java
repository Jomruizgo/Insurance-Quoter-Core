package com.sofka.insurancequoter.core.zipcode.domain.port.out;

import com.sofka.insurancequoter.core.zipcode.domain.model.ZipCode;

import java.util.Optional;

/**
 * Output port — contract for zip code persistence access.
 * Implemented by the infrastructure persistence adapter.
 */
public interface ZipCodeRepository {

    Optional<ZipCode> findByZipCode(String zipCode);
}
