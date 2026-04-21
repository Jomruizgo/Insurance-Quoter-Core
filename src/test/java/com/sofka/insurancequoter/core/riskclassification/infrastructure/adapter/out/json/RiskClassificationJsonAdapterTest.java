package com.sofka.insurancequoter.core.riskclassification.infrastructure.adapter.out.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sofka.insurancequoter.core.riskclassification.domain.model.RiskClassification;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RiskClassificationJsonAdapterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void findAll_returnsClassificationsFromJson() {
        // GIVEN
        String json = "[{\"code\":\"STANDARD\",\"description\":\"Riesgo estándar\"},"
                + "{\"code\":\"PREFERRED\",\"description\":\"Riesgo preferente\"}]";
        Resource resource = new ByteArrayResource(json.getBytes());

        // WHEN
        RiskClassificationJsonAdapter adapter = new RiskClassificationJsonAdapter(objectMapper, resource);
        List<RiskClassification> result = adapter.findAll();

        // THEN
        assertThat(result).hasSize(2);
        assertThat(result.get(0).code()).isEqualTo("STANDARD");
        assertThat(result.get(0).description()).isEqualTo("Riesgo estándar");
        assertThat(result.get(1).code()).isEqualTo("PREFERRED");
        assertThat(result.get(1).description()).isEqualTo("Riesgo preferente");
    }

    @Test
    void findAll_returnsEmptyListWhenJsonIsEmpty() {
        // GIVEN
        Resource resource = new ByteArrayResource("[]".getBytes());

        // WHEN
        RiskClassificationJsonAdapter adapter = new RiskClassificationJsonAdapter(objectMapper, resource);
        List<RiskClassification> result = adapter.findAll();

        // THEN
        assertThat(result).isEmpty();
    }

    @Test
    void findAll_returnsImmutableList() {
        // GIVEN
        String json = "[{\"code\":\"STANDARD\",\"description\":\"Riesgo estándar\"}]";
        RiskClassificationJsonAdapter adapter = new RiskClassificationJsonAdapter(
                objectMapper, new ByteArrayResource(json.getBytes()));

        // WHEN
        List<RiskClassification> result = adapter.findAll();

        // THEN
        assertThat(result).hasSize(1);
        assertThatThrownBy(() -> result.add(new RiskClassification("X", "Y")))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void constructor_throwsWhenResourceIsUnreadable() {
        // GIVEN
        Resource badResource = new ByteArrayResource("not-valid-json".getBytes());

        // THEN
        assertThatThrownBy(() -> new RiskClassificationJsonAdapter(objectMapper, badResource))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to load risk classifications");
    }
}
