package com.sofka.insurancequoter.core.folio.infrastructure.config;

import com.sofka.insurancequoter.core.folio.application.usecase.GenerateFolioUseCaseImpl;
import com.sofka.insurancequoter.core.folio.domain.port.in.GenerateFolioUseCase;
import com.sofka.insurancequoter.core.folio.domain.port.out.FolioSequencePort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Spring configuration for the folio bounded context.
 * Wires the use case and the Clock bean so they are injectable
 * without placing Spring annotations in the application layer.
 */
@Configuration
public class FolioConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public GenerateFolioUseCase generateFolioUseCase(FolioSequencePort folioSequencePort, Clock clock) {
        return new GenerateFolioUseCaseImpl(folioSequencePort, clock);
    }
}
