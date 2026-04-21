package com.sofka.insurancequoter.core.folio.infrastructure.adapter.in.rest.dto;

import java.time.Instant;

/**
 * REST response DTO for the folio generation endpoint.
 * Maps from the Folio domain value object — no JPA annotations allowed here.
 */
public record FolioResponse(String folioNumber, Instant generatedAt) {}
