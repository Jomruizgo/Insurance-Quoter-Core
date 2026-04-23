-- DDL for zip codes catalog.
-- zip_codes is a read-only lookup table; no write endpoints exist at runtime.
CREATE TABLE zip_codes (
    zip_code          VARCHAR(10)  PRIMARY KEY,
    state             VARCHAR(100) NOT NULL,
    municipality      VARCHAR(100) NOT NULL,
    city              VARCHAR(100) NOT NULL,
    catastrophic_zone VARCHAR(20)  NOT NULL,
    tev_zone          VARCHAR(20)  NOT NULL,
    fhm_zone          VARCHAR(20)  NOT NULL
);

-- Neighborhoods are stored separately to support 0..N per zip code.
CREATE TABLE zip_code_neighborhoods (
    id           BIGSERIAL    PRIMARY KEY,
    zip_code     VARCHAR(10)  NOT NULL REFERENCES zip_codes(zip_code),
    neighborhood VARCHAR(150) NOT NULL
);

-- Index to avoid full-table scan when loading neighborhoods for a given zip code.
CREATE INDEX idx_zip_code_neighborhoods_zip_code ON zip_code_neighborhoods(zip_code);
