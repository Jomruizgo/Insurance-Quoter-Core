package com.sofka.insurancequoter.core.agent.infrastructure.adapter.out.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sofka.insurancequoter.core.agent.domain.model.Agent;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgentJsonAdapterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void findAll_returnsAgentsFromJson() {
        // GIVEN
        String json = "[{\"code\":\"AGT-123\",\"name\":\"Juan Pérez\",\"subscriberId\":\"SUB-001\"},"
                + "{\"code\":\"AGT-124\",\"name\":\"María López\",\"subscriberId\":\"SUB-001\"}]";
        Resource resource = new ByteArrayResource(json.getBytes());

        // WHEN
        AgentJsonAdapter adapter = new AgentJsonAdapter(objectMapper, resource);
        List<Agent> result = adapter.findAll();

        // THEN
        assertThat(result).hasSize(2);
        assertThat(result.get(0).code()).isEqualTo("AGT-123");
        assertThat(result.get(0).name()).isEqualTo("Juan Pérez");
        assertThat(result.get(0).subscriberId()).isEqualTo("SUB-001");
        assertThat(result.get(1).code()).isEqualTo("AGT-124");
    }

    @Test
    void findAll_returnsEmptyListWhenJsonIsEmpty() {
        // GIVEN
        Resource resource = new ByteArrayResource("[]".getBytes());

        // WHEN
        AgentJsonAdapter adapter = new AgentJsonAdapter(objectMapper, resource);
        List<Agent> result = adapter.findAll();

        // THEN
        assertThat(result).isEmpty();
    }

    @Test
    void existsByCode_returnsTrueWhenCodeExists() {
        // GIVEN
        String json = "[{\"code\":\"AGT-123\",\"name\":\"Juan Pérez\",\"subscriberId\":\"SUB-001\"}]";
        AgentJsonAdapter adapter = new AgentJsonAdapter(objectMapper, new ByteArrayResource(json.getBytes()));

        // WHEN / THEN
        assertThat(adapter.existsByCode("AGT-123")).isTrue();
    }

    @Test
    void existsByCode_returnsFalseWhenCodeDoesNotExist() {
        // GIVEN
        String json = "[{\"code\":\"AGT-123\",\"name\":\"Juan Pérez\",\"subscriberId\":\"SUB-001\"}]";
        AgentJsonAdapter adapter = new AgentJsonAdapter(objectMapper, new ByteArrayResource(json.getBytes()));

        // WHEN / THEN
        assertThat(adapter.existsByCode("AGT-999")).isFalse();
    }

    @Test
    void existsByCode_returnsFalseWhenCatalogIsEmpty() {
        // GIVEN
        AgentJsonAdapter adapter = new AgentJsonAdapter(objectMapper, new ByteArrayResource("[]".getBytes()));

        // WHEN / THEN
        assertThat(adapter.existsByCode("AGT-123")).isFalse();
    }

    @Test
    void constructor_throwsWhenResourceIsUnreadable() {
        // GIVEN
        Resource badResource = new ByteArrayResource("not-valid-json".getBytes());

        // THEN
        assertThatThrownBy(() -> new AgentJsonAdapter(objectMapper, badResource))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to load agents");
    }
}
