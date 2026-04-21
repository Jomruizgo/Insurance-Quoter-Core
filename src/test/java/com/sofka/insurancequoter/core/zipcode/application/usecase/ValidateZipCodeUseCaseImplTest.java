package com.sofka.insurancequoter.core.zipcode.application.usecase;

import com.sofka.insurancequoter.core.zipcode.domain.model.ZipCode;
import com.sofka.insurancequoter.core.zipcode.domain.model.ZipCodeValidationResult;
import com.sofka.insurancequoter.core.zipcode.domain.port.out.ZipCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidateZipCodeUseCaseImplTest {

    @Mock
    private ZipCodeRepository zipCodeRepository;

    private ValidateZipCodeUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new ValidateZipCodeUseCaseImpl(zipCodeRepository);
    }

    @Test
    void validate_whenZipCodeExists_returnsValidTrue() {
        // GIVEN
        ZipCode zipCode = new ZipCode(
                "06600", "Ciudad de México", "Cuauhtémoc", "Ciudad de México",
                List.of("Juárez"), "ZONE_A", "TEV-1", "FHM-2"
        );
        when(zipCodeRepository.findByZipCode("06600")).thenReturn(Optional.of(zipCode));

        // WHEN
        ZipCodeValidationResult result = useCase.validate("06600");

        // THEN
        assertThat(result.valid()).isTrue();
        assertThat(result.zipCode()).isEqualTo("06600");
        verify(zipCodeRepository, times(1)).findByZipCode("06600");
    }

    @Test
    void validate_whenZipCodeDoesNotExist_returnsValidFalse() {
        // GIVEN
        when(zipCodeRepository.findByZipCode("99999")).thenReturn(Optional.empty());

        // WHEN
        ZipCodeValidationResult result = useCase.validate("99999");

        // THEN
        assertThat(result.valid()).isFalse();
        assertThat(result.zipCode()).isEqualTo("99999");
        verify(zipCodeRepository, times(1)).findByZipCode("99999");
    }
}
