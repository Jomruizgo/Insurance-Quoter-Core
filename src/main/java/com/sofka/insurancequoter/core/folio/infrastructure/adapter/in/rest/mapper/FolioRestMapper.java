package com.sofka.insurancequoter.core.folio.infrastructure.adapter.in.rest.mapper;

import com.sofka.insurancequoter.core.folio.domain.model.Folio;
import com.sofka.insurancequoter.core.folio.infrastructure.adapter.in.rest.dto.FolioResponse;
import org.springframework.stereotype.Component;

/**
 * Maps domain Folio value object to FolioResponse DTO.
 * Keeps the domain model decoupled from the REST layer.
 */
@Component
public class FolioRestMapper {

    public FolioResponse toResponse(Folio folio) {
        return new FolioResponse(folio.folioNumber(), folio.generatedAt());
    }
}
