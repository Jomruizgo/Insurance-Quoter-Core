package com.sofka.insurancequoter.core.zipcode.application.usecase;

import com.sofka.insurancequoter.core.zipcode.domain.model.ZipCodeValidationResult;
import com.sofka.insurancequoter.core.zipcode.domain.port.in.ValidateZipCodeUseCase;
import com.sofka.insurancequoter.core.zipcode.domain.port.out.ZipCodeRepository;

/**
 * Use case implementation — validates zip code existence.
 * Always returns a result; never throws. Registered as @Bean via ZipCodeConfig.
 */
public class ValidateZipCodeUseCaseImpl implements ValidateZipCodeUseCase {

    private final ZipCodeRepository zipCodeRepository;

    public ValidateZipCodeUseCaseImpl(ZipCodeRepository zipCodeRepository) {
        this.zipCodeRepository = zipCodeRepository;
    }

    @Override
    public ZipCodeValidationResult validate(String zipCode) {
        boolean exists = zipCodeRepository.findByZipCode(zipCode).isPresent();
        return new ZipCodeValidationResult(exists, zipCode);
    }
}
