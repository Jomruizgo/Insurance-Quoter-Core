package com.sofka.insurancequoter.core.folio.application.usecase;

import com.sofka.insurancequoter.core.folio.domain.model.Folio;
import com.sofka.insurancequoter.core.folio.domain.port.in.GenerateFolioUseCase;
import com.sofka.insurancequoter.core.folio.domain.port.out.FolioSequencePort;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.Instant;

/**
 * Application use case — orchestrates folio generation.
 * Not annotated with @Service; registered as a @Bean in FolioConfig to keep
 * the application layer free of Spring infrastructure annotations.
 */
@RequiredArgsConstructor
public class GenerateFolioUseCaseImpl implements GenerateFolioUseCase {

    private final FolioSequencePort folioSequencePort;
    private final Clock clock;

    @Override
    public Folio generate() {
        String folioNumber = folioSequencePort.nextFolioNumber();
        return new Folio(folioNumber, Instant.now(clock));
    }
}
