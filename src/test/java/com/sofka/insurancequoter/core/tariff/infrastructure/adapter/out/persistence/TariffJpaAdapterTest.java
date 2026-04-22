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
                .cattevFactor(0.0008)
                .catfhmFactor(0.0005)
                .theftRate(0.003)
                .electronicEquipmentRate(0.002)
                .build();
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(jpa));

        // WHEN
        Optional<Tariffs> result = adapter.findCurrent();

        // THEN
        assertThat(result).isPresent();
        Tariffs tariffs = result.get();
        assertThat(tariffs.fireRate()).isEqualTo(0.0015);
        assertThat(tariffs.cattevFactor()).isEqualTo(0.0008);
        assertThat(tariffs.catfhmFactor()).isEqualTo(0.0005);
        assertThat(tariffs.theftRate()).isEqualTo(0.003);
        assertThat(tariffs.electronicEquipmentRate()).isEqualTo(0.002);
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
        Tariffs domain = new Tariffs(0.002, 0.001, 0.0007, 0.004, 0.003);
        TariffJpa savedJpa = TariffJpa.builder()
                .id(1L)
                .fireRate(0.002)
                .cattevFactor(0.001)
                .catfhmFactor(0.0007)
                .theftRate(0.004)
                .electronicEquipmentRate(0.003)
                .build();
        when(jpaRepository.save(any(TariffJpa.class))).thenReturn(savedJpa);

        // WHEN
        Tariffs result = adapter.save(domain);

        // THEN
        assertThat(result.fireRate()).isEqualTo(0.002);
        assertThat(result.cattevFactor()).isEqualTo(0.001);
        assertThat(result.catfhmFactor()).isEqualTo(0.0007);
        assertThat(result.theftRate()).isEqualTo(0.004);
        assertThat(result.electronicEquipmentRate()).isEqualTo(0.003);
        verify(jpaRepository).save(any(TariffJpa.class));
    }
}
