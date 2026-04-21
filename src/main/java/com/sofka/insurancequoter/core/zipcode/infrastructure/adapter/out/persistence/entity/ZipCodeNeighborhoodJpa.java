package com.sofka.insurancequoter.core.zipcode.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity mapping the zip_code_neighborhoods table.
 * Never exposed outside the persistence adapter.
 */
@Entity
@Table(name = "zip_code_neighborhoods")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZipCodeNeighborhoodJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zip_code", nullable = false)
    private ZipCodeJpa zipCode;

    @Column(name = "neighborhood", nullable = false, length = 150)
    private String neighborhood;
}
