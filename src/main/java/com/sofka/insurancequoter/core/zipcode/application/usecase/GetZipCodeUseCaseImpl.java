package com.sofka.insurancequoter.core.zipcode.application.usecase;

import com.sofka.insurancequoter.core.zipcode.domain.exception.ZipCodeNotFoundException;
import com.sofka.insurancequoter.core.zipcode.domain.model.ZipCode;
import com.sofka.insurancequoter.core.zipcode.domain.port.in.GetZipCodeUseCase;
import com.sofka.insurancequoter.core.zipcode.domain.port.out.ZipCodeRepository;

/**
 * Use case implementation — retrieves full zip code data.
 * Registered as @Bean via ZipCodeConfig (no @Service annotation).
 */
public class GetZipCodeUseCaseImpl implements GetZipCodeUseCase {

    private final ZipCodeRepository zipCodeRepository;

    public GetZipCodeUseCaseImpl(ZipCodeRepository zipCodeRepository) {
        this.zipCodeRepository = zipCodeRepository;
    }

    @Override
    public ZipCode getByZipCode(String zipCode) {
        return zipCodeRepository.findByZipCode(zipCode)
                .orElseThrow(() -> new ZipCodeNotFoundException(zipCode));
    }
}
