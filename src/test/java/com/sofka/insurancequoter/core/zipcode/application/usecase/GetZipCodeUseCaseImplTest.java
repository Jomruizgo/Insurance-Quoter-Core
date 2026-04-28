package com.sofka.insurancequoter.core.zipcode.application.usecase;

import com.sofka.insurancequoter.core.shared.infrastructure.metrics.ZipCodeMetrics;
import com.sofka.insurancequoter.core.zipcode.domain.exception.ZipCodeNotFoundException;
import com.sofka.insurancequoter.core.zipcode.domain.model.ZipCode;
import com.sofka.insurancequoter.core.zipcode.domain.port.out.ZipCodeRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetZipCodeUseCaseImplTest {

    @Mock
    private ZipCodeRepository zipCodeRepository;

    private GetZipCodeUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        // ZipCodeMetrics uses a real SimpleMeterRegistry — no mocking needed for infrastructure
        ZipCodeMetrics zipCodeMetrics = new ZipCodeMetrics(new SimpleMeterRegistry());
        useCase = new GetZipCodeUseCaseImpl(zipCodeRepository, zipCodeMetrics);
    }

    @Test
    void getByZipCode_whenZipCodeExists_returnsZipCode() {
        // GIVEN
        ZipCode expected = new ZipCode(
                "06600", "Ciudad de México", "Cuauhtémoc", "Ciudad de México",
                List.of("Juárez", "Tabacalera"), "ZONE_A", "TEV-1", "FHM-2"
        );
        when(zipCodeRepository.findByZipCode("06600")).thenReturn(Optional.of(expected));

        // WHEN
        ZipCode result = useCase.getByZipCode("06600");

        // THEN
        assertThat(result).isEqualTo(expected);
        verify(zipCodeRepository, times(1)).findByZipCode("06600");
    }

    @Test
    void getByZipCode_whenZipCodeDoesNotExist_throwsZipCodeNotFoundException() {
        // GIVEN
        when(zipCodeRepository.findByZipCode("99999")).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> useCase.getByZipCode("99999"))
                .isInstanceOf(ZipCodeNotFoundException.class)
                .hasMessageContaining("99999");

        verify(zipCodeRepository, times(1)).findByZipCode("99999");
    }
}
