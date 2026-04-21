package com.sofka.insurancequoter.core.zipcode.infrastructure.adapter.out.persistence;

import com.sofka.insurancequoter.core.zipcode.domain.model.ZipCode;
import com.sofka.insurancequoter.core.zipcode.infrastructure.adapter.out.persistence.entity.ZipCodeJpa;
import com.sofka.insurancequoter.core.zipcode.infrastructure.adapter.out.persistence.entity.ZipCodeNeighborhoodJpa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ZipCodePersistenceAdapterTest {

    @Mock
    private ZipCodeJpaRepository zipCodeJpaRepository;

    private ZipCodePersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ZipCodePersistenceAdapter(zipCodeJpaRepository);
    }

    @Test
    void findByZipCode_whenFoundWithNeighborhoods_mapsCorrectly() {
        // GIVEN
        ZipCodeJpa jpa = buildJpaWithNeighborhoods("06600",
                List.of("Juárez", "Tabacalera"));
        when(zipCodeJpaRepository.findByZipCodeWithNeighborhoods("06600"))
                .thenReturn(Optional.of(jpa));

        // WHEN
        Optional<ZipCode> result = adapter.findByZipCode("06600");

        // THEN
        assertThat(result).isPresent();
        ZipCode zipCode = result.get();
        assertThat(zipCode.zipCode()).isEqualTo("06600");
        assertThat(zipCode.state()).isEqualTo("Ciudad de México");
        assertThat(zipCode.municipality()).isEqualTo("Cuauhtémoc");
        assertThat(zipCode.city()).isEqualTo("Ciudad de México");
        assertThat(zipCode.neighborhoods()).containsExactlyInAnyOrder("Juárez", "Tabacalera");
        assertThat(zipCode.catastrophicZone()).isEqualTo("ZONE_A");
        assertThat(zipCode.tevZone()).isEqualTo("TEV-1");
        assertThat(zipCode.fhmZone()).isEqualTo("FHM-2");
    }

    @Test
    void findByZipCode_whenFoundWithoutNeighborhoods_returnsEmptyNeighborhoodsList() {
        // GIVEN
        ZipCodeJpa jpa = buildJpaWithNeighborhoods("44100", List.of());
        when(zipCodeJpaRepository.findByZipCodeWithNeighborhoods("44100"))
                .thenReturn(Optional.of(jpa));

        // WHEN
        Optional<ZipCode> result = adapter.findByZipCode("44100");

        // THEN
        assertThat(result).isPresent();
        assertThat(result.get().neighborhoods()).isEmpty();
    }

    @Test
    void findByZipCode_whenNotFound_returnsEmpty() {
        // GIVEN
        when(zipCodeJpaRepository.findByZipCodeWithNeighborhoods("99999"))
                .thenReturn(Optional.empty());

        // WHEN
        Optional<ZipCode> result = adapter.findByZipCode("99999");

        // THEN
        assertThat(result).isEmpty();
    }

    // ---- helpers ----

    private ZipCodeJpa buildJpaWithNeighborhoods(String zipCode, List<String> neighborhoodNames) {
        ZipCodeJpa jpa = ZipCodeJpa.builder()
                .zipCode(zipCode)
                .state("Ciudad de México")
                .municipality("Cuauhtémoc")
                .city("Ciudad de México")
                .catastrophicZone("ZONE_A")
                .tevZone("TEV-1")
                .fhmZone("FHM-2")
                .build();

        List<ZipCodeNeighborhoodJpa> neighborhoods = neighborhoodNames.stream()
                .map(name -> ZipCodeNeighborhoodJpa.builder()
                        .zipCode(jpa)
                        .neighborhood(name)
                        .build())
                .toList();
        jpa.setNeighborhoods(neighborhoods);
        return jpa;
    }
}
