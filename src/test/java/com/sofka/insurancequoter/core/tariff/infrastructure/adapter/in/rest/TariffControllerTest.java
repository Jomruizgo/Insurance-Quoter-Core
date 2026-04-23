package com.sofka.insurancequoter.core.tariff.infrastructure.adapter.in.rest;

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

    private static final Tariffs DOMAIN = new Tariffs(
            0.0015, 0.0012, 0.07, 0.0008, 0.0005,
            0.03, 0.02, 0.015, 0.015, 0.002,
            0.003, 0.005, 0.001, 0.002, 1.16
    );

    private static final TariffsDto DTO = new TariffsDto(
            0.0015, 0.0012, 0.07, 0.0008, 0.0005,
            0.03, 0.02, 0.015, 0.015, 0.002,
            0.003, 0.005, 0.001, 0.002, 1.16
    );

    // ---- GET /v1/tariffs ----

    @Test
    void getTariffs_whenExists_returns200WithBody() {
        // GIVEN
        TariffsResponse response = new TariffsResponse(DTO);
        when(getTariffsUseCase.getCurrent()).thenReturn(DOMAIN);
        when(mapper.toResponse(DOMAIN)).thenReturn(response);

        // WHEN
        ResponseEntity<TariffsResponse> result = controller.getTariffs();

        // THEN
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().tariffs().fireRate()).isEqualTo(0.0015);
        assertThat(result.getBody().tariffs().commercialFactor()).isEqualTo(1.16);
        verify(getTariffsUseCase).getCurrent();
        verify(mapper).toResponse(DOMAIN);
    }

    // ---- PUT /v1/tariffs ----

    @Test
    void updateTariffs_whenValid_returns200WithUpdatedBody() {
        // GIVEN
        UpdateTariffsRequest request = new UpdateTariffsRequest(
                0.002, 0.0015, 0.08, 0.001, 0.0007,
                0.04, 0.025, 0.02, 0.02, 0.003,
                0.004, 0.006, 0.0015, 0.0025, 1.20
        );
        Tariffs updated = new Tariffs(
                0.002, 0.0015, 0.08, 0.001, 0.0007,
                0.04, 0.025, 0.02, 0.02, 0.003,
                0.004, 0.006, 0.0015, 0.0025, 1.20
        );
        TariffsDto updatedDto = new TariffsDto(
                0.002, 0.0015, 0.08, 0.001, 0.0007,
                0.04, 0.025, 0.02, 0.02, 0.003,
                0.004, 0.006, 0.0015, 0.0025, 1.20
        );
        TariffsResponse response = new TariffsResponse(updatedDto);

        when(mapper.toDomain(request)).thenReturn(updated);
        when(updateTariffsUseCase.update(updated)).thenReturn(updated);
        when(mapper.toResponse(updated)).thenReturn(response);

        // WHEN
        ResponseEntity<TariffsResponse> result = controller.updateTariffs(request);

        // THEN
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().tariffs().fireRate()).isEqualTo(0.002);
        assertThat(result.getBody().tariffs().commercialFactor()).isEqualTo(1.20);
        verify(mapper).toDomain(request);
        verify(updateTariffsUseCase).update(updated);
        verify(mapper).toResponse(updated);
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
