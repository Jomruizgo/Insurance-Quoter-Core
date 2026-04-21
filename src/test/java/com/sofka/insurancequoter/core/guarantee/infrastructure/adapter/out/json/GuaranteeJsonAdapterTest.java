package com.sofka.insurancequoter.core.guarantee.infrastructure.adapter.out.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sofka.insurancequoter.core.guarantee.domain.model.Guarantee;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GuaranteeJsonAdapterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void findAll_returnsGuaranteesFromJson() {
        // GIVEN
        String json = "[{\"code\":\"GUA-FIRE\",\"description\":\"Incendio edificios\",\"tarifable\":true},"
                + "{\"code\":\"GUA-THEFT\",\"description\":\"Robo\",\"tarifable\":true},"
                + "{\"code\":\"GUA-GLASS\",\"description\":\"Vidrios\",\"tarifable\":true}]";
        Resource resource = new ByteArrayResource(json.getBytes());

        // WHEN
        GuaranteeJsonAdapter adapter = new GuaranteeJsonAdapter(objectMapper, resource);
        List<Guarantee> result = adapter.findAll();

        // THEN
        assertThat(result).hasSize(3);
        assertThat(result.get(0).code()).isEqualTo("GUA-FIRE");
        assertThat(result.get(0).description()).isEqualTo("Incendio edificios");
        assertThat(result.get(0).tarifable()).isTrue();
        assertThat(result.get(1).code()).isEqualTo("GUA-THEFT");
        assertThat(result.get(2).code()).isEqualTo("GUA-GLASS");
    }

    @Test
    void findAll_returnsEmptyListWhenJsonIsEmpty() {
        // GIVEN
        Resource resource = new ByteArrayResource("[]".getBytes());

        // WHEN
        GuaranteeJsonAdapter adapter = new GuaranteeJsonAdapter(objectMapper, resource);
        List<Guarantee> result = adapter.findAll();

        // THEN
        assertThat(result).isEmpty();
    }

    @Test
    void findAll_returnsImmutableList() {
        // GIVEN
        String json = "[{\"code\":\"GUA-FIRE\",\"description\":\"Incendio edificios\",\"tarifable\":true}]";
        GuaranteeJsonAdapter adapter = new GuaranteeJsonAdapter(objectMapper, new ByteArrayResource(json.getBytes()));

        // WHEN
        List<Guarantee> result = adapter.findAll();

        // THEN
        assertThatThrownBy(() -> result.add(new Guarantee("GUA-999", "Test", false)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void findAllTarifable_returnsOnlyTarifableGuarantees() {
        // GIVEN
        String json = "[{\"code\":\"GUA-FIRE\",\"description\":\"Incendio edificios\",\"tarifable\":true},"
                + "{\"code\":\"GUA-NON\",\"description\":\"No tarifable\",\"tarifable\":false},"
                + "{\"code\":\"GUA-THEFT\",\"description\":\"Robo\",\"tarifable\":true}]";
        Resource resource = new ByteArrayResource(json.getBytes());
        GuaranteeJsonAdapter adapter = new GuaranteeJsonAdapter(objectMapper, resource);

        // WHEN
        List<Guarantee> result = adapter.findAllTarifable();

        // THEN
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(Guarantee::tarifable);
        assertThat(result.get(0).code()).isEqualTo("GUA-FIRE");
        assertThat(result.get(1).code()).isEqualTo("GUA-THEFT");
    }

    @Test
    void findAllTarifable_returnsEmptyWhenAllNonTarifable() {
        // GIVEN
        String json = "[{\"code\":\"GUA-NON\",\"description\":\"No tarifable\",\"tarifable\":false}]";
        GuaranteeJsonAdapter adapter = new GuaranteeJsonAdapter(objectMapper, new ByteArrayResource(json.getBytes()));

        // WHEN
        List<Guarantee> result = adapter.findAllTarifable();

        // THEN
        assertThat(result).isEmpty();
    }

    @Test
    void constructor_throwsWhenJsonIsMalformed() {
        // GIVEN
        Resource badResource = new ByteArrayResource("not-valid-json".getBytes());

        // THEN
        assertThatThrownBy(() -> new GuaranteeJsonAdapter(objectMapper, badResource))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to load guarantees");
    }

    @Test
    void constructor_throwsWhenFileNotFound() throws IOException {
        // GIVEN — resource that throws IOException on getInputStream (simulates missing file)
        Resource missingResource = mock(Resource.class);
        when(missingResource.getInputStream()).thenThrow(new IOException("File not found"));
        when(missingResource.getDescription()).thenReturn("fixtures/guarantees.json");

        // THEN
        assertThatThrownBy(() -> new GuaranteeJsonAdapter(objectMapper, missingResource))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to load guarantees");
    }
}
