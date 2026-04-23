package com.sofka.insurancequoter.core.businessline.infrastructure.adapter.in.rest.mapper;

import com.sofka.insurancequoter.core.businessline.domain.model.BusinessLine;
import com.sofka.insurancequoter.core.businessline.infrastructure.adapter.in.rest.dto.BusinessLinesResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessLineRestMapperTest {

    private final BusinessLineRestMapper mapper = new BusinessLineRestMapper();

    @Test
    void toResponse_mapsAllFieldsCorrectly() {
        // GIVEN
        List<BusinessLine> businessLines = List.of(
                new BusinessLine("BL-001", "Bodega de mercancías", "FK-INC-01"),
                new BusinessLine("BL-002", "Oficina administrativa", "FK-INC-02")
        );

        // WHEN
        BusinessLinesResponse response = mapper.toResponse(businessLines);

        // THEN
        assertThat(response.businessLines()).hasSize(2);
        assertThat(response.businessLines().get(0).code()).isEqualTo("BL-001");
        assertThat(response.businessLines().get(0).description()).isEqualTo("Bodega de mercancías");
        assertThat(response.businessLines().get(0).fireKey()).isEqualTo("FK-INC-01");
        assertThat(response.businessLines().get(1).code()).isEqualTo("BL-002");
        assertThat(response.businessLines().get(1).description()).isEqualTo("Oficina administrativa");
        assertThat(response.businessLines().get(1).fireKey()).isEqualTo("FK-INC-02");
    }

    @Test
    void toResponse_returnsEmptyBusinessLinesWhenListIsEmpty() {
        // GIVEN / WHEN
        BusinessLinesResponse response = mapper.toResponse(List.of());

        // THEN
        assertThat(response.businessLines()).isEmpty();
    }
}
