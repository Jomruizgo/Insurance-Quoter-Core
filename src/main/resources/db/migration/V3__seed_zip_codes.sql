-- Seed data for zip_codes catalog.
-- At least 5 representative postal codes from different states and zones.
INSERT INTO zip_codes (zip_code, state, municipality, city, catastrophic_zone, tev_zone, fhm_zone) VALUES
    ('06600', 'Ciudad de México', 'Cuauhtémoc',      'Ciudad de México', 'ZONE_A', 'TEV-1', 'FHM-2'),
    ('44100', 'Jalisco',          'Guadalajara',      'Guadalajara',      'ZONE_B', 'TEV-2', 'FHM-1'),
    ('64000', 'Nuevo León',       'Monterrey',        'Monterrey',        'ZONE_C', 'TEV-3', 'FHM-3'),
    ('72000', 'Puebla',           'Puebla',           'Puebla',           'ZONE_B', 'TEV-2', 'FHM-2'),
    ('20000', 'Aguascalientes',   'Aguascalientes',   'Aguascalientes',   'ZONE_D', 'TEV-4', 'FHM-1');

-- Neighborhoods for CDMX zip code
INSERT INTO zip_code_neighborhoods (zip_code, neighborhood) VALUES
    ('06600', 'Juárez'),
    ('06600', 'Tabacalera');

-- Neighborhoods for Guadalajara zip code
INSERT INTO zip_code_neighborhoods (zip_code, neighborhood) VALUES
    ('44100', 'Centro');

-- Neighborhoods for Monterrey zip code
INSERT INTO zip_code_neighborhoods (zip_code, neighborhood) VALUES
    ('64000', 'Centro');
