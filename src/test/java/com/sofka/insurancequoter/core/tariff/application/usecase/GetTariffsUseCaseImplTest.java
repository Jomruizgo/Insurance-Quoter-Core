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
class GetTariffsUseCaseImplTest {

    @Mock
    private TariffRepository tariffRepository;

    private GetTariffsUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetTariffsUseCaseImpl(tariffRepository);
    }

    @Test
    void getCurrent_whenTariffsExist_returnsTariffs() {
        // GIVEN
        Tariffs expected = new Tariffs(0.0015, 0.0008, 0.0005, 0.003, 0.002);
        when(tariffRepository.findCurrent()).thenReturn(Optional.of(expected));

        // WHEN
        Tariffs result = useCase.getCurrent();

        // THEN
        assertThat(result).isEqualTo(expected);
        assertThat(result.fireRate()).isEqualTo(0.0015);
        assertThat(result.cattevFactor()).isEqualTo(0.0008);
        assertThat(result.catfhmFactor()).isEqualTo(0.0005);
        assertThat(result.theftRate()).isEqualTo(0.003);
        assertThat(result.electronicEquipmentRate()).isEqualTo(0.002);
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
