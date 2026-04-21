package com.sofka.insurancequoter.core.guarantee.domain.port.out;

import com.sofka.insurancequoter.core.guarantee.domain.model.Guarantee;

import java.util.List;

public interface GuaranteeRepository {
    List<Guarantee> findAll();
    List<Guarantee> findAllTarifable();
}
