package com.sofka.insurancequoter.core.guarantee.domain.port.in;

import com.sofka.insurancequoter.core.guarantee.domain.model.Guarantee;

import java.util.List;

public interface GetGuaranteesUseCase {
    List<Guarantee> getAll();
}
