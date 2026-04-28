package com.sofka.insurancequoter.core.zipcode.application.usecase;

import com.sofka.insurancequoter.core.zipcode.domain.exception.ZipCodeNotFoundException;
import com.sofka.insurancequoter.core.zipcode.domain.model.ZipCode;
import com.sofka.insurancequoter.core.zipcode.domain.port.in.GetZipCodeUseCase;
import com.sofka.insurancequoter.core.zipcode.domain.port.out.ZipCodeRepository;
import com.sofka.insurancequoter.core.shared.infrastructure.metrics.ZipCodeMetrics;
import io.micrometer.observation.annotation.Observed;

/**
 * Use case implementation — retrieves full zip code data.
 * Registered as @Bean via ZipCodeConfig (no @Service annotation).
 *
 * @Observed creates a trace span per invocation via ObservedAspect (registered in ObservabilityConfig).
 * ZipCodeMetrics records zipcode_lookups_total{found=true|false} on each call.
 */
@Observed(name = "zipcode.get", contextualName = "get-zipcode")
public class GetZipCodeUseCaseImpl implements GetZipCodeUseCase {

    private final ZipCodeRepository zipCodeRepository;
    private final ZipCodeMetrics zipCodeMetrics;

    public GetZipCodeUseCaseImpl(ZipCodeRepository zipCodeRepository, ZipCodeMetrics zipCodeMetrics) {
        this.zipCodeRepository = zipCodeRepository;
        this.zipCodeMetrics = zipCodeMetrics;
    }

    @Override
    public ZipCode getByZipCode(String zipCode) {
        return zipCodeRepository.findByZipCode(zipCode)
                .map(result -> {
                    zipCodeMetrics.recordLookup(true);
                    return result;
                })
                .orElseThrow(() -> {
                    zipCodeMetrics.recordLookup(false);
                    return new ZipCodeNotFoundException(zipCode);
                });
    }
}
