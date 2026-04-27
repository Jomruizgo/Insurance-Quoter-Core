package com.sofka.insurancequoter.core.shared.infrastructure.rest;

import com.sofka.insurancequoter.core.shared.infrastructure.metrics.CatalogErrorMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Global fallback exception handler for all unhandled exceptions.
 * Controllers may define their own @ExceptionHandler for domain-specific exceptions
 * (e.g. TariffController handles TariffNotFoundException locally). This handler
 * only activates when no local handler matches — it acts as a safety net.
 *
 * Records catalog_errors_total{errorType} for every unhandled exception so that
 * unexpected failure patterns become visible in Prometheus/Grafana dashboards.
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final CatalogErrorMetrics catalogErrorMetrics;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericError(Exception ex) {
        catalogErrorMetrics.recordError(ex.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error", "Internal server error",
                        "code", "CATALOG_ERROR"
                ));
    }
}
