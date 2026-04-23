package com.sofka.insurancequoter.core.businessline.infrastructure.adapter.in.rest.mapper;

import com.sofka.insurancequoter.core.businessline.domain.model.BusinessLine;
import com.sofka.insurancequoter.core.businessline.infrastructure.adapter.in.rest.dto.BusinessLineDto;
import com.sofka.insurancequoter.core.businessline.infrastructure.adapter.in.rest.dto.BusinessLinesResponse;

import java.util.List;

public class BusinessLineRestMapper {

    public BusinessLinesResponse toResponse(List<BusinessLine> businessLines) {
        List<BusinessLineDto> dtos = businessLines.stream()
                .map(bl -> new BusinessLineDto(bl.code(), bl.description(), bl.fireKey()))
                .toList();
        return new BusinessLinesResponse(dtos);
    }
}
