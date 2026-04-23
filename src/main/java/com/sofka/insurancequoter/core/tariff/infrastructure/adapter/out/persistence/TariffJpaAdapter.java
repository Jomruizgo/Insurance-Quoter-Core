package com.sofka.insurancequoter.core.tariff.infrastructure.adapter.out.persistence;

import com.sofka.insurancequoter.core.tariff.domain.model.Tariffs;
import com.sofka.insurancequoter.core.tariff.domain.port.out.TariffRepository;
import com.sofka.insurancequoter.core.tariff.infrastructure.adapter.out.persistence.entity.TariffJpa;
import com.sofka.insurancequoter.core.tariff.infrastructure.adapter.out.persistence.repository.TariffJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * JPA adapter that implements the TariffRepository output port.
 * The tariffs table is a single-row catalog — always uses id = 1.
 */
@Component
@RequiredArgsConstructor
public class TariffJpaAdapter implements TariffRepository {

    private final TariffJpaRepository jpaRepository;

    @Override
    public Optional<Tariffs> findCurrent() {
        return jpaRepository.findById(1L).map(this::toDomain);
    }

    @Override
    public Tariffs save(Tariffs tariffs) {
        TariffJpa jpa = toJpa(tariffs);
        jpa.setId(1L);
        return toDomain(jpaRepository.save(jpa));
    }

    private Tariffs toDomain(TariffJpa j) {
        return new Tariffs(
                j.getFireRate(),
                j.getFireContentsRate(),
                j.getCoverageExtensionFactor(),
                j.getCattevFactor(),
                j.getCatfhmFactor(),
                j.getDebrisRemovalFactor(),
                j.getExtraordinaryExpensesFactor(),
                j.getRentalLossRate(),
                j.getBusinessInterruptionRate(),
                j.getElectronicEquipmentRate(),
                j.getTheftRate(),
                j.getCashAndValuesRate(),
                j.getGlassRate(),
                j.getLuminousSignageRate(),
                j.getCommercialFactor()
        );
    }

    private TariffJpa toJpa(Tariffs t) {
        return TariffJpa.builder()
                .fireRate(t.fireRate())
                .fireContentsRate(t.fireContentsRate())
                .coverageExtensionFactor(t.coverageExtensionFactor())
                .cattevFactor(t.cattevFactor())
                .catfhmFactor(t.catfhmFactor())
                .debrisRemovalFactor(t.debrisRemovalFactor())
                .extraordinaryExpensesFactor(t.extraordinaryExpensesFactor())
                .rentalLossRate(t.rentalLossRate())
                .businessInterruptionRate(t.businessInterruptionRate())
                .electronicEquipmentRate(t.electronicEquipmentRate())
                .theftRate(t.theftRate())
                .cashAndValuesRate(t.cashAndValuesRate())
                .glassRate(t.glassRate())
                .luminousSignageRate(t.luminousSignageRate())
                .commercialFactor(t.commercialFactor())
                .build();
    }
}
