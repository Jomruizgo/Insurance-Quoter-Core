package com.sofka.insurancequoter.core.businessline.infrastructure.adapter.out.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sofka.insurancequoter.core.businessline.domain.model.BusinessLine;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BusinessLineJsonAdapterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void findAll_returnsBusinessLinesFromJson() {
        // GIVEN
        String json = "[{\"code\":\"BL-001\",\"description\":\"Bodega de mercanc\\u00edas\",\"fireKey\":\"FK-INC-01\"},"
                + "{\"code\":\"BL-002\",\"description\":\"Oficina administrativa\",\"fireKey\":\"FK-INC-02\"}]";
        Resource resource = new ByteArrayResource(json.getBytes());

        // WHEN
        BusinessLineJsonAdapter adapter = new BusinessLineJsonAdapter(objectMapper, resource);
        List<BusinessLine> result = adapter.findAll();

        // THEN
        assertThat(result).hasSize(2);
        assertThat(result.get(0).code()).isEqualTo("BL-001");
        assertThat(result.get(0).description()).isEqualTo("Bodega de mercancías");
        assertThat(result.get(0).fireKey()).isEqualTo("FK-INC-01");
        assertThat(result.get(1).code()).isEqualTo("BL-002");
        assertThat(result.get(1).description()).isEqualTo("Oficina administrativa");
        assertThat(result.get(1).fireKey()).isEqualTo("FK-INC-02");
    }

    @Test
    void findAll_returnsEmptyListWhenJsonIsEmpty() {
        // GIVEN
        Resource resource = new ByteArrayResource("[]".getBytes());

        // WHEN
        BusinessLineJsonAdapter adapter = new BusinessLineJsonAdapter(objectMapper, resource);
        List<BusinessLine> result = adapter.findAll();

        // THEN
        assertThat(result).isEmpty();
    }

    @Test
    void findAll_returnsImmutableList() {
        // GIVEN
        String json = "[{\"code\":\"BL-001\",\"description\":\"Bodega de mercanc\\u00edas\",\"fireKey\":\"FK-INC-01\"}]";
        BusinessLineJsonAdapter adapter = new BusinessLineJsonAdapter(objectMapper, new ByteArrayResource(json.getBytes()));

        // WHEN
        List<BusinessLine> result = adapter.findAll();

        // THEN
        assertThatThrownBy(() -> result.add(new BusinessLine("BL-999", "Test", "FK-999")))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void constructor_throwsWhenResourceIsUnreadable() {
        // GIVEN
        Resource badResource = new ByteArrayResource("not-valid-json".getBytes());

        // THEN
        assertThatThrownBy(() -> new BusinessLineJsonAdapter(objectMapper, badResource))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to load business lines");
    }
}
