package com.sofka.insurancequoter.core.riskclassification.domain.port.in;

import com.sofka.insurancequoter.core.riskclassification.domain.model.RiskClassification;

import java.util.List;

public interface GetRiskClassificationsUseCase {
    List<RiskClassification> getAll();
}
