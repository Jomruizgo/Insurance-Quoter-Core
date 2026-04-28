package com.sofka.insurancequoter.core.businessline.application.usecase;

import com.sofka.insurancequoter.core.businessline.domain.model.BusinessLine;
import com.sofka.insurancequoter.core.businessline.domain.port.in.GetBusinessLinesUseCase;
import com.sofka.insurancequoter.core.businessline.domain.port.out.BusinessLineRepository;
import io.micrometer.observation.annotation.Observed;

import java.util.List;

/**
 * @Observed creates a trace span per invocation via ObservedAspect (registered in ObservabilityConfig).
 */
@Observed(name = "business-line.get-all", contextualName = "get-business-lines")
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
