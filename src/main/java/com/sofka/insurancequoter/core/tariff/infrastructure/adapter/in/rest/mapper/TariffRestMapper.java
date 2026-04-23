package com.sofka.insurancequoter.core.tariff.infrastructure.adapter.in.rest.mapper;

import com.sofka.insurancequoter.core.tariff.domain.model.Tariffs;
import com.sofka.insurancequoter.core.tariff.infrastructure.adapter.in.rest.dto.TariffsDto;
import com.sofka.insurancequoter.core.tariff.infrastructure.adapter.in.rest.dto.TariffsResponse;
import com.sofka.insurancequoter.core.tariff.infrastructure.adapter.in.rest.dto.UpdateTariffsRequest;
import org.springframework.stereotype.Component;

/**
 * Maps between the Tariffs domain model and REST DTOs.
 */
@Component
public class TariffRestMapper {

    public TariffsResponse toResponse(Tariffs tariffs) {
        TariffsDto dto = new TariffsDto(
                tariffs.fireRate(),
                tariffs.fireContentsRate(),
                tariffs.coverageExtensionFactor(),
                tariffs.cattevFactor(),
                tariffs.catfhmFactor(),
                tariffs.debrisRemovalFactor(),
                tariffs.extraordinaryExpensesFactor(),
                tariffs.rentalLossRate(),
                tariffs.businessInterruptionRate(),
                tariffs.electronicEquipmentRate(),
                tariffs.theftRate(),
                tariffs.cashAndValuesRate(),
                tariffs.glassRate(),
                tariffs.luminousSignageRate(),
                tariffs.commercialFactor()
        );
        return new TariffsResponse(dto);
    }

    public Tariffs toDomain(UpdateTariffsRequest request) {
        return new Tariffs(
                request.fireRate(),
                request.fireContentsRate(),
                request.coverageExtensionFactor(),
                request.cattevFactor(),
                request.catfhmFactor(),
                request.debrisRemovalFactor(),
                request.extraordinaryExpensesFactor(),
                request.rentalLossRate(),
                request.businessInterruptionRate(),
                request.electronicEquipmentRate(),
                request.theftRate(),
                request.cashAndValuesRate(),
                request.glassRate(),
                request.luminousSignageRate(),
                request.commercialFactor()
        );
    }
}
