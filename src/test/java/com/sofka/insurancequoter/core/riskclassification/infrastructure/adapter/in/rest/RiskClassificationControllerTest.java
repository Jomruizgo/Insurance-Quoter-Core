package com.sofka.insurancequoter.core.riskclassification.infrastructure.adapter.in.rest;

import com.sofka.insurancequoter.core.riskclassification.domain.model.RiskClassification;
import com.sofka.insurancequoter.core.riskclassification.domain.port.in.GetRiskClassificationsUseCase;
import com.sofka.insurancequoter.core.riskclassification.infrastructure.adapter.in.rest.dto.RiskClassificationDto;
import com.sofka.insurancequoter.core.riskclassification.infrastructure.adapter.in.rest.dto.RiskClassificationsResponse;
import com.sofka.insurancequoter.core.riskclassification.infrastructure.adapter.in.rest.mapper.RiskClassificationRestMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RiskClassificationControllerTest {

    @Mock
    private GetRiskClassificationsUseCase getRiskClassificationsUseCase;

    @Mock
    private RiskClassificationRestMapper riskClassificationRestMapper;

    @InjectMocks
    private RiskClassificationController controller;

    @Test
    void getRiskClassifications_returnsHttp200WithClassificationList() {
        // GIVEN
        List<RiskClassification> classifications = List.of(
                new RiskClassification("STANDARD", "Riesgo estándar"),
                new RiskClassification("PREFERRED", "Riesgo preferente")
        );
        RiskClassificationsResponse response = new RiskClassificationsResponse(List.of(
                new RiskClassificationDto("STANDARD", "Riesgo estándar"),
                new RiskClassificationDto("PREFERRED", "Riesgo preferente")
        ));
        when(getRiskClassificationsUseCase.getAll()).thenReturn(classifications);
        when(riskClassificationRestMapper.toResponse(classifications)).thenReturn(response);

        // WHEN
        ResponseEntity<RiskClassificationsResponse> result = controller.getRiskClassifications();

        // THEN
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().riskClassifications()).hasSize(2);
        assertThat(result.getBody().riskClassifications().get(0).code()).isEqualTo("STANDARD");
    }

    @Test
    void getRiskClassifications_delegatesToUseCaseAndMapper() {
        // GIVEN
        List<RiskClassification> classifications = List.of();
        when(getRiskClassificationsUseCase.getAll()).thenReturn(classifications);
        when(riskClassificationRestMapper.toResponse(classifications))
                .thenReturn(new RiskClassificationsResponse(List.of()));

        // WHEN
        controller.getRiskClassifications();

        // THEN
        verify(getRiskClassificationsUseCase, times(1)).getAll();
        verify(riskClassificationRestMapper, times(1)).toResponse(classifications);
        verifyNoMoreInteractions(getRiskClassificationsUseCase, riskClassificationRestMapper);
    }
}
