-- Creates the native PostgreSQL sequence used to generate unique folio numbers.
-- nextval('folio_sequence') is atomic and concurrency-safe — no application-level locks needed.
CREATE SEQUENCE IF NOT EXISTS folio_sequence
    START WITH 1
    INCREMENT BY 1
    NO CYCLE;

-- Auxiliary control table required to anchor the JPA entity that executes the nativeQuery.
-- It holds a single dummy row; it does NOT store folios.
CREATE TABLE IF NOT EXISTS folio_sequence_ctrl (
    id BIGINT PRIMARY KEY DEFAULT 1
);

INSERT INTO folio_sequence_ctrl VALUES (1) ON CONFLICT DO NOTHING;
