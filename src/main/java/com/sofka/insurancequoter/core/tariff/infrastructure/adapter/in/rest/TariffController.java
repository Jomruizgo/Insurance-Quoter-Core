package com.sofka.insurancequoter.core.tariff.infrastructure.adapter.in.rest;

import com.sofka.insurancequoter.core.tariff.domain.exception.TariffNotFoundException;
import com.sofka.insurancequoter.core.tariff.domain.port.in.GetTariffsUseCase;
import com.sofka.insurancequoter.core.tariff.domain.port.in.UpdateTariffsUseCase;
import com.sofka.insurancequoter.core.tariff.infrastructure.adapter.in.rest.dto.TariffsResponse;
import com.sofka.insurancequoter.core.tariff.infrastructure.adapter.in.rest.dto.UpdateTariffsRequest;
import com.sofka.insurancequoter.core.tariff.infrastructure.adapter.in.rest.mapper.TariffRestMapper;
import com.sofka.insurancequoter.core.tariff.infrastructure.adapter.in.rest.swaggerdocs.TariffApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller for the tariffs resource.
 * Parses HTTP, delegates to use cases, maps results.
 * No business logic — no Swagger annotations (those live in TariffApi).
 */
@RestController
@RequiredArgsConstructor
public class TariffController implements TariffApi {

    private final GetTariffsUseCase getTariffsUseCase;
    private final UpdateTariffsUseCase updateTariffsUseCase;
    private final TariffRestMapper mapper;

    @Override
    public ResponseEntity<TariffsResponse> getTariffs() {
        return ResponseEntity.ok(mapper.toResponse(getTariffsUseCase.getCurrent()));
    }

    @Override
    public ResponseEntity<TariffsResponse> updateTariffs(UpdateTariffsRequest request) {
        return ResponseEntity.ok(mapper.toResponse(updateTariffsUseCase.update(mapper.toDomain(request))));
    }

    @ExceptionHandler(TariffNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Tariffs not found"));
    }
}
