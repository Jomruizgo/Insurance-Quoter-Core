package com.sofka.insurancequoter.core.subscriber.infrastructure;

import com.sofka.insurancequoter.core.subscriber.domain.model.Subscriber;
import com.sofka.insurancequoter.core.subscriber.domain.port.in.GetSubscribersUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class SubscriberCatalogIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configurePostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    }

    @Autowired
    private GetSubscribersUseCase getSubscribersUseCase;

    @Test
    void getAll_returnsSubscribersFromFixture() {
        List<Subscriber> subscribers = getSubscribersUseCase.getAll();

        assertThat(subscribers).hasSize(2);
        assertThat(subscribers).anyMatch(s -> s.id().equals("SUB-001") && s.name().equals("Seguros Sofka"));
        assertThat(subscribers).anyMatch(s -> s.id().equals("SUB-002") && s.name().equals("Aseguradora Norte"));
    }

    @Test
    void getAll_returnedListIsImmutable() {
        List<Subscriber> subscribers = getSubscribersUseCase.getAll();

        assertThat(subscribers).isNotEmpty();
        assertThat(subscribers.get(0).id()).isNotBlank();
        assertThat(subscribers.get(0).name()).isNotBlank();
    }
}
