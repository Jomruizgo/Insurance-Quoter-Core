package com.sofka.insurancequoter.core.riskclassification.application.usecase;

import com.sofka.insurancequoter.core.riskclassification.domain.model.RiskClassification;
import com.sofka.insurancequoter.core.riskclassification.domain.port.out.RiskClassificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetRiskClassificationsUseCaseImplTest {

    @Mock
    private RiskClassificationRepository riskClassificationRepository;

    @InjectMocks
    private GetRiskClassificationsUseCaseImpl useCase;

    @Test
    void getAll_returnsListFromRepository() {
        // GIVEN
        List<RiskClassification> expected = List.of(
                new RiskClassification("STANDARD", "Riesgo estándar"),
                new RiskClassification("PREFERRED", "Riesgo preferente")
        );
        when(riskClassificationRepository.findAll()).thenReturn(expected);

        // WHEN
        List<RiskClassification> result = useCase.getAll();

        // THEN
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getAll_delegatesToRepositoryExactlyOnce() {
        // GIVEN
        when(riskClassificationRepository.findAll()).thenReturn(List.of());

        // WHEN
        useCase.getAll();

        // THEN
        verify(riskClassificationRepository, times(1)).findAll();
        verifyNoMoreInteractions(riskClassificationRepository);
    }

    @Test
    void getAll_returnsEmptyListWhenRepositoryIsEmpty() {
        // GIVEN
        when(riskClassificationRepository.findAll()).thenReturn(List.of());

        // WHEN
        List<RiskClassification> result = useCase.getAll();

        // THEN
        assertThat(result).isEmpty();
    }
}
