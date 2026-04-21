package com.sofka.insurancequoter.core.folio.infrastructure.adapter.out.persistence.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

/**
 * JPA entity anchored to folio_sequence_ctrl.
 * This table holds a single dummy row whose sole purpose is to allow
 * FolioRepository to execute SELECT nextval('folio_sequence') via a nativeQuery.
 * No folio data is stored here.
 */
@Entity
@Table(name = "folio_sequence_ctrl")
@Getter
public class FolioSequenceEntity {

    @Id
    private Long id;
}
