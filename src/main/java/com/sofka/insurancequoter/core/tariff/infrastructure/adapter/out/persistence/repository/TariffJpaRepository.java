package com.sofka.insurancequoter.core.tariff.infrastructure.adapter.out.persistence.repository;

import com.sofka.insurancequoter.core.tariff.infrastructure.adapter.out.persistence.entity.TariffJpa;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the tariffs table.
 * Only findById(1L) and save() are used — the table holds a single row.
 */
public interface TariffJpaRepository extends JpaRepository<TariffJpa, Long> {
}
