package com.sofka.insurancequoter.core.guarantee.infrastructure.adapter.in.rest.mapper;

import com.sofka.insurancequoter.core.guarantee.domain.model.Guarantee;
import com.sofka.insurancequoter.core.guarantee.infrastructure.adapter.in.rest.dto.GuaranteesResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GuaranteeRestMapperTest {

    private final GuaranteeRestMapper mapper = new GuaranteeRestMapper();

    @Test
    void toResponse_mapsAllFieldsCorrectly() {
        // GIVEN
        List<Guarantee> guarantees = List.of(
                new Guarantee("GUA-FIRE", "Incendio edificios", true),
                new Guarantee("GUA-THEFT", "Robo", true),
                new Guarantee("GUA-GLASS", "Vidrios", true)
        );

        // WHEN
        GuaranteesResponse response = mapper.toResponse(guarantees);

        // THEN
        assertThat(response.guarantees()).hasSize(3);
        assertThat(response.guarantees().get(0).code()).isEqualTo("GUA-FIRE");
        assertThat(response.guarantees().get(0).description()).isEqualTo("Incendio edificios");
        assertThat(response.guarantees().get(0).tarifable()).isTrue();
        assertThat(response.guarantees().get(1).code()).isEqualTo("GUA-THEFT");
        assertThat(response.guarantees().get(2).code()).isEqualTo("GUA-GLASS");
    }

    @Test
    void toResponse_mapsTarifableFalseCorrectly() {
        // GIVEN
        List<Guarantee> guarantees = List.of(
                new Guarantee("GUA-NON", "No tarifable", false)
        );

        // WHEN
        GuaranteesResponse response = mapper.toResponse(guarantees);

        // THEN
        assertThat(response.guarantees()).hasSize(1);
        assertThat(response.guarantees().get(0).tarifable()).isFalse();
    }

    @Test
    void toResponse_returnsEmptyGuaranteesWhenListIsEmpty() {
        // GIVEN / WHEN
        GuaranteesResponse response = mapper.toResponse(List.of());

        // THEN
        assertThat(response.guarantees()).isEmpty();
    }
}
