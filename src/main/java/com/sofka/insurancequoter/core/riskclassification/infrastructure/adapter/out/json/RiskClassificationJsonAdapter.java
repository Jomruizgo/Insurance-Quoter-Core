package com.sofka.insurancequoter.core.riskclassification.infrastructure.adapter.out.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sofka.insurancequoter.core.riskclassification.domain.model.RiskClassification;
import com.sofka.insurancequoter.core.riskclassification.domain.port.out.RiskClassificationRepository;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

public class RiskClassificationJsonAdapter implements RiskClassificationRepository {

    private final List<RiskClassification> classifications;

    public RiskClassificationJsonAdapter(ObjectMapper objectMapper, Resource resource) {
        try {
            this.classifications = objectMapper.readValue(
                    resource.getInputStream(),
                    new TypeReference<>() {}
            );
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load risk classifications from " + resource.getDescription(), e);
        }
    }

    @Override
    public List<RiskClassification> findAll() {
        return List.copyOf(classifications);
    }
}
