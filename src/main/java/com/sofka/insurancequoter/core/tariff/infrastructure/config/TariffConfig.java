package com.sofka.insurancequoter.core.tariff.infrastructure.config;

import com.sofka.insurancequoter.core.shared.infrastructure.metrics.TariffMetrics;
import com.sofka.insurancequoter.core.tariff.application.usecase.GetTariffsUseCaseImpl;
import com.sofka.insurancequoter.core.tariff.application.usecase.UpdateTariffsUseCaseImpl;
import com.sofka.insurancequoter.core.tariff.domain.port.in.GetTariffsUseCase;
import com.sofka.insurancequoter.core.tariff.domain.port.in.UpdateTariffsUseCase;
import com.sofka.insurancequoter.core.tariff.domain.port.out.TariffRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for the tariff bounded context.
 * Wires use case implementations so no @Service annotation is needed in the application layer.
 * TariffMetrics is injected into GetTariffsUseCaseImpl to record lookup counters and timers.
 */
@Configuration
public class TariffConfig {

    @Bean
    public GetTariffsUseCase getTariffsUseCase(TariffRepository tariffRepository, TariffMetrics tariffMetrics) {
        return new GetTariffsUseCaseImpl(tariffRepository, tariffMetrics);
    }

    @Bean
    public UpdateTariffsUseCase updateTariffsUseCase(TariffRepository tariffRepository) {
        return new UpdateTariffsUseCaseImpl(tariffRepository);
    }
}
