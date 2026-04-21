package com.sofka.insurancequoter.core.businessline.infrastructure.adapter.out.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sofka.insurancequoter.core.businessline.domain.model.BusinessLine;
import com.sofka.insurancequoter.core.businessline.domain.port.out.BusinessLineRepository;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

public class BusinessLineJsonAdapter implements BusinessLineRepository {

    private final List<BusinessLine> businessLines;

    public BusinessLineJsonAdapter(ObjectMapper objectMapper, Resource resource) {
        try {
            this.businessLines = objectMapper.readValue(
                    resource.getInputStream(),
                    new TypeReference<>() {}
            );
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load business lines from " + resource.getDescription(), e);
        }
    }

    @Override
    public List<BusinessLine> findAll() {
        return List.copyOf(businessLines);
    }
}
