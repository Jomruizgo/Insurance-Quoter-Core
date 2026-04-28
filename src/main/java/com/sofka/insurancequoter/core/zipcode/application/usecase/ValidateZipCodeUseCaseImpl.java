package com.sofka.insurancequoter.core.zipcode.application.usecase;

import com.sofka.insurancequoter.core.zipcode.domain.model.ZipCodeValidationResult;
import com.sofka.insurancequoter.core.zipcode.domain.port.in.ValidateZipCodeUseCase;
import com.sofka.insurancequoter.core.zipcode.domain.port.out.ZipCodeRepository;
import io.micrometer.observation.annotation.Observed;

/**
 * Use case implementation — validates zip code existence.
 * Always returns a result; never throws. Registered as @Bean via ZipCodeConfig.
 *
 * @Observed creates a trace span per invocation via ObservedAspect (registered in ObservabilityConfig).
 */
@Observed(name = "zipcode.validate", contextualName = "validate-zipcode")
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
