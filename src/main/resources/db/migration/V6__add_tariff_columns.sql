-- V6: Adds 10 new columns to the tariffs table required for full premium calculation
ALTER TABLE tariffs
    ADD COLUMN fire_contents_rate          DOUBLE PRECISION NOT NULL DEFAULT 0.0012,
    ADD COLUMN coverage_extension_factor   DOUBLE PRECISION NOT NULL DEFAULT 0.07,
    ADD COLUMN debris_removal_factor       DOUBLE PRECISION NOT NULL DEFAULT 0.03,
    ADD COLUMN extraordinary_expenses_factor DOUBLE PRECISION NOT NULL DEFAULT 0.02,
    ADD COLUMN rental_loss_rate            DOUBLE PRECISION NOT NULL DEFAULT 0.015,
    ADD COLUMN business_interruption_rate  DOUBLE PRECISION NOT NULL DEFAULT 0.015,
    ADD COLUMN cash_and_values_rate        DOUBLE PRECISION NOT NULL DEFAULT 0.005,
    ADD COLUMN glass_rate                  DOUBLE PRECISION NOT NULL DEFAULT 0.001,
    ADD COLUMN luminous_signage_rate       DOUBLE PRECISION NOT NULL DEFAULT 0.002,
    ADD COLUMN commercial_factor           DOUBLE PRECISION NOT NULL DEFAULT 1.16;
