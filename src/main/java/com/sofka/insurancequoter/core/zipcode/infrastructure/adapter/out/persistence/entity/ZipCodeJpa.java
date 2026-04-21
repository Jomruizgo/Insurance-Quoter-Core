package com.sofka.insurancequoter.core.zipcode.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * JPA entity mapping the zip_codes table.
 * Never exposed outside the persistence adapter — map to domain model before returning.
 */
@Entity
@Table(name = "zip_codes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZipCodeJpa {

    @Id
    @Column(name = "zip_code", nullable = false, length = 10)
    private String zipCode;

    @Column(name = "state", nullable = false, length = 100)
    private String state;

    @Column(name = "municipality", nullable = false, length = 100)
    private String municipality;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "catastrophic_zone", nullable = false, length = 20)
    private String catastrophicZone;

    @Column(name = "tev_zone", nullable = false, length = 20)
    private String tevZone;

    @Column(name = "fhm_zone", nullable = false, length = 20)
    private String fhmZone;

    @OneToMany(mappedBy = "zipCode", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ZipCodeNeighborhoodJpa> neighborhoods;
}
