package com.sofka.insurancequoter.core.folio.infrastructure.adapter.in.rest.mapper;

import com.sofka.insurancequoter.core.folio.domain.model.Folio;
import com.sofka.insurancequoter.core.folio.infrastructure.adapter.in.rest.dto.FolioResponse;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class FolioRestMapperTest {

    private final FolioRestMapper mapper = new FolioRestMapper();

    @Test
    void toResponse_mapsFollioNumberAndGeneratedAt() {
        Instant now = Instant.parse("2026-04-21T10:00:00Z");
        Folio folio = new Folio("FOL-2026-00043", now);

        FolioResponse response = mapper.toResponse(folio);

        assertThat(response.folioNumber()).isEqualTo("FOL-2026-00043");
        assertThat(response.generatedAt()).isEqualTo(now);
    }
}
