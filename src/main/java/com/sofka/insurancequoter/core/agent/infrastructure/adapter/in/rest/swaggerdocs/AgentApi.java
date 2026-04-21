package com.sofka.insurancequoter.core.agent.infrastructure.adapter.in.rest.swaggerdocs;

import com.sofka.insurancequoter.core.agent.infrastructure.adapter.in.rest.dto.AgentsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Agents", description = "Catálogo de agentes disponibles")
@RequestMapping("/v1/agents")
public interface AgentApi {

    @Operation(summary = "Obtener catálogo de agentes", description = "Returns the full list of available agents loaded from static fixtures.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agent catalog returned successfully")
    })
    @GetMapping
    ResponseEntity<AgentsResponse> getAgents();
}
