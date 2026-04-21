package com.sofka.insurancequoter.core.folio.domain.model;

import java.time.Instant;

/**
 * Value Object representing a generated folio number.
 * Pure Java record — no JPA or Spring annotations allowed in domain.
 */
public record Folio(String folioNumber, Instant generatedAt) {}
