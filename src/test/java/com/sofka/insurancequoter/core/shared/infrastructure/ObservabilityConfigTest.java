package com.sofka.insurancequoter.core.shared.infrastructure;

import io.micrometer.observation.aop.ObservedAspect;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test — T-030.
 * Verifies that ObservabilityConfig registers ObservedAspect in the Spring context,
 * which is required for @Observed annotations to generate trace spans.
 */
@SpringBootTest
@Testcontainers
class ObservabilityConfigTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        // Disable OTLP export so missing Jaeger does not break context startup
        registry.add("management.otlp.tracing.endpoint", () -> "http://localhost:14317");
    }

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void observedAspect_beanExistsInContext() {
        // GIVEN / WHEN — context is already started by @SpringBootTest

        // THEN
        assertThat(applicationContext.getBean(ObservedAspect.class)).isNotNull();
    }

    @Test
    void observedAspect_isSingletonInContext() {
        // WHEN
        ObservedAspect bean1 = applicationContext.getBean(ObservedAspect.class);
        ObservedAspect bean2 = applicationContext.getBean(ObservedAspect.class);

        // THEN — Spring default scope is singleton
        assertThat(bean1).isSameAs(bean2);
    }
}
