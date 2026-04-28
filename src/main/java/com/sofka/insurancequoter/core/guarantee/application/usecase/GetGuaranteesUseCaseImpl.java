package com.sofka.insurancequoter.core.guarantee.application.usecase;

import com.sofka.insurancequoter.core.guarantee.domain.model.Guarantee;
import com.sofka.insurancequoter.core.guarantee.domain.port.in.GetGuaranteesUseCase;
import com.sofka.insurancequoter.core.guarantee.domain.port.out.GuaranteeRepository;
import io.micrometer.observation.annotation.Observed;

import java.util.List;

/**
 * @Observed creates a trace span per invocation via ObservedAspect (registered in ObservabilityConfig).
 */
@Observed(name = "guarantee.get-all", contextualName = "get-guarantees")
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
