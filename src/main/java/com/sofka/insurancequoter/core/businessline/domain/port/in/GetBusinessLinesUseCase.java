package com.sofka.insurancequoter.core.businessline.domain.port.in;

import com.sofka.insurancequoter.core.businessline.domain.model.BusinessLine;

import java.util.List;

public interface GetBusinessLinesUseCase {
    List<BusinessLine> getAll();
}
