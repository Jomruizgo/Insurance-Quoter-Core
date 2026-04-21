package com.sofka.insurancequoter.core.guarantee.infrastructure.adapter.in.rest;

import com.sofka.insurancequoter.core.guarantee.domain.port.in.GetGuaranteesUseCase;
import com.sofka.insurancequoter.core.guarantee.infrastructure.adapter.in.rest.dto.GuaranteesResponse;
import com.sofka.insurancequoter.core.guarantee.infrastructure.adapter.in.rest.mapper.GuaranteeRestMapper;
import com.sofka.insurancequoter.core.guarantee.infrastructure.adapter.in.rest.swaggerdocs.GuaranteeApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GuaranteeController implements GuaranteeApi {

    private final GetGuaranteesUseCase getGuaranteesUseCase;
    private final GuaranteeRestMapper guaranteeRestMapper;

    @Override
    public ResponseEntity<GuaranteesResponse> getGuarantees() {
        return ResponseEntity.ok(guaranteeRestMapper.toResponse(getGuaranteesUseCase.getAll()));
    }
}
