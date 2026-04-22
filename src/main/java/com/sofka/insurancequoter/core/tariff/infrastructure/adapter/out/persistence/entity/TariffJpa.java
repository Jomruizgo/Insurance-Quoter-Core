package com.sofka.insurancequoter.core.tariff.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity for the single-row tariffs catalog.
 * Always persisted with id = 1.
 */
@Entity
@Table(name = "tariffs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TariffJpa {

    @Id
    private Long id;

    private double fireRate;
    private double cattevFactor;
    private double catfhmFactor;
    private double theftRate;
    private double electronicEquipmentRate;
}
