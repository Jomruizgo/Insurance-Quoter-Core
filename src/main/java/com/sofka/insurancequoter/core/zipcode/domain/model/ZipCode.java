package com.sofka.insurancequoter.core.zipcode.domain.model;

import java.util.List;

/**
 * Domain model for a postal code (zip code).
 * Pure Java record — no JPA or Spring annotations allowed here.
 */
public record ZipCode(
        String zipCode,
        String state,
        String municipality,
        String city,
        List<String> neighborhoods,
        String catastrophicZone,
        String tevZone,
        String fhmZone
) {}
