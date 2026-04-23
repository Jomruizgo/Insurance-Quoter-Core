package com.sofka.insurancequoter.core.riskclassification.infrastructure.adapter.in.rest;

import com.sofka.insurancequoter.core.riskclassification.domain.port.in.GetRiskClassificationsUseCase;
import com.sofka.insurancequoter.core.riskclassification.infrastructure.adapter.in.rest.dto.RiskClassificationsResponse;
import com.sofka.insurancequoter.core.riskclassification.infrastructure.adapter.in.rest.mapper.RiskClassificationRestMapper;
import com.sofka.insurancequoter.core.riskclassification.infrastructure.adapter.in.rest.swaggerdocs.RiskClassificationApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RiskClassificationController implements RiskClassificationApi {

    private final GetRiskClassificationsUseCase getRiskClassificationsUseCase;
    private final RiskClassificationRestMapper riskClassificationRestMapper;

    @Override
    public ResponseEntity<RiskClassificationsResponse> getRiskClassifications() {
        return ResponseEntity.ok(
                riskClassificationRestMapper.toResponse(getRiskClassificationsUseCase.getAll())
        );
    }
}
