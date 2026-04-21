package com.sofka.insurancequoter.core.riskclassification.infrastructure.adapter.in.rest.mapper;

import com.sofka.insurancequoter.core.riskclassification.domain.model.RiskClassification;
import com.sofka.insurancequoter.core.riskclassification.infrastructure.adapter.in.rest.dto.RiskClassificationsResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RiskClassificationRestMapperTest {

    private final RiskClassificationRestMapper mapper = new RiskClassificationRestMapper();

    @Test
    void toResponse_mapsAllFieldsCorrectly() {
        // GIVEN
        List<RiskClassification> classifications = List.of(
                new RiskClassification("STANDARD", "Riesgo estándar"),
                new RiskClassification("PREFERRED", "Riesgo preferente"),
                new RiskClassification("SUBSTANDARD", "Riesgo subestándar")
        );

        // WHEN
        RiskClassificationsResponse response = mapper.toResponse(classifications);

        // THEN
        assertThat(response.riskClassifications()).hasSize(3);
        assertThat(response.riskClassifications().get(0).code()).isEqualTo("STANDARD");
        assertThat(response.riskClassifications().get(0).description()).isEqualTo("Riesgo estándar");
        assertThat(response.riskClassifications().get(1).code()).isEqualTo("PREFERRED");
        assertThat(response.riskClassifications().get(2).code()).isEqualTo("SUBSTANDARD");
        assertThat(response.riskClassifications().get(2).description()).isEqualTo("Riesgo subestándar");
    }

    @Test
    void toResponse_returnsEmptyListWhenClassificationsIsEmpty() {
        // GIVEN / WHEN
        RiskClassificationsResponse response = mapper.toResponse(List.of());

        // THEN
        assertThat(response.riskClassifications()).isEmpty();
    }
}
