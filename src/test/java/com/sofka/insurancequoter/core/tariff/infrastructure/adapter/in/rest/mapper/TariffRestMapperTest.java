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
        Tariffs domain = new Tariffs(0.0015, 0.0008, 0.0005, 0.003, 0.002);

        // WHEN
        TariffsResponse response = mapper.toResponse(domain);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.tariffs()).isNotNull();
        assertThat(response.tariffs().fireRate()).isEqualTo(0.0015);
        assertThat(response.tariffs().cattevFactor()).isEqualTo(0.0008);
        assertThat(response.tariffs().catfhmFactor()).isEqualTo(0.0005);
        assertThat(response.tariffs().theftRate()).isEqualTo(0.003);
        assertThat(response.tariffs().electronicEquipmentRate()).isEqualTo(0.002);
    }

    @Test
    void toDomain_mapsAllFieldsFromRequest() {
        // GIVEN
        UpdateTariffsRequest request = new UpdateTariffsRequest(0.002, 0.001, 0.0007, 0.004, 0.003);

        // WHEN
        Tariffs domain = mapper.toDomain(request);

        // THEN
        assertThat(domain).isNotNull();
        assertThat(domain.fireRate()).isEqualTo(0.002);
        assertThat(domain.cattevFactor()).isEqualTo(0.001);
        assertThat(domain.catfhmFactor()).isEqualTo(0.0007);
        assertThat(domain.theftRate()).isEqualTo(0.004);
        assertThat(domain.electronicEquipmentRate()).isEqualTo(0.003);
    }
}
