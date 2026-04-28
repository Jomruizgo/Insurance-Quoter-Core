package com.sofka.insurancequoter.core.shared.infrastructure.rest;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Extracts the W3C traceparent header from every incoming HTTP request and makes
 * the extracted OTel context current for the duration of the request.
 *
 * This ensures that any span created by @Observed use cases is a child of the
 * parent span sent by Insurance-Quoter-Back, rather than a new root trace.
 * Must run before any span-creating code (e.g., before the DispatcherServlet).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class W3CTracePropagationFilter extends OncePerRequestFilter {

    private static final TextMapGetter<HttpServletRequest> GETTER = new TextMapGetter<>() {
        @Override
        public Iterable<String> keys(HttpServletRequest carrier) {
            return Collections.list(carrier.getHeaderNames());
        }

        @Override
        @Nullable
        public String get(@Nullable HttpServletRequest carrier, String key) {
            return carrier == null ? null : carrier.getHeader(key);
        }
    };

    private final OpenTelemetry openTelemetry;

    public W3CTracePropagationFilter(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Context parentContext = openTelemetry.getPropagators()
                .getTextMapPropagator()
                .extract(Context.current(), request, GETTER);
        try (io.opentelemetry.context.Scope scope = parentContext.makeCurrent()) {
            filterChain.doFilter(request, response);
        }
    }
}
