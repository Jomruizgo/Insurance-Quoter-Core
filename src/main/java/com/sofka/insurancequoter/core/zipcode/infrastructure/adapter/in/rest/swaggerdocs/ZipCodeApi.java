package com.sofka.insurancequoter.core.zipcode.infrastructure.adapter.in.rest.swaggerdocs;

import com.sofka.insurancequoter.core.zipcode.infrastructure.adapter.in.rest.dto.ValidateZipCodeRequest;
import com.sofka.insurancequoter.core.zipcode.infrastructure.adapter.in.rest.dto.ZipCodeResponse;
import com.sofka.insurancequoter.core.zipcode.infrastructure.adapter.in.rest.dto.ZipCodeValidationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Swagger / OpenAPI contract for the Zip Codes resource.
 * All @Tag, @Operation and @ApiResponse annotations live here — never in the controller.
 */
@Tag(name = "Zip Codes", description = "Consulta y validación de códigos postales")
@RequestMapping("/v1/zip-codes")
public interface ZipCodeApi {

    @Operation(summary = "Obtener datos completos de un código postal")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Código postal encontrado"),
            @ApiResponse(responseCode = "404", description = "Código postal no encontrado"),
            @ApiResponse(responseCode = "400", description = "Formato de código postal inválido")
    })
    @GetMapping("/{zipCode}")
    ResponseEntity<ZipCodeResponse> getZipCode(@PathVariable String zipCode);

    @Operation(summary = "Validar existencia de un código postal")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resultado de validación"),
            @ApiResponse(responseCode = "400", description = "Request inválido — zipCode requerido")
    })
    @PostMapping("/validate")
    ResponseEntity<ZipCodeValidationResponse> validate(@Valid @RequestBody ValidateZipCodeRequest request);
}
