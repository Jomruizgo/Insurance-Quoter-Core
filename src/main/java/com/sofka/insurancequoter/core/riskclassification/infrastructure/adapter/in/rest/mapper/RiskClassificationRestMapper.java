package com.sofka.insurancequoter.core.riskclassification.infrastructure.adapter.in.rest.mapper;

import com.sofka.insurancequoter.core.riskclassification.domain.model.RiskClassification;
import com.sofka.insurancequoter.core.riskclassification.infrastructure.adapter.in.rest.dto.RiskClassificationDto;
import com.sofka.insurancequoter.core.riskclassification.infrastructure.adapter.in.rest.dto.RiskClassificationsResponse;

import java.util.List;

public class RiskClassificationRestMapper {

    public RiskClassificationsResponse toResponse(List<RiskClassification> classifications) {
        List<RiskClassificationDto> dtos = classifications.stream()
                .map(c -> new RiskClassificationDto(c.code(), c.description()))
                .toList();
        return new RiskClassificationsResponse(dtos);
    }
}
