package com.sofka.insurancequoter.core.folio.domain.port.out;

/**
 * Output Port — abstracts the mechanism used to obtain the next folio number.
 * Implemented by FolioSequenceJpaAdapter in the infrastructure layer.
 */
public interface FolioSequencePort {

    String nextFolioNumber();
}
