package com.sofka.insurancequoter.core.zipcode.infrastructure.adapter.out.persistence;

import com.sofka.insurancequoter.core.zipcode.infrastructure.adapter.out.persistence.entity.ZipCodeJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Spring Data JPA repository for zip codes.
 * Uses JOIN FETCH to load neighborhoods in a single query and avoid N+1.
 */
public interface ZipCodeJpaRepository extends JpaRepository<ZipCodeJpa, String> {

    @Query("SELECT z FROM ZipCodeJpa z LEFT JOIN FETCH z.neighborhoods WHERE z.zipCode = :zipCode")
    Optional<ZipCodeJpa> findByZipCodeWithNeighborhoods(@Param("zipCode") String zipCode);
}
