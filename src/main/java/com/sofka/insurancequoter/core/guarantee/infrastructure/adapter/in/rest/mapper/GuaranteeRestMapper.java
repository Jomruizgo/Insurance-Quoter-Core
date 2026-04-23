package com.sofka.insurancequoter.core.guarantee.infrastructure.adapter.in.rest.mapper;

import com.sofka.insurancequoter.core.guarantee.domain.model.Guarantee;
import com.sofka.insurancequoter.core.guarantee.infrastructure.adapter.in.rest.dto.GuaranteeDto;
import com.sofka.insurancequoter.core.guarantee.infrastructure.adapter.in.rest.dto.GuaranteesResponse;

import java.util.List;

public class GuaranteeRestMapper {

    public GuaranteesResponse toResponse(List<Guarantee> guarantees) {
        List<GuaranteeDto> dtos = guarantees.stream()
                .map(g -> new GuaranteeDto(g.code(), g.description(), g.tarifable()))
                .toList();
        return new GuaranteesResponse(dtos);
    }
}
