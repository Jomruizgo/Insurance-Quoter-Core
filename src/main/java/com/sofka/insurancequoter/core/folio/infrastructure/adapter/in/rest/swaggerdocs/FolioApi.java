package com.sofka.insurancequoter.core.folio.infrastructure.adapter.in.rest.swaggerdocs;

import com.sofka.insurancequoter.core.folio.infrastructure.adapter.in.rest.dto.FolioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Swagger/OpenAPI contract for the Folios resource.
 * All Swagger annotations are declared here; FolioController stays clean.
 */
@Tag(name = "Folios", description = "Generación de folios de cotización")
@RequestMapping("/v1/folios")
public interface FolioApi {

    @Operation(summary = "Generar número de folio", description = "Generates the next unique folio number using a PostgreSQL sequence.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Folio generated successfully"),
            @ApiResponse(responseCode = "503", description = "Database unavailable")
    })
    @GetMapping
    ResponseEntity<FolioResponse> getFolio();
}
