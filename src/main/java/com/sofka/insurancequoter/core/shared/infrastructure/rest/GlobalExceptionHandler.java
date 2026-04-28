package com.sofka.insurancequoter.core.shared.infrastructure.rest;

import com.sofka.insurancequoter.core.shared.infrastructure.metrics.CatalogErrorMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;

/**
 * Global fallback exception handler for all unhandled exceptions.
 * Extends ResponseEntityExceptionHandler so Spring's built-in handlers for
 * validation exceptions (MethodArgumentNotValidException → 400, etc.) take
 * precedence over the generic Exception handler below.
 *
 * Records catalog_errors_total{errorType} for every truly unhandled exception
 * so unexpected failure patterns become visible in Prometheus/Grafana.
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

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
