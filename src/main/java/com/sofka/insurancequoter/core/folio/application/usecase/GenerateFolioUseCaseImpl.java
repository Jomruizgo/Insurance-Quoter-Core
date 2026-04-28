package com.sofka.insurancequoter.core.folio.application.usecase;

import com.sofka.insurancequoter.core.folio.domain.model.Folio;
import com.sofka.insurancequoter.core.folio.domain.port.in.GenerateFolioUseCase;
import com.sofka.insurancequoter.core.folio.domain.port.out.FolioSequencePort;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.Instant;

/**
 * Application use case — orchestrates folio generation.
 * Not annotated with @Service; registered as a @Bean in FolioConfig to keep
 * the application layer free of Spring infrastructure annotations.
 *
 * @Observed generates a trace span and timer metric per method invocation.
 * The AOP proxy is created by Spring because this class is returned by a @Bean factory.
 */
@Observed(name = "folio.generate", contextualName = "generate-folio")
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
