package com.sofka.insurancequoter.core.zipcode.infrastructure.adapter.in.rest.mapper;

import com.sofka.insurancequoter.core.zipcode.domain.model.ZipCode;
import com.sofka.insurancequoter.core.zipcode.domain.model.ZipCodeValidationResult;
import com.sofka.insurancequoter.core.zipcode.infrastructure.adapter.in.rest.dto.ZipCodeResponse;
import com.sofka.insurancequoter.core.zipcode.infrastructure.adapter.in.rest.dto.ZipCodeValidationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ZipCodeRestMapperTest {

    private ZipCodeRestMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ZipCodeRestMapper();
    }

    @Test
    void toResponse_mapsAllFieldsAndSetsValidTrue() {
        // GIVEN
        ZipCode zipCode = new ZipCode(
                "06600", "Ciudad de México", "Cuauhtémoc", "Ciudad de México",
                List.of("Juárez", "Tabacalera"), "ZONE_A", "TEV-1", "FHM-2"
        );

        // WHEN
        ZipCodeResponse response = mapper.toResponse(zipCode);

        // THEN
        assertThat(response.zipCode()).isEqualTo("06600");
        assertThat(response.state()).isEqualTo("Ciudad de México");
        assertThat(response.municipality()).isEqualTo("Cuauhtémoc");
        assertThat(response.city()).isEqualTo("Ciudad de México");
        assertThat(response.neighborhoods()).containsExactly("Juárez", "Tabacalera");
        assertThat(response.catastrophicZone()).isEqualTo("ZONE_A");
        assertThat(response.tevZone()).isEqualTo("TEV-1");
        assertThat(response.fhmZone()).isEqualTo("FHM-2");
        assertThat(response.valid()).isTrue();
    }

    @Test
    void toValidationResponse_whenValid_mapsCorrectly() {
        // GIVEN
        ZipCodeValidationResult result = new ZipCodeValidationResult(true, "06600");

        // WHEN
        ZipCodeValidationResponse response = mapper.toValidationResponse(result);

        // THEN
        assertThat(response.valid()).isTrue();
        assertThat(response.zipCode()).isEqualTo("06600");
    }

    @Test
    void toValidationResponse_whenInvalid_mapsCorrectly() {
        // GIVEN
        ZipCodeValidationResult result = new ZipCodeValidationResult(false, "99999");

        // WHEN
        ZipCodeValidationResponse response = mapper.toValidationResponse(result);

        // THEN
        assertThat(response.valid()).isFalse();
        assertThat(response.zipCode()).isEqualTo("99999");
    }
}
