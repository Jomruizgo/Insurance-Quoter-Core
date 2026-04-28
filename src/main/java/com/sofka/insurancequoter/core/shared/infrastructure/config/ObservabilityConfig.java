package com.sofka.insurancequoter.core.shared.infrastructure.config;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import io.micrometer.tracing.handler.DefaultTracingObservationHandler;
import io.micrometer.tracing.otel.bridge.OtelBaggageManager;
import io.micrometer.tracing.otel.bridge.OtelCurrentTraceContext;
import io.micrometer.tracing.otel.bridge.OtelTracer;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import org.slf4j.MDC;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

/**
 * Wires the full OTel SDK + micrometer-tracing bridge manually.
 *
 * Spring Boot 4 removed all tracing auto-configuration. This class replicates the
 * beans that OtelAutoConfiguration provided in Spring Boot 3:
 *   - OtelCurrentTraceContext / OtelBaggageManager / OtelTracer (bridge classes)
 *   - DefaultTracingObservationHandler registered with ObservationRegistry
 *   - MdcObservationHandler to populate traceId/spanId into SLF4J MDC
 *
 * W3C traceparent propagation (continuing Back's trace) is handled by
 * W3CTracePropagationFilter which extracts the header before any span is created.
 */
@Configuration
public class ObservabilityConfig {

    private static final AttributeKey<String> SERVICE_NAME_KEY = AttributeKey.stringKey("service.name");

    @Bean
    public ObservedAspect observedAspect(ObservationRegistry registry) {
        return new ObservedAspect(registry);
    }

    @Bean
    public OtlpHttpSpanExporter otlpHttpSpanExporter(
            @Value("${management.otlp.tracing.endpoint:http://localhost:4318/v1/traces}") String endpoint) {
        return OtlpHttpSpanExporter.builder()
                .setEndpoint(endpoint)
                .build();
    }

    @Bean
    public OpenTelemetry openTelemetry(
            OtlpHttpSpanExporter spanExporter,
            @Value("${spring.application.name:insurance-quoter-core}") String serviceName) {

        Resource resource = Resource.getDefault().merge(
                Resource.create(Attributes.of(SERVICE_NAME_KEY, serviceName)));

        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
                .setResource(resource)
                .build();

        return OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .build();
    }

    @Bean
    public OtelCurrentTraceContext otelCurrentTraceContext() {
        return new OtelCurrentTraceContext();
    }

    @Bean
    public OtelBaggageManager otelBaggageManager(OtelCurrentTraceContext currentTraceContext) {
        return new OtelBaggageManager(currentTraceContext, Collections.emptyList(), Collections.emptyList());
    }

    @Bean
    public OtelTracer otelTracer(OpenTelemetry openTelemetry,
                                  OtelCurrentTraceContext currentTraceContext,
                                  OtelBaggageManager baggageManager) {
        io.opentelemetry.api.trace.Tracer nativeTracer =
                openTelemetry.getTracer("insurance-quoter-core");
        return new OtelTracer(nativeTracer, currentTraceContext, event -> {}, baggageManager);
    }

    @Bean
    public DefaultTracingObservationHandler tracingObservationHandler(OtelTracer tracer) {
        return new DefaultTracingObservationHandler(tracer);
    }

    /**
     * Registers the tracing handler (creates spans) and the MDC handler (populates
     * traceId/spanId into SLF4J MDC) after all Spring singletons are initialized.
     * Tracing handler must come first so the OTel span is current when MDC handler runs.
     */
    @Bean
    public SmartInitializingSingleton tracingHandlerRegistrar(
            ObservationRegistry registry,
            DefaultTracingObservationHandler tracingHandler) {
        return () -> registry.observationConfig()
                .observationHandler(tracingHandler)
                .observationHandler(new MdcObservationHandler());
    }

    /** Populates traceId/spanId into SLF4J MDC using the active OTel span. */
    static final class MdcObservationHandler implements ObservationHandler<Observation.Context> {

        private static final String TRACE_ID = "traceId";
        private static final String SPAN_ID = "spanId";

        @Override
        public void onScopeOpened(Observation.Context context) {
            io.opentelemetry.api.trace.Span current = io.opentelemetry.api.trace.Span.current();
            if (current.getSpanContext().isValid()) {
                MDC.put(TRACE_ID, current.getSpanContext().getTraceId());
                MDC.put(SPAN_ID, current.getSpanContext().getSpanId());
            }
        }

        @Override
        public void onScopeClosed(Observation.Context context) {
            MDC.remove(TRACE_ID);
            MDC.remove(SPAN_ID);
        }

        @Override
        public void onStart(Observation.Context context) {}

        @Override
        public void onError(Observation.Context context) {}

        @Override
        public void onEvent(Observation.Event event, Observation.Context context) {}

        @Override
        public void onStop(Observation.Context context) {}

        @Override
        public boolean supportsContext(Observation.Context context) {
            return true;
        }
    }
}
