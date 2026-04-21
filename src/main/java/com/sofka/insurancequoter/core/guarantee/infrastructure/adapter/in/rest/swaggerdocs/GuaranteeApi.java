package com.sofka.insurancequoter.core.guarantee.infrastructure.adapter.in.rest.swaggerdocs;

import com.sofka.insurancequoter.core.guarantee.infrastructure.adapter.in.rest.dto.GuaranteesResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Guarantees", description = "Catálogo de garantías disponibles para cotización")
@RequestMapping("/v1/catalogs/guarantees")
public interface GuaranteeApi {

    @Operation(
            summary = "Obtener catálogo de garantías",
            description = "Returns the full list of guarantees loaded from static fixtures, including the tarifable flag."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Guarantees catalog returned successfully")
    })
    @GetMapping
    ResponseEntity<GuaranteesResponse> getGuarantees();
}
