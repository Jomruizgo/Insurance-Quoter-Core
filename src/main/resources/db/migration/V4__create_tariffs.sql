-- V4: Creates the tariffs table with a single-row pattern (id = 1)
CREATE TABLE tariffs (
    id                        BIGINT PRIMARY KEY DEFAULT 1,
    fire_rate                 DOUBLE PRECISION NOT NULL CHECK (fire_rate > 0),
    cattev_factor             DOUBLE PRECISION NOT NULL CHECK (cattev_factor > 0),
    catfhm_factor             DOUBLE PRECISION NOT NULL CHECK (catfhm_factor > 0),
    theft_rate                DOUBLE PRECISION NOT NULL CHECK (theft_rate > 0),
    electronic_equipment_rate DOUBLE PRECISION NOT NULL CHECK (electronic_equipment_rate > 0)
);
