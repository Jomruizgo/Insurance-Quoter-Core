package com.sofka.insurancequoter.core.zipcode.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * REST request DTO for POST /v1/zip-codes/validate.
 */
public record ValidateZipCodeRequest(@NotBlank String zipCode) {}
