package com.sofka.insurancequoter.core.subscriber.infrastructure.adapter.in.rest.swaggerdocs;

import com.sofka.insurancequoter.core.subscriber.infrastructure.adapter.in.rest.dto.SubscribersResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Subscribers", description = "Catálogo de suscriptores disponibles")
@RequestMapping("/v1/subscribers")
public interface SubscriberApi {

    @Operation(summary = "Obtener catálogo de suscriptores", description = "Returns the full list of available subscribers loaded from static fixtures.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Subscriber catalog returned successfully")
    })
    @GetMapping
    ResponseEntity<SubscribersResponse> getSubscribers();
}
