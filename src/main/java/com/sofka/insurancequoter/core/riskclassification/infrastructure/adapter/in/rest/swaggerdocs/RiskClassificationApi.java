package com.sofka.insurancequoter.core.riskclassification.infrastructure.adapter.in.rest.swaggerdocs;

import com.sofka.insurancequoter.core.riskclassification.infrastructure.adapter.in.rest.dto.RiskClassificationsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Risk Classifications", description = "Catálogo de clasificaciones de riesgo")
@RequestMapping("/v1/catalogs/risk-classification")
public interface RiskClassificationApi {

    @Operation(summary = "Obtener catálogo de clasificaciones de riesgo",
            description = "Returns the full list of available risk classifications loaded from static fixtures.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Risk classification catalog returned successfully")
    })
    @GetMapping
    ResponseEntity<RiskClassificationsResponse> getRiskClassifications();
}
