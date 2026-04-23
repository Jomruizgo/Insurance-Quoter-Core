package com.sofka.insurancequoter.core.guarantee.infrastructure.adapter.in.rest;

import com.sofka.insurancequoter.core.guarantee.domain.model.Guarantee;
import com.sofka.insurancequoter.core.guarantee.domain.port.in.GetGuaranteesUseCase;
import com.sofka.insurancequoter.core.guarantee.infrastructure.adapter.in.rest.dto.GuaranteeDto;
import com.sofka.insurancequoter.core.guarantee.infrastructure.adapter.in.rest.dto.GuaranteesResponse;
import com.sofka.insurancequoter.core.guarantee.infrastructure.adapter.in.rest.mapper.GuaranteeRestMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuaranteeControllerTest {

    @Mock
    private GetGuaranteesUseCase getGuaranteesUseCase;

    @Mock
    private GuaranteeRestMapper guaranteeRestMapper;

    @InjectMocks
    private GuaranteeController controller;

    @Test
    void getGuarantees_returnsHttp200WithGuaranteeList() {
        // GIVEN
        List<Guarantee> guarantees = List.of(
                new Guarantee("GUA-FIRE", "Incendio edificios", true)
        );
        GuaranteesResponse response = new GuaranteesResponse(
                List.of(new GuaranteeDto("GUA-FIRE", "Incendio edificios", true))
        );
        when(getGuaranteesUseCase.getAll()).thenReturn(guarantees);
        when(guaranteeRestMapper.toResponse(guarantees)).thenReturn(response);

        // WHEN
        ResponseEntity<GuaranteesResponse> result = controller.getGuarantees();

        // THEN
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().guarantees()).hasSize(1);
        assertThat(result.getBody().guarantees().get(0).code()).isEqualTo("GUA-FIRE");
        assertThat(result.getBody().guarantees().get(0).tarifable()).isTrue();
    }

    @Test
    void getGuarantees_delegatesToUseCaseAndMapper() {
        // GIVEN
        List<Guarantee> guarantees = List.of();
        when(getGuaranteesUseCase.getAll()).thenReturn(guarantees);
        when(guaranteeRestMapper.toResponse(guarantees)).thenReturn(new GuaranteesResponse(List.of()));

        // WHEN
        controller.getGuarantees();

        // THEN
        verify(getGuaranteesUseCase, times(1)).getAll();
        verify(guaranteeRestMapper, times(1)).toResponse(guarantees);
        verifyNoMoreInteractions(getGuaranteesUseCase, guaranteeRestMapper);
    }

    @Test
    void getGuarantees_returnsHttp200WithEmptyListWhenCatalogIsEmpty() {
        // GIVEN
        List<Guarantee> guarantees = List.of();
        GuaranteesResponse response = new GuaranteesResponse(List.of());
        when(getGuaranteesUseCase.getAll()).thenReturn(guarantees);
        when(guaranteeRestMapper.toResponse(guarantees)).thenReturn(response);

        // WHEN
        ResponseEntity<GuaranteesResponse> result = controller.getGuarantees();

        // THEN
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().guarantees()).isEmpty();
    }
}
