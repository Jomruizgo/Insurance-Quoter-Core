package com.sofka.insurancequoter.core.tariff.infrastructure.adapter.in.rest;

import com.sofka.insurancequoter.core.tariff.domain.exception.TariffNotFoundException;
import com.sofka.insurancequoter.core.tariff.domain.model.Tariffs;
import com.sofka.insurancequoter.core.tariff.domain.port.in.GetTariffsUseCase;
import com.sofka.insurancequoter.core.tariff.domain.port.in.UpdateTariffsUseCase;
import com.sofka.insurancequoter.core.tariff.infrastructure.adapter.in.rest.dto.TariffsDto;
import com.sofka.insurancequoter.core.tariff.infrastructure.adapter.in.rest.dto.TariffsResponse;
import com.sofka.insurancequoter.core.tariff.infrastructure.adapter.in.rest.dto.UpdateTariffsRequest;
import com.sofka.insurancequoter.core.tariff.infrastructure.adapter.in.rest.mapper.TariffRestMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TariffControllerTest {

    @Mock
    private GetTariffsUseCase getTariffsUseCase;

    @Mock
    private UpdateTariffsUseCase updateTariffsUseCase;

    @Mock
    private TariffRestMapper mapper;

    @InjectMocks
    private TariffController controller;

    // ---- GET /v1/tariffs ----

    @Test
    void getTariffs_whenExists_returns200WithBody() {
        // GIVEN
        Tariffs domain = new Tariffs(0.0015, 0.0008, 0.0005, 0.003, 0.002);
        TariffsDto dto = new TariffsDto(0.0015, 0.0008, 0.0005, 0.003, 0.002);
        TariffsResponse response = new TariffsResponse(dto);

        when(getTariffsUseCase.getCurrent()).thenReturn(domain);
        when(mapper.toResponse(domain)).thenReturn(response);

        // WHEN
        ResponseEntity<TariffsResponse> result = controller.getTariffs();

        // THEN
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().tariffs().fireRate()).isEqualTo(0.0015);
        verify(getTariffsUseCase).getCurrent();
        verify(mapper).toResponse(domain);
    }

    // ---- PUT /v1/tariffs ----

    @Test
    void updateTariffs_whenValid_returns200WithUpdatedBody() {
        // GIVEN
        UpdateTariffsRequest request = new UpdateTariffsRequest(0.002, 0.001, 0.0007, 0.004, 0.003);
        Tariffs domain = new Tariffs(0.002, 0.001, 0.0007, 0.004, 0.003);
        TariffsDto dto = new TariffsDto(0.002, 0.001, 0.0007, 0.004, 0.003);
        TariffsResponse response = new TariffsResponse(dto);

        when(mapper.toDomain(request)).thenReturn(domain);
        when(updateTariffsUseCase.update(domain)).thenReturn(domain);
        when(mapper.toResponse(domain)).thenReturn(response);

        // WHEN
        ResponseEntity<TariffsResponse> result = controller.updateTariffs(request);

        // THEN
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().tariffs().fireRate()).isEqualTo(0.002);
        verify(mapper).toDomain(request);
        verify(updateTariffsUseCase).update(domain);
        verify(mapper).toResponse(domain);
    }

    // ---- @ExceptionHandler ----

    @Test
    void handleNotFound_returns404WithErrorBody() {
        // GIVEN — test the handler directly (Mockito-only, no Spring dispatcher)

        // WHEN
        ResponseEntity<Map<String, String>> result = controller.handleNotFound();

        // THEN
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().get("error")).isEqualTo("Tariffs not found");
    }
}
