package com.sofka.insurancequoter.core.tariff.infrastructure.adapter.in.rest.swaggerdocs;

import com.sofka.insurancequoter.core.tariff.infrastructure.adapter.in.rest.dto.TariffsResponse;
import com.sofka.insurancequoter.core.tariff.infrastructure.adapter.in.rest.dto.UpdateTariffsRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Swagger/OpenAPI contract for the tariffs endpoints.
 * All @Tag, @Operation and @ApiResponse annotations live here — never in TariffController.
 */
@Tag(name = "Tariffs", description = "Consulta y actualización de tarifas del catálogo")
@RequestMapping("/v1/tariffs")
public interface TariffApi {

    @Operation(summary = "Obtener tarifas vigentes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarifas encontradas"),
            @ApiResponse(responseCode = "404", description = "No existe fila de tarifas")
    })
    @GetMapping
    ResponseEntity<TariffsResponse> getTariffs();

    @Operation(summary = "Actualizar tarifas vigentes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarifas actualizadas"),
            @ApiResponse(responseCode = "400", description = "Algún campo es ≤ 0"),
            @ApiResponse(responseCode = "404", description = "No existe fila de tarifas")
    })
    @PutMapping
    ResponseEntity<TariffsResponse> updateTariffs(@Valid @RequestBody UpdateTariffsRequest request);
}
