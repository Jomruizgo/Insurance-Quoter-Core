package com.sofka.insurancequoter.core.subscriber.infrastructure.adapter.in.rest.mapper;

import com.sofka.insurancequoter.core.subscriber.domain.model.Subscriber;
import com.sofka.insurancequoter.core.subscriber.infrastructure.adapter.in.rest.dto.SubscribersResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SubscriberRestMapperTest {

    private final SubscriberRestMapper mapper = new SubscriberRestMapper();

    @Test
    void toResponse_mapsSubscribersToDto() {
        // GIVEN
        List<Subscriber> subscribers = List.of(
                new Subscriber("SUB-001", "Seguros Sofka"),
                new Subscriber("SUB-002", "Aseguradora Norte")
        );

        // WHEN
        SubscribersResponse response = mapper.toResponse(subscribers);

        // THEN
        assertThat(response.subscribers()).hasSize(2);
        assertThat(response.subscribers().get(0).id()).isEqualTo("SUB-001");
        assertThat(response.subscribers().get(0).name()).isEqualTo("Seguros Sofka");
        assertThat(response.subscribers().get(1).id()).isEqualTo("SUB-002");
        assertThat(response.subscribers().get(1).name()).isEqualTo("Aseguradora Norte");
    }

    @Test
    void toResponse_returnsEmptyListWhenNoSubscribers() {
        // WHEN
        SubscribersResponse response = mapper.toResponse(List.of());

        // THEN
        assertThat(response.subscribers()).isEmpty();
    }
}
