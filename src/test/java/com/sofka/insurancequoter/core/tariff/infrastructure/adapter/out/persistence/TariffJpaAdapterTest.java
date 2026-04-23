package com.sofka.insurancequoter.core.tariff.infrastructure.adapter.out.persistence;

import com.sofka.insurancequoter.core.tariff.domain.model.Tariffs;
import com.sofka.insurancequoter.core.tariff.infrastructure.adapter.out.persistence.entity.TariffJpa;
import com.sofka.insurancequoter.core.tariff.infrastructure.adapter.out.persistence.repository.TariffJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TariffJpaAdapterTest {

    @Mock
    private TariffJpaRepository jpaRepository;

    private TariffJpaAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new TariffJpaAdapter(jpaRepository);
    }

    @Test
    void findCurrent_whenRowExists_returnsMappedDomain() {
        // GIVEN
        TariffJpa jpa = TariffJpa.builder()
                .id(1L)
                .fireRate(0.0015)
                .fireContentsRate(0.0012)
                .coverageExtensionFactor(0.07)
                .cattevFactor(0.0008)
                .catfhmFactor(0.0005)
                .debrisRemovalFactor(0.03)
                .extraordinaryExpensesFactor(0.02)
                .rentalLossRate(0.015)
                .businessInterruptionRate(0.015)
                .electronicEquipmentRate(0.002)
                .theftRate(0.003)
                .cashAndValuesRate(0.005)
                .glassRate(0.001)
                .luminousSignageRate(0.002)
                .commercialFactor(1.16)
                .build();
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(jpa));

        // WHEN
        Optional<Tariffs> result = adapter.findCurrent();

        // THEN
        assertThat(result).isPresent();
        Tariffs tariffs = result.get();
        assertThat(tariffs.fireRate()).isEqualTo(0.0015);
        assertThat(tariffs.fireContentsRate()).isEqualTo(0.0012);
        assertThat(tariffs.coverageExtensionFactor()).isEqualTo(0.07);
        assertThat(tariffs.cattevFactor()).isEqualTo(0.0008);
        assertThat(tariffs.catfhmFactor()).isEqualTo(0.0005);
        assertThat(tariffs.debrisRemovalFactor()).isEqualTo(0.03);
        assertThat(tariffs.extraordinaryExpensesFactor()).isEqualTo(0.02);
        assertThat(tariffs.rentalLossRate()).isEqualTo(0.015);
        assertThat(tariffs.businessInterruptionRate()).isEqualTo(0.015);
        assertThat(tariffs.electronicEquipmentRate()).isEqualTo(0.002);
        assertThat(tariffs.theftRate()).isEqualTo(0.003);
        assertThat(tariffs.cashAndValuesRate()).isEqualTo(0.005);
        assertThat(tariffs.glassRate()).isEqualTo(0.001);
        assertThat(tariffs.luminousSignageRate()).isEqualTo(0.002);
        assertThat(tariffs.commercialFactor()).isEqualTo(1.16);
        verify(jpaRepository).findById(1L);
    }

    @Test
    void findCurrent_whenNoRow_returnsEmpty() {
        // GIVEN
        when(jpaRepository.findById(1L)).thenReturn(Optional.empty());

        // WHEN
        Optional<Tariffs> result = adapter.findCurrent();

        // THEN
        assertThat(result).isEmpty();
        verify(jpaRepository).findById(1L);
    }

    @Test
    void save_persistsWithIdOneAndReturnsMappedDomain() {
        // GIVEN
        Tariffs domain = new Tariffs(
                0.002, 0.0015, 0.08, 0.001, 0.0007,
                0.04, 0.025, 0.02, 0.02, 0.003,
                0.004, 0.006, 0.0015, 0.0025, 1.20
        );
        TariffJpa savedJpa = TariffJpa.builder()
                .id(1L)
                .fireRate(0.002)
                .fireContentsRate(0.0015)
                .coverageExtensionFactor(0.08)
                .cattevFactor(0.001)
                .catfhmFactor(0.0007)
                .debrisRemovalFactor(0.04)
                .extraordinaryExpensesFactor(0.025)
                .rentalLossRate(0.02)
                .businessInterruptionRate(0.02)
                .electronicEquipmentRate(0.003)
                .theftRate(0.004)
                .cashAndValuesRate(0.006)
                .glassRate(0.0015)
                .luminousSignageRate(0.0025)
                .commercialFactor(1.20)
                .build();
        when(jpaRepository.save(any(TariffJpa.class))).thenReturn(savedJpa);

        // WHEN
        Tariffs result = adapter.save(domain);

        // THEN
        assertThat(result.fireRate()).isEqualTo(0.002);
        assertThat(result.fireContentsRate()).isEqualTo(0.0015);
        assertThat(result.coverageExtensionFactor()).isEqualTo(0.08);
        assertThat(result.commercialFactor()).isEqualTo(1.20);
        verify(jpaRepository).save(any(TariffJpa.class));
    }
}
