package com.sofka.insurancequoter.core.businessline.infrastructure.adapter.in.rest;

import com.sofka.insurancequoter.core.businessline.domain.port.in.GetBusinessLinesUseCase;
import com.sofka.insurancequoter.core.businessline.infrastructure.adapter.in.rest.dto.BusinessLinesResponse;
import com.sofka.insurancequoter.core.businessline.infrastructure.adapter.in.rest.mapper.BusinessLineRestMapper;
import com.sofka.insurancequoter.core.businessline.infrastructure.adapter.in.rest.swaggerdocs.BusinessLineApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BusinessLineController implements BusinessLineApi {

    private final GetBusinessLinesUseCase getBusinessLinesUseCase;
    private final BusinessLineRestMapper businessLineRestMapper;

    @Override
    public ResponseEntity<BusinessLinesResponse> getBusinessLines() {
        return ResponseEntity.ok(businessLineRestMapper.toResponse(getBusinessLinesUseCase.getAll()));
    }
}
