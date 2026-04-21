package com.sofka.insurancequoter.core.subscriber.infrastructure.adapter.out.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sofka.insurancequoter.core.subscriber.domain.model.Subscriber;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubscriberJsonAdapterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void findAll_returnsSubscribersFromJson() {
        // GIVEN
        String json = "[{\"id\":\"SUB-001\",\"name\":\"Seguros Sofka\"},{\"id\":\"SUB-002\",\"name\":\"Aseguradora Norte\"}]";
        Resource resource = new ByteArrayResource(json.getBytes());

        // WHEN
        SubscriberJsonAdapter adapter = new SubscriberJsonAdapter(objectMapper, resource);
        List<Subscriber> result = adapter.findAll();

        // THEN
        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo("SUB-001");
        assertThat(result.get(0).name()).isEqualTo("Seguros Sofka");
        assertThat(result.get(1).id()).isEqualTo("SUB-002");
        assertThat(result.get(1).name()).isEqualTo("Aseguradora Norte");
    }

    @Test
    void findAll_returnsEmptyListWhenJsonIsEmpty() {
        // GIVEN
        Resource resource = new ByteArrayResource("[]".getBytes());

        // WHEN
        SubscriberJsonAdapter adapter = new SubscriberJsonAdapter(objectMapper, resource);
        List<Subscriber> result = adapter.findAll();

        // THEN
        assertThat(result).isEmpty();
    }

    @Test
    void constructor_throwsWhenResourceIsUnreadable() {
        // GIVEN
        Resource badResource = new ByteArrayResource("not-valid-json".getBytes());

        // THEN
        assertThatThrownBy(() -> new SubscriberJsonAdapter(objectMapper, badResource))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to load subscribers");
    }
}
