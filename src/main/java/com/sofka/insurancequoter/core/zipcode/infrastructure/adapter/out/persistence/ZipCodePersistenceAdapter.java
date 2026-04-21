package com.sofka.insurancequoter.core.zipcode.infrastructure.adapter.out.persistence;

import com.sofka.insurancequoter.core.zipcode.domain.model.ZipCode;
import com.sofka.insurancequoter.core.zipcode.domain.port.out.ZipCodeRepository;
import com.sofka.insurancequoter.core.zipcode.infrastructure.adapter.out.persistence.entity.ZipCodeJpa;
import com.sofka.insurancequoter.core.zipcode.infrastructure.adapter.out.persistence.entity.ZipCodeNeighborhoodJpa;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Persistence adapter — implements ZipCodeRepository output port using Spring Data JPA.
 * Maps ZipCodeJpa → ZipCode domain model before returning to the application layer.
 */
@Component
@RequiredArgsConstructor
public class ZipCodePersistenceAdapter implements ZipCodeRepository {

    private final ZipCodeJpaRepository zipCodeJpaRepository;

    @Override
    public Optional<ZipCode> findByZipCode(String zipCode) {
        return zipCodeJpaRepository.findByZipCodeWithNeighborhoods(zipCode)
                .map(this::toDomain);
    }

    private ZipCode toDomain(ZipCodeJpa jpa) {
        List<String> neighborhoods = jpa.getNeighborhoods() == null
                ? List.of()
                : jpa.getNeighborhoods().stream()
                        .map(ZipCodeNeighborhoodJpa::getNeighborhood)
                        .toList();

        return new ZipCode(
                jpa.getZipCode(),
                jpa.getState(),
                jpa.getMunicipality(),
                jpa.getCity(),
                neighborhoods,
                jpa.getCatastrophicZone(),
                jpa.getTevZone(),
                jpa.getFhmZone()
        );
    }
}
