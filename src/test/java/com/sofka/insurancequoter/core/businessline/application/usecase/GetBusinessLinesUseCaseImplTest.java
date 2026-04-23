package com.sofka.insurancequoter.core.businessline.application.usecase;

import com.sofka.insurancequoter.core.businessline.domain.model.BusinessLine;
import com.sofka.insurancequoter.core.businessline.domain.port.out.BusinessLineRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetBusinessLinesUseCaseImplTest {

    @Mock
    private BusinessLineRepository businessLineRepository;

    @InjectMocks
    private GetBusinessLinesUseCaseImpl useCase;

    @Test
    void getAll_returnsListFromRepository() {
        // GIVEN
        List<BusinessLine> expected = List.of(
                new BusinessLine("BL-001", "Bodega de mercancías", "FK-INC-01"),
                new BusinessLine("BL-002", "Oficina administrativa", "FK-INC-02")
        );
        when(businessLineRepository.findAll()).thenReturn(expected);

        // WHEN
        List<BusinessLine> result = useCase.getAll();

        // THEN
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getAll_delegatesToRepositoryExactlyOnce() {
        // GIVEN
        when(businessLineRepository.findAll()).thenReturn(List.of());

        // WHEN
        useCase.getAll();

        // THEN
        verify(businessLineRepository, times(1)).findAll();
        verifyNoMoreInteractions(businessLineRepository);
    }

    @Test
    void getAll_returnsEmptyListWhenRepositoryIsEmpty() {
        // GIVEN
        when(businessLineRepository.findAll()).thenReturn(List.of());

        // WHEN
        List<BusinessLine> result = useCase.getAll();

        // THEN
        assertThat(result).isEmpty();
    }
}
