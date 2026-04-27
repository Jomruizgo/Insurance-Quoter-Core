package com.sofka.insurancequoter.core.shared.infrastructure.config;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures AOP support for @Observed annotations on use case implementations.
 * The ObservedAspect intercepts methods on Spring-managed beans annotated with @Observed
 * and creates a Micrometer observation (timer + trace span) for each invocation.
 *
 * Use cases are registered as @Bean in their respective *Config classes, so Spring
 * creates a proxy that allows the aspect to intercept calls correctly.
 */
@Configuration
public class ObservabilityConfig {

    @Bean
    public ObservedAspect observedAspect(ObservationRegistry registry) {
        return new ObservedAspect(registry);
    }
}
