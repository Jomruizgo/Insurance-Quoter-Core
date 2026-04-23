package com.sofka.insurancequoter.core.tariff.application.usecase;

import com.sofka.insurancequoter.core.tariff.domain.exception.TariffNotFoundException;
import com.sofka.insurancequoter.core.tariff.domain.model.Tariffs;
import com.sofka.insurancequoter.core.tariff.domain.port.out.TariffRepository;
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
class UpdateTariffsUseCaseImplTest {

    @Mock
    private TariffRepository tariffRepository;

    private UpdateTariffsUseCaseImpl useCase;

    private static final Tariffs EXISTING = new Tariffs(
            0.0015, 0.0012, 0.07, 0.0008, 0.0005,
            0.03, 0.02, 0.015, 0.015, 0.002,
            0.003, 0.005, 0.001, 0.002, 1.16
    );

    private static final Tariffs UPDATED = new Tariffs(
            0.002, 0.0015, 0.08, 0.001, 0.0007,
            0.04, 0.025, 0.02, 0.02, 0.003,
            0.004, 0.006, 0.0015, 0.0025, 1.20
    );

    @BeforeEach
    void setUp() {
        useCase = new UpdateTariffsUseCaseImpl(tariffRepository);
    }

    @Test
    void update_whenTariffsExist_savesAndReturnsUpdatedTariffs() {
        // GIVEN
        when(tariffRepository.findCurrent()).thenReturn(Optional.of(EXISTING));
        when(tariffRepository.save(UPDATED)).thenReturn(UPDATED);

        // WHEN
        Tariffs result = useCase.update(UPDATED);

        // THEN
        assertThat(result).isEqualTo(UPDATED);
        assertThat(result.fireRate()).isEqualTo(0.002);
        assertThat(result.fireContentsRate()).isEqualTo(0.0015);
        assertThat(result.coverageExtensionFactor()).isEqualTo(0.08);
        assertThat(result.commercialFactor()).isEqualTo(1.20);
        verify(tariffRepository).findCurrent();
        verify(tariffRepository).save(UPDATED);
    }

    @Test
    void update_whenNoTariffsExist_throwsTariffNotFoundException() {
        // GIVEN
        when(tariffRepository.findCurrent()).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> useCase.update(UPDATED))
                .isInstanceOf(TariffNotFoundException.class)
                .hasMessage("Tariffs not found");

        verify(tariffRepository).findCurrent();
    }
}
