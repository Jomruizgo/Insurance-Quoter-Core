package com.sofka.insurancequoter.core.zipcode.infrastructure.config;

import com.sofka.insurancequoter.core.zipcode.application.usecase.GetZipCodeUseCaseImpl;
import com.sofka.insurancequoter.core.zipcode.application.usecase.ValidateZipCodeUseCaseImpl;
import com.sofka.insurancequoter.core.zipcode.domain.port.in.GetZipCodeUseCase;
import com.sofka.insurancequoter.core.zipcode.domain.port.in.ValidateZipCodeUseCase;
import com.sofka.insurancequoter.core.zipcode.domain.port.out.ZipCodeRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for the zipcode bounded context.
 * Wires use case implementations without placing Spring annotations in the application layer.
 * ZipCodePersistenceAdapter is @Component and satisfies ZipCodeRepository automatically.
 */
@Configuration
public class ZipCodeConfig {

    @Bean
    public GetZipCodeUseCase getZipCodeUseCase(ZipCodeRepository zipCodeRepository) {
        return new GetZipCodeUseCaseImpl(zipCodeRepository);
    }

    @Bean
    public ValidateZipCodeUseCase validateZipCodeUseCase(ZipCodeRepository zipCodeRepository) {
        return new ValidateZipCodeUseCaseImpl(zipCodeRepository);
    }
}
