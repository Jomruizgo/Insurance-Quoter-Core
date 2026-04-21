package com.sofka.insurancequoter.core.folio.infrastructure.adapter.out.persistence.repositories;

import com.sofka.insurancequoter.core.folio.infrastructure.adapter.out.persistence.entities.FolioSequenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Spring Data repository that executes the native nextval call.
 * The nativeQuery delegates atomicity and concurrency guarantees to PostgreSQL.
 */
public interface FolioRepository extends JpaRepository<FolioSequenceEntity, Long> {

    @Query(value = "SELECT nextval('folio_sequence')", nativeQuery = true)
    Long nextValue();
}
