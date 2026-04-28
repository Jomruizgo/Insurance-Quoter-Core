package com.sofka.insurancequoter.core.tariff.application.usecase;

import com.sofka.insurancequoter.core.shared.infrastructure.metrics.TariffMetrics;
import com.sofka.insurancequoter.core.tariff.domain.exception.TariffNotFoundException;
import com.sofka.insurancequoter.core.tariff.domain.model.Tariffs;
import com.sofka.insurancequoter.core.tariff.domain.port.out.TariffRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTariffsUseCaseImplTest {

    @Mock
    private TariffRepository tariffRepository;

    private GetTariffsUseCaseImpl useCase;

    // Full 15-field tariff used across tests
    private static final Tariffs TARIFFS = new Tariffs(
            0.0015, 0.0012, 0.07, 0.0008, 0.0005,
            0.03, 0.02, 0.015, 0.015, 0.002,
            0.003, 0.005, 0.001, 0.002, 1.16
    );

    @BeforeEach
    void setUp() {
        // TariffMetrics uses a real SimpleMeterRegistry — no mocking needed for infrastructure
        TariffMetrics tariffMetrics = new TariffMetrics(new SimpleMeterRegistry());
        useCase = new GetTariffsUseCaseImpl(tariffRepository, tariffMetrics);
    }

    @Test
    void getCurrent_whenTariffsExist_returnsTariffs() {
        // GIVEN
        when(tariffRepository.findCurrent()).thenReturn(Optional.of(TARIFFS));

        // WHEN
        Tariffs result = useCase.getCurrent();

        // THEN
        assertThat(result).isEqualTo(TARIFFS);
        assertThat(result.fireRate()).isEqualTo(0.0015);
        assertThat(result.fireContentsRate()).isEqualTo(0.0012);
        assertThat(result.coverageExtensionFactor()).isEqualTo(0.07);
        assertThat(result.cattevFactor()).isEqualTo(0.0008);
        assertThat(result.catfhmFactor()).isEqualTo(0.0005);
        assertThat(result.debrisRemovalFactor()).isEqualTo(0.03);
        assertThat(result.extraordinaryExpensesFactor()).isEqualTo(0.02);
        assertThat(result.rentalLossRate()).isEqualTo(0.015);
        assertThat(result.businessInterruptionRate()).isEqualTo(0.015);
        assertThat(result.electronicEquipmentRate()).isEqualTo(0.002);
        assertThat(result.theftRate()).isEqualTo(0.003);
        assertThat(result.cashAndValuesRate()).isEqualTo(0.005);
        assertThat(result.glassRate()).isEqualTo(0.001);
        assertThat(result.luminousSignageRate()).isEqualTo(0.002);
        assertThat(result.commercialFactor()).isEqualTo(1.16);
        verify(tariffRepository).findCurrent();
    }

    @Test
    void getCurrent_whenNoTariffsExist_throwsTariffNotFoundException() {
        // GIVEN
        when(tariffRepository.findCurrent()).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> useCase.getCurrent())
                .isInstanceOf(TariffNotFoundException.class)
                .hasMessage("Tariffs not found");

        verify(tariffRepository).findCurrent();
    }
}
