package com.sofka.insurancequoter.core.businessline.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sofka.insurancequoter.core.businessline.application.usecase.GetBusinessLinesUseCaseImpl;
import com.sofka.insurancequoter.core.businessline.domain.port.in.GetBusinessLinesUseCase;
import com.sofka.insurancequoter.core.businessline.infrastructure.adapter.in.rest.mapper.BusinessLineRestMapper;
import com.sofka.insurancequoter.core.businessline.infrastructure.adapter.out.json.BusinessLineJsonAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class BusinessLineConfig {

    @Bean
    public BusinessLineJsonAdapter businessLineJsonAdapter(ObjectMapper objectMapper) {
        return new BusinessLineJsonAdapter(objectMapper, new ClassPathResource("fixtures/business-lines.json"));
    }

    @Bean
    public GetBusinessLinesUseCase getBusinessLinesUseCase(BusinessLineJsonAdapter businessLineJsonAdapter) {
        return new GetBusinessLinesUseCaseImpl(businessLineJsonAdapter);
    }

    @Bean
    public BusinessLineRestMapper businessLineRestMapper() {
        return new BusinessLineRestMapper();
    }
}
