package com.sofka.insurancequoter.core.businessline.domain.port.out;

import com.sofka.insurancequoter.core.businessline.domain.model.BusinessLine;

import java.util.List;

public interface BusinessLineRepository {
    List<BusinessLine> findAll();
}
