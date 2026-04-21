package com.sofka.insurancequoter.core.zipcode.infrastructure.adapter.in.rest;

import com.sofka.insurancequoter.core.zipcode.domain.exception.ZipCodeNotFoundException;
import com.sofka.insurancequoter.core.zipcode.domain.port.in.GetZipCodeUseCase;
import com.sofka.insurancequoter.core.zipcode.domain.port.in.ValidateZipCodeUseCase;
import com.sofka.insurancequoter.core.zipcode.infrastructure.adapter.in.rest.dto.ValidateZipCodeRequest;
import com.sofka.insurancequoter.core.zipcode.infrastructure.adapter.in.rest.dto.ZipCodeResponse;
import com.sofka.insurancequoter.core.zipcode.infrastructure.adapter.in.rest.dto.ZipCodeValidationResponse;
import com.sofka.insurancequoter.core.zipcode.infrastructure.adapter.in.rest.mapper.ZipCodeRestMapper;
import com.sofka.insurancequoter.core.zipcode.infrastructure.adapter.in.rest.swaggerdocs.ZipCodeApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller for the Zip Codes resource.
 * Delegates to use cases; no business logic here.
 * Swagger annotations live in ZipCodeApi.
 */
@RestController
@RequiredArgsConstructor
public class ZipCodeController implements ZipCodeApi {

    private final GetZipCodeUseCase getZipCodeUseCase;
    private final ValidateZipCodeUseCase validateZipCodeUseCase;
    private final ZipCodeRestMapper zipCodeRestMapper;

    @Override
    public ResponseEntity<ZipCodeResponse> getZipCode(String zipCode) {
        ZipCodeResponse response = zipCodeRestMapper.toResponse(getZipCodeUseCase.getByZipCode(zipCode));
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ZipCodeValidationResponse> validate(ValidateZipCodeRequest request) {
        ZipCodeValidationResponse response = zipCodeRestMapper.toValidationResponse(
                validateZipCodeUseCase.validate(request.zipCode())
        );
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(ZipCodeNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleZipCodeNotFound(ZipCodeNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", "Zip code not found",
                        "code", "ZIP_CODE_NOT_FOUND"
                ));
    }
}
