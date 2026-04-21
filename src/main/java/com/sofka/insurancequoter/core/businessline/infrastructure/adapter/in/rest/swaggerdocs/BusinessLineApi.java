package com.sofka.insurancequoter.core.businessline.infrastructure.adapter.in.rest.swaggerdocs;

import com.sofka.insurancequoter.core.businessline.infrastructure.adapter.in.rest.dto.BusinessLinesResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Business Lines", description = "Catálogo de giros de negocio")
@RequestMapping("/v1/business-lines")
public interface BusinessLineApi {

    @Operation(summary = "Obtener catálogo de giros de negocio", description = "Returns the full list of business lines loaded from static fixtures.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Business lines catalog returned successfully")
    })
    @GetMapping
    ResponseEntity<BusinessLinesResponse> getBusinessLines();
}
