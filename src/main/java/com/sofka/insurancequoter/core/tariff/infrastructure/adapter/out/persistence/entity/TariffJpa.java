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
 * Contains all 15 rate/factor columns required for full premium calculation.
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
    private double fireContentsRate;
    private double coverageExtensionFactor;
    private double cattevFactor;
    private double catfhmFactor;
    private double debrisRemovalFactor;
    private double extraordinaryExpensesFactor;
    private double rentalLossRate;
    private double businessInterruptionRate;
    private double electronicEquipmentRate;
    private double theftRate;
    private double cashAndValuesRate;
    private double glassRate;
    private double luminousSignageRate;
    private double commercialFactor;
}
