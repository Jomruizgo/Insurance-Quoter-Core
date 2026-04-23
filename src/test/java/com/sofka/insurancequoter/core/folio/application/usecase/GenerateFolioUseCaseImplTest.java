package com.sofka.insurancequoter.core.folio.application.usecase;

import com.sofka.insurancequoter.core.folio.domain.model.Folio;
import com.sofka.insurancequoter.core.folio.domain.port.out.FolioSequencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerateFolioUseCaseImplTest {

    @Mock
    private FolioSequencePort folioSequencePort;

    private Clock fixedClock;
    private GenerateFolioUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2026-04-21T10:00:00Z"), ZoneOffset.UTC);
        useCase = new GenerateFolioUseCaseImpl(folioSequencePort, fixedClock);
    }

    @Test
    void generate_returnsFollioWithCorrectFormatAndTimestamp() {
        when(folioSequencePort.nextFolioNumber()).thenReturn("FOL-2026-00001");

        Folio result = useCase.generate();

        assertThat(result.folioNumber()).isEqualTo("FOL-2026-00001");
        assertThat(result.generatedAt()).isEqualTo(Instant.parse("2026-04-21T10:00:00Z"));
    }

    @Test
    void generate_delegatesToSequencePortExactlyOnce() {
        when(folioSequencePort.nextFolioNumber()).thenReturn("FOL-2026-00042");

        useCase.generate();

        verify(folioSequencePort, times(1)).nextFolioNumber();
        verifyNoMoreInteractions(folioSequencePort);
    }
}
