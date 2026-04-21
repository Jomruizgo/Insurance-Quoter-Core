package com.sofka.insurancequoter.core.businessline.application.usecase;

import com.sofka.insurancequoter.core.businessline.domain.model.BusinessLine;
import com.sofka.insurancequoter.core.businessline.domain.port.in.GetBusinessLinesUseCase;
import com.sofka.insurancequoter.core.businessline.domain.port.out.BusinessLineRepository;

import java.util.List;

public class GetBusinessLinesUseCaseImpl implements GetBusinessLinesUseCase {

    private final BusinessLineRepository businessLineRepository;

    public GetBusinessLinesUseCaseImpl(BusinessLineRepository businessLineRepository) {
        this.businessLineRepository = businessLineRepository;
    }

    @Override
    public List<BusinessLine> getAll() {
        return businessLineRepository.findAll();
    }
}
