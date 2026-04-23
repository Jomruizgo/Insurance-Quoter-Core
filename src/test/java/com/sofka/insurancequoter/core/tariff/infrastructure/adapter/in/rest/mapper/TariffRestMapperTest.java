package com.sofka.insurancequoter.core.tariff.infrastructure.adapter.in.rest.mapper;

import com.sofka.insurancequoter.core.tariff.domain.model.Tariffs;
import com.sofka.insurancequoter.core.tariff.infrastructure.adapter.in.rest.dto.TariffsResponse;
import com.sofka.insurancequoter.core.tariff.infrastructure.adapter.in.rest.dto.UpdateTariffsRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TariffRestMapperTest {

    private TariffRestMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TariffRestMapper();
    }

    @Test
    void toResponse_mapsAllFieldsCorrectly() {
        // GIVEN
        Tariffs domain = new Tariffs(
                0.0015, 0.0012, 0.07, 0.0008, 0.0005,
                0.03, 0.02, 0.015, 0.015, 0.002,
                0.003, 0.005, 0.001, 0.002, 1.16
        );

        // WHEN
        TariffsResponse response = mapper.toResponse(domain);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.tariffs()).isNotNull();
        assertThat(response.tariffs().fireRate()).isEqualTo(0.0015);
        assertThat(response.tariffs().fireContentsRate()).isEqualTo(0.0012);
        assertThat(response.tariffs().coverageExtensionFactor()).isEqualTo(0.07);
        assertThat(response.tariffs().cattevFactor()).isEqualTo(0.0008);
        assertThat(response.tariffs().catfhmFactor()).isEqualTo(0.0005);
        assertThat(response.tariffs().debrisRemovalFactor()).isEqualTo(0.03);
        assertThat(response.tariffs().extraordinaryExpensesFactor()).isEqualTo(0.02);
        assertThat(response.tariffs().rentalLossRate()).isEqualTo(0.015);
        assertThat(response.tariffs().businessInterruptionRate()).isEqualTo(0.015);
        assertThat(response.tariffs().electronicEquipmentRate()).isEqualTo(0.002);
        assertThat(response.tariffs().theftRate()).isEqualTo(0.003);
        assertThat(response.tariffs().cashAndValuesRate()).isEqualTo(0.005);
        assertThat(response.tariffs().glassRate()).isEqualTo(0.001);
        assertThat(response.tariffs().luminousSignageRate()).isEqualTo(0.002);
        assertThat(response.tariffs().commercialFactor()).isEqualTo(1.16);
    }

    @Test
    void toDomain_mapsAllFieldsFromRequest() {
        // GIVEN
        UpdateTariffsRequest request = new UpdateTariffsRequest(
                0.002, 0.0015, 0.08, 0.001, 0.0007,
                0.04, 0.025, 0.02, 0.02, 0.003,
                0.004, 0.006, 0.0015, 0.0025, 1.20
        );

        // WHEN
        Tariffs domain = mapper.toDomain(request);

        // THEN
        assertThat(domain).isNotNull();
        assertThat(domain.fireRate()).isEqualTo(0.002);
        assertThat(domain.fireContentsRate()).isEqualTo(0.0015);
        assertThat(domain.coverageExtensionFactor()).isEqualTo(0.08);
        assertThat(domain.cattevFactor()).isEqualTo(0.001);
        assertThat(domain.catfhmFactor()).isEqualTo(0.0007);
        assertThat(domain.debrisRemovalFactor()).isEqualTo(0.04);
        assertThat(domain.extraordinaryExpensesFactor()).isEqualTo(0.025);
        assertThat(domain.rentalLossRate()).isEqualTo(0.02);
        assertThat(domain.businessInterruptionRate()).isEqualTo(0.02);
        assertThat(domain.electronicEquipmentRate()).isEqualTo(0.003);
        assertThat(domain.theftRate()).isEqualTo(0.004);
        assertThat(domain.cashAndValuesRate()).isEqualTo(0.006);
        assertThat(domain.glassRate()).isEqualTo(0.0015);
        assertThat(domain.luminousSignageRate()).isEqualTo(0.0025);
        assertThat(domain.commercialFactor()).isEqualTo(1.20);
    }
}
