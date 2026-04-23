package com.sofka.insurancequoter.core.zipcode.domain.model;

/**
 * Domain model representing the result of validating a zip code existence.
 * Pure Java record — no JPA or Spring annotations allowed here.
 */
public record ZipCodeValidationResult(boolean valid, String zipCode) {}
