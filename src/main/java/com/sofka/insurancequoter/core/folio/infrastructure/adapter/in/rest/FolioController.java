package com.sofka.insurancequoter.core.folio.infrastructure.adapter.in.rest;

import com.sofka.insurancequoter.core.folio.domain.port.in.GenerateFolioUseCase;
import com.sofka.insurancequoter.core.folio.infrastructure.adapter.in.rest.dto.FolioResponse;
import com.sofka.insurancequoter.core.folio.infrastructure.adapter.in.rest.mapper.FolioRestMapper;
import com.sofka.insurancequoter.core.folio.infrastructure.adapter.in.rest.swaggerdocs.FolioApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for the Folios resource.
 * Parses HTTP, delegates to the use case, and maps the result to a DTO.
 * No business logic — no Swagger annotations (those live in FolioApi).
 */
@RestController
@RequiredArgsConstructor
public class FolioController implements FolioApi {

    private final GenerateFolioUseCase generateFolioUseCase;
    private final FolioRestMapper folioRestMapper;

    @Override
    public ResponseEntity<FolioResponse> getFolio() {
        return ResponseEntity.ok(folioRestMapper.toResponse(generateFolioUseCase.generate()));
    }
}
