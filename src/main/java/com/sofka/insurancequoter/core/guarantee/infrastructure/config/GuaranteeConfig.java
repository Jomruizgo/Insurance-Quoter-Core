package com.sofka.insurancequoter.core.guarantee.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sofka.insurancequoter.core.guarantee.application.usecase.GetGuaranteesUseCaseImpl;
import com.sofka.insurancequoter.core.guarantee.domain.port.in.GetGuaranteesUseCase;
import com.sofka.insurancequoter.core.guarantee.infrastructure.adapter.in.rest.mapper.GuaranteeRestMapper;
import com.sofka.insurancequoter.core.guarantee.infrastructure.adapter.out.json.GuaranteeJsonAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class GuaranteeConfig {

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public GuaranteeJsonAdapter guaranteeJsonAdapter(ObjectMapper objectMapper) {
        return new GuaranteeJsonAdapter(objectMapper, new ClassPathResource("fixtures/guarantees.json"));
    }

    @Bean
    public GetGuaranteesUseCase getGuaranteesUseCase(GuaranteeJsonAdapter guaranteeJsonAdapter) {
        return new GetGuaranteesUseCaseImpl(guaranteeJsonAdapter);
    }

    @Bean
    public GuaranteeRestMapper guaranteeRestMapper() {
        return new GuaranteeRestMapper();
    }
}
