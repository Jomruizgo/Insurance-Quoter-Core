package com.sofka.insurancequoter.core.guarantee.application.usecase;

import com.sofka.insurancequoter.core.guarantee.domain.model.Guarantee;
import com.sofka.insurancequoter.core.guarantee.domain.port.out.GuaranteeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetGuaranteesUseCaseImplTest {

    @Mock
    private GuaranteeRepository guaranteeRepository;

    @InjectMocks
    private GetGuaranteesUseCaseImpl useCase;

    @Test
    void getAll_returnsListFromRepository() {
        // GIVEN
        List<Guarantee> expected = List.of(
                new Guarantee("GUA-FIRE", "Incendio edificios", true),
                new Guarantee("GUA-THEFT", "Robo", true),
                new Guarantee("GUA-GLASS", "Vidrios", true)
        );
        when(guaranteeRepository.findAll()).thenReturn(expected);

        // WHEN
        List<Guarantee> result = useCase.getAll();

        // THEN
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getAll_delegatesToRepositoryExactlyOnce() {
        // GIVEN
        when(guaranteeRepository.findAll()).thenReturn(List.of());

        // WHEN
        useCase.getAll();

        // THEN
        verify(guaranteeRepository, times(1)).findAll();
        verifyNoMoreInteractions(guaranteeRepository);
    }

    @Test
    void getAll_returnsEmptyListWhenRepositoryIsEmpty() {
        // GIVEN
        when(guaranteeRepository.findAll()).thenReturn(List.of());

        // WHEN
        List<Guarantee> result = useCase.getAll();

        // THEN
        assertThat(result).isEmpty();
    }
}
