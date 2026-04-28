package com.sofka.insurancequoter.core.riskclassification.application.usecase;

import com.sofka.insurancequoter.core.riskclassification.domain.model.RiskClassification;
import com.sofka.insurancequoter.core.riskclassification.domain.port.in.GetRiskClassificationsUseCase;
import com.sofka.insurancequoter.core.riskclassification.domain.port.out.RiskClassificationRepository;
import io.micrometer.observation.annotation.Observed;

import java.util.List;

/**
 * @Observed creates a trace span per invocation via ObservedAspect (registered in ObservabilityConfig).
 */
@Observed(name = "risk-classification.get-all", contextualName = "get-risk-classifications")
public class GetRiskClassificationsUseCaseImpl implements GetRiskClassificationsUseCase {

    private final RiskClassificationRepository riskClassificationRepository;

    public GetRiskClassificationsUseCaseImpl(RiskClassificationRepository riskClassificationRepository) {
        this.riskClassificationRepository = riskClassificationRepository;
    }

    @Override
    public List<RiskClassification> getAll() {
        return riskClassificationRepository.findAll();
    }
}
