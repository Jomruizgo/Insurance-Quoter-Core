package com.sofka.insurancequoter.core.zipcode.infrastructure.adapter.in.rest.dto;

/**
 * REST response DTO for POST /v1/zip-codes/validate.
 */
public record ZipCodeValidationResponse(boolean valid, String zipCode) {}
