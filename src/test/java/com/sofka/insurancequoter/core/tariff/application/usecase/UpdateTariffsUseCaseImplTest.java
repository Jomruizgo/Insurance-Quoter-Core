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

    @BeforeEach
    void setUp() {
        useCase = new UpdateTariffsUseCaseImpl(tariffRepository);
    }

    @Test
    void update_whenTariffsExist_savesAndReturnsUpdatedTariffs() {
        // GIVEN
        Tariffs existing = new Tariffs(0.0015, 0.0008, 0.0005, 0.003, 0.002);
        Tariffs updated = new Tariffs(0.002, 0.001, 0.0007, 0.004, 0.003);
        when(tariffRepository.findCurrent()).thenReturn(Optional.of(existing));
        when(tariffRepository.save(updated)).thenReturn(updated);

        // WHEN
        Tariffs result = useCase.update(updated);

        // THEN
        assertThat(result).isEqualTo(updated);
        assertThat(result.fireRate()).isEqualTo(0.002);
        assertThat(result.cattevFactor()).isEqualTo(0.001);
        assertThat(result.catfhmFactor()).isEqualTo(0.0007);
        assertThat(result.theftRate()).isEqualTo(0.004);
        assertThat(result.electronicEquipmentRate()).isEqualTo(0.003);
        verify(tariffRepository).findCurrent();
        verify(tariffRepository).save(updated);
    }

    @Test
    void update_whenNoTariffsExist_throwsTariffNotFoundException() {
        // GIVEN
        Tariffs updated = new Tariffs(0.002, 0.001, 0.0007, 0.004, 0.003);
        when(tariffRepository.findCurrent()).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> useCase.update(updated))
                .isInstanceOf(TariffNotFoundException.class)
                .hasMessage("Tariffs not found");

        verify(tariffRepository).findCurrent();
    }
}
