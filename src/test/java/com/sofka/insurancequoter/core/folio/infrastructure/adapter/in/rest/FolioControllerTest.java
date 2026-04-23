package com.sofka.insurancequoter.core.folio.infrastructure.adapter.in.rest;

import com.sofka.insurancequoter.core.folio.domain.model.Folio;
import com.sofka.insurancequoter.core.folio.domain.port.in.GenerateFolioUseCase;
import com.sofka.insurancequoter.core.folio.infrastructure.adapter.in.rest.dto.FolioResponse;
import com.sofka.insurancequoter.core.folio.infrastructure.adapter.in.rest.mapper.FolioRestMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FolioControllerTest {

    @Mock
    private GenerateFolioUseCase generateFolioUseCase;

    @Mock
    private FolioRestMapper folioRestMapper;

    @InjectMocks
    private FolioController controller;

    @Test
    void getFolio_returnsHttp200WithFolioNumberAndGeneratedAt() {
        Instant now = Instant.parse("2026-04-21T10:00:00Z");
        Folio folio = new Folio("FOL-2026-00001", now);
        FolioResponse response = new FolioResponse("FOL-2026-00001", now);
        when(generateFolioUseCase.generate()).thenReturn(folio);
        when(folioRestMapper.toResponse(folio)).thenReturn(response);

        ResponseEntity<FolioResponse> result = controller.getFolio();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().folioNumber()).isEqualTo("FOL-2026-00001");
        assertThat(result.getBody().generatedAt()).isEqualTo(now);
    }

    @Test
    void getFolio_delegatesToUseCaseAndMapper() {
        Instant now = Instant.parse("2026-04-21T10:00:00Z");
        Folio folio = new Folio("FOL-2026-00001", now);
        when(generateFolioUseCase.generate()).thenReturn(folio);
        when(folioRestMapper.toResponse(folio)).thenReturn(new FolioResponse("FOL-2026-00001", now));

        controller.getFolio();

        verify(generateFolioUseCase, times(1)).generate();
        verify(folioRestMapper, times(1)).toResponse(folio);
        verifyNoMoreInteractions(generateFolioUseCase, folioRestMapper);
    }
}
