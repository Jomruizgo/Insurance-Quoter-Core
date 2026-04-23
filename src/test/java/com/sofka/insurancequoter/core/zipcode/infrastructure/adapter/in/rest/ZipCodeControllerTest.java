package com.sofka.insurancequoter.core.zipcode.infrastructure.adapter.in.rest;

import com.sofka.insurancequoter.core.zipcode.domain.exception.ZipCodeNotFoundException;
import com.sofka.insurancequoter.core.zipcode.domain.model.ZipCode;
import com.sofka.insurancequoter.core.zipcode.domain.model.ZipCodeValidationResult;
import com.sofka.insurancequoter.core.zipcode.domain.port.in.GetZipCodeUseCase;
import com.sofka.insurancequoter.core.zipcode.domain.port.in.ValidateZipCodeUseCase;
import com.sofka.insurancequoter.core.zipcode.infrastructure.adapter.in.rest.dto.ValidateZipCodeRequest;
import com.sofka.insurancequoter.core.zipcode.infrastructure.adapter.in.rest.dto.ZipCodeResponse;
import com.sofka.insurancequoter.core.zipcode.infrastructure.adapter.in.rest.dto.ZipCodeValidationResponse;
import com.sofka.insurancequoter.core.zipcode.infrastructure.adapter.in.rest.mapper.ZipCodeRestMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ZipCodeControllerTest {

    @Mock
    private GetZipCodeUseCase getZipCodeUseCase;

    @Mock
    private ValidateZipCodeUseCase validateZipCodeUseCase;

    @Mock
    private ZipCodeRestMapper zipCodeRestMapper;

    @InjectMocks
    private ZipCodeController controller;

    // ---- GET /v1/zip-codes/{zipCode} ----

    @Test
    void getZipCode_whenExists_returns200WithBody() {
        // GIVEN
        ZipCode zipCode = new ZipCode("06600", "Ciudad de México", "Cuauhtémoc", "Ciudad de México",
                List.of("Juárez", "Tabacalera"), "ZONE_A", "TEV-1", "FHM-2");
        ZipCodeResponse response = new ZipCodeResponse("06600", "Ciudad de México", "Cuauhtémoc",
                "Ciudad de México", List.of("Juárez", "Tabacalera"), "ZONE_A", "TEV-1", "FHM-2", true);

        when(getZipCodeUseCase.getByZipCode("06600")).thenReturn(zipCode);
        when(zipCodeRestMapper.toResponse(zipCode)).thenReturn(response);

        // WHEN
        ResponseEntity<ZipCodeResponse> result = controller.getZipCode("06600");

        // THEN
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().zipCode()).isEqualTo("06600");
        assertThat(result.getBody().valid()).isTrue();
    }

    @Test
    void handleZipCodeNotFound_returns404WithStandardErrorBody() {
        // GIVEN — the exception handler is tested directly (Mockito-only, no Spring dispatcher)
        ZipCodeNotFoundException ex = new ZipCodeNotFoundException("99999");

        // WHEN
        ResponseEntity<Map<String, String>> result = controller.handleZipCodeNotFound(ex);

        // THEN
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().get("error")).isEqualTo("Zip code not found");
        assertThat(result.getBody().get("code")).isEqualTo("ZIP_CODE_NOT_FOUND");
    }

    // ---- POST /v1/zip-codes/validate ----

    @Test
    void validate_whenZipCodeIsValid_returns200WithValidTrue() {
        // GIVEN
        ValidateZipCodeRequest request = new ValidateZipCodeRequest("06600");
        ZipCodeValidationResult domainResult = new ZipCodeValidationResult(true, "06600");
        ZipCodeValidationResponse response = new ZipCodeValidationResponse(true, "06600");

        when(validateZipCodeUseCase.validate("06600")).thenReturn(domainResult);
        when(zipCodeRestMapper.toValidationResponse(domainResult)).thenReturn(response);

        // WHEN
        ResponseEntity<ZipCodeValidationResponse> result = controller.validate(request);

        // THEN
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().valid()).isTrue();
        assertThat(result.getBody().zipCode()).isEqualTo("06600");
    }

    @Test
    void validate_whenZipCodeIsInvalid_returns200WithValidFalse() {
        // GIVEN
        ValidateZipCodeRequest request = new ValidateZipCodeRequest("99999");
        ZipCodeValidationResult domainResult = new ZipCodeValidationResult(false, "99999");
        ZipCodeValidationResponse response = new ZipCodeValidationResponse(false, "99999");

        when(validateZipCodeUseCase.validate("99999")).thenReturn(domainResult);
        when(zipCodeRestMapper.toValidationResponse(domainResult)).thenReturn(response);

        // WHEN
        ResponseEntity<ZipCodeValidationResponse> result = controller.validate(request);

        // THEN
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().valid()).isFalse();
        assertThat(result.getBody().zipCode()).isEqualTo("99999");
    }
}
