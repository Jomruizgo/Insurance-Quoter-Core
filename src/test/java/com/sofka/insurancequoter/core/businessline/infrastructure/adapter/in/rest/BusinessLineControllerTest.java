package com.sofka.insurancequoter.core.businessline.infrastructure.adapter.in.rest;

import com.sofka.insurancequoter.core.businessline.domain.model.BusinessLine;
import com.sofka.insurancequoter.core.businessline.domain.port.in.GetBusinessLinesUseCase;
import com.sofka.insurancequoter.core.businessline.infrastructure.adapter.in.rest.dto.BusinessLineDto;
import com.sofka.insurancequoter.core.businessline.infrastructure.adapter.in.rest.dto.BusinessLinesResponse;
import com.sofka.insurancequoter.core.businessline.infrastructure.adapter.in.rest.mapper.BusinessLineRestMapper;
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
class BusinessLineControllerTest {

    @Mock
    private GetBusinessLinesUseCase getBusinessLinesUseCase;

    @Mock
    private BusinessLineRestMapper businessLineRestMapper;

    @InjectMocks
    private BusinessLineController controller;

    @Test
    void getBusinessLines_returnsHttp200WithBusinessLineList() {
        // GIVEN
        List<BusinessLine> businessLines = List.of(
                new BusinessLine("BL-001", "Bodega de mercancías", "FK-INC-01")
        );
        BusinessLinesResponse response = new BusinessLinesResponse(
                List.of(new BusinessLineDto("BL-001", "Bodega de mercancías", "FK-INC-01"))
        );
        when(getBusinessLinesUseCase.getAll()).thenReturn(businessLines);
        when(businessLineRestMapper.toResponse(businessLines)).thenReturn(response);

        // WHEN
        ResponseEntity<BusinessLinesResponse> result = controller.getBusinessLines();

        // THEN
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().businessLines()).hasSize(1);
        assertThat(result.getBody().businessLines().get(0).code()).isEqualTo("BL-001");
        assertThat(result.getBody().businessLines().get(0).fireKey()).isEqualTo("FK-INC-01");
    }

    @Test
    void getBusinessLines_delegatesToUseCaseAndMapper() {
        // GIVEN
        List<BusinessLine> businessLines = List.of();
        when(getBusinessLinesUseCase.getAll()).thenReturn(businessLines);
        when(businessLineRestMapper.toResponse(businessLines)).thenReturn(new BusinessLinesResponse(List.of()));

        // WHEN
        controller.getBusinessLines();

        // THEN
        verify(getBusinessLinesUseCase, times(1)).getAll();
        verify(businessLineRestMapper, times(1)).toResponse(businessLines);
        verifyNoMoreInteractions(getBusinessLinesUseCase, businessLineRestMapper);
    }
}
