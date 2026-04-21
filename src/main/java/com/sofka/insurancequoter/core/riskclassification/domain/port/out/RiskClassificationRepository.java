package com.sofka.insurancequoter.core.riskclassification.domain.port.out;

import com.sofka.insurancequoter.core.riskclassification.domain.model.RiskClassification;

import java.util.List;

public interface RiskClassificationRepository {
    List<RiskClassification> findAll();
}
