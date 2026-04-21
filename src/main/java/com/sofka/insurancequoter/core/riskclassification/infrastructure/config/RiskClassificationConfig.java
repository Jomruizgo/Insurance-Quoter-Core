package com.sofka.insurancequoter.core.riskclassification.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sofka.insurancequoter.core.riskclassification.application.usecase.GetRiskClassificationsUseCaseImpl;
import com.sofka.insurancequoter.core.riskclassification.domain.port.in.GetRiskClassificationsUseCase;
import com.sofka.insurancequoter.core.riskclassification.infrastructure.adapter.in.rest.mapper.RiskClassificationRestMapper;
import com.sofka.insurancequoter.core.riskclassification.infrastructure.adapter.out.json.RiskClassificationJsonAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class RiskClassificationConfig {

    @Bean
    public RiskClassificationJsonAdapter riskClassificationJsonAdapter(ObjectMapper objectMapper) {
        return new RiskClassificationJsonAdapter(objectMapper, new ClassPathResource("fixtures/risk-classifications.json"));
    }

    @Bean
    public GetRiskClassificationsUseCase getRiskClassificationsUseCase(RiskClassificationJsonAdapter riskClassificationJsonAdapter) {
        return new GetRiskClassificationsUseCaseImpl(riskClassificationJsonAdapter);
    }

    @Bean
    public RiskClassificationRestMapper riskClassificationRestMapper() {
        return new RiskClassificationRestMapper();
    }
}
