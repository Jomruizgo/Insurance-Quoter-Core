package com.sofka.insurancequoter.core.guarantee.application.usecase;

import com.sofka.insurancequoter.core.guarantee.domain.model.Guarantee;
import com.sofka.insurancequoter.core.guarantee.domain.port.in.GetGuaranteesUseCase;
import com.sofka.insurancequoter.core.guarantee.domain.port.out.GuaranteeRepository;

import java.util.List;

public class GetGuaranteesUseCaseImpl implements GetGuaranteesUseCase {

    private final GuaranteeRepository guaranteeRepository;

    public GetGuaranteesUseCaseImpl(GuaranteeRepository guaranteeRepository) {
        this.guaranteeRepository = guaranteeRepository;
    }

    @Override
    public List<Guarantee> getAll() {
        return guaranteeRepository.findAll();
    }
}
