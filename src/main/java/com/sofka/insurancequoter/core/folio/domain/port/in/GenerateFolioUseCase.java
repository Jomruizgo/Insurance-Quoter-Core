package com.sofka.insurancequoter.core.folio.domain.port.in;

import com.sofka.insurancequoter.core.folio.domain.model.Folio;

/**
 * Input Port — defines the use case contract for folio generation.
 * Implemented by GenerateFolioUseCaseImpl in the application layer.
 */
public interface GenerateFolioUseCase {

    Folio generate();
}
