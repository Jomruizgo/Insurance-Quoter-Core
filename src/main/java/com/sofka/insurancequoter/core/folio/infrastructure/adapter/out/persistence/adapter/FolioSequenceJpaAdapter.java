package com.sofka.insurancequoter.core.folio.infrastructure.adapter.out.persistence.adapter;

import com.sofka.insurancequoter.core.folio.domain.port.out.FolioSequencePort;
import com.sofka.insurancequoter.core.folio.infrastructure.adapter.out.persistence.repositories.FolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Output Adapter — implements FolioSequencePort using Spring Data JPA.
 * Calls PostgreSQL nextval via FolioRepository and formats the result
 * as FOL-<YYYY>-<NNNNN> using the UTC year from the injected clock.
 */
@Component
@RequiredArgsConstructor
public class FolioSequenceJpaAdapter implements FolioSequencePort {

    private final FolioRepository folioRepository;
    private final Clock clock;

    @Override
    public String nextFolioNumber() {
        long seq = folioRepository.nextValue();
        int year = LocalDate.now(clock).getYear();
        return String.format("FOL-%d-%05d", year, seq);
    }
}
