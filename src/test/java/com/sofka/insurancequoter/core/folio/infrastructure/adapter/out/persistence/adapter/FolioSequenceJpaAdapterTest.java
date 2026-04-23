package com.sofka.insurancequoter.core.folio.infrastructure.adapter.out.persistence.adapter;

import com.sofka.insurancequoter.core.folio.infrastructure.adapter.out.persistence.repositories.FolioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FolioSequenceJpaAdapterTest {

    @Mock
    private FolioRepository folioRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private FolioSequenceJpaAdapter adapter;

    @Test
    void nextFolioNumber_formatsPatternFolYearFiveDigits() {
        Clock fixedClock = Clock.fixed(
                LocalDate.of(2026, 4, 21).atStartOfDay().toInstant(ZoneOffset.UTC),
                ZoneOffset.UTC);
        when(clock.getZone()).thenReturn(fixedClock.getZone());
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(folioRepository.nextValue()).thenReturn(43L);

        String result = adapter.nextFolioNumber();

        assertThat(result).isEqualTo("FOL-2026-00043");
    }
}
