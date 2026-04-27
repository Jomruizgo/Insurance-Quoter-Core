package com.sofka.insurancequoter.core.folio.infrastructure.adapter.in.rest;

import com.sofka.insurancequoter.core.folio.domain.port.in.GenerateFolioUseCase;
import com.sofka.insurancequoter.core.folio.infrastructure.adapter.in.rest.dto.FolioResponse;
import com.sofka.insurancequoter.core.folio.infrastructure.adapter.in.rest.mapper.FolioRestMapper;
import com.sofka.insurancequoter.core.folio.infrastructure.adapter.in.rest.swaggerdocs.FolioApi;
import com.sofka.insurancequoter.core.shared.infrastructure.metrics.FolioMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for the Folios resource.
 * Parses HTTP, delegates to the use case, and maps the result to a DTO.
 * No business logic — no Swagger annotations (those live in FolioApi).
 * Records folios_generated_total metric on every successful response.
 */
@RestController
@RequiredArgsConstructor
public class FolioController implements FolioApi {

    private final GenerateFolioUseCase generateFolioUseCase;
    private final FolioRestMapper folioRestMapper;
    private final FolioMetrics folioMetrics;

    @Override
    public ResponseEntity<FolioResponse> getFolio() {
        ResponseEntity<FolioResponse> response = ResponseEntity.ok(
                folioRestMapper.toResponse(generateFolioUseCase.generate()));
        folioMetrics.recordFolioGenerated();
        return response;
    }
}
