package com.sofka.insurancequoter.core.guarantee.infrastructure.adapter.out.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sofka.insurancequoter.core.guarantee.domain.model.Guarantee;
import com.sofka.insurancequoter.core.guarantee.domain.port.out.GuaranteeRepository;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

public class GuaranteeJsonAdapter implements GuaranteeRepository {

    private final List<Guarantee> guarantees;

    public GuaranteeJsonAdapter(ObjectMapper objectMapper, Resource resource) {
        try {
            this.guarantees = objectMapper.readValue(
                    resource.getInputStream(),
                    new TypeReference<>() {}
            );
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load guarantees from " + resource.getDescription(), e);
        }
    }

    @Override
    public List<Guarantee> findAll() {
        return List.copyOf(guarantees);
    }

    @Override
    public List<Guarantee> findAllTarifable() {
        return guarantees.stream().filter(Guarantee::tarifable).toList();
    }
}
