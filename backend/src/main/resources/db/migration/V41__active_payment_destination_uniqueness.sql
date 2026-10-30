-- Batch 5J.1: allow archived payment destinations to be re-registered.
-- Historical rows remain intact for audit and reconciliation.
-- Active destinations must remain unique per host.

ALTER TABLE host_upi_accounts
    DROP CONSTRAINT IF EXISTS ux_host_upi_fingerprint;

DROP INDEX IF EXISTS ux_host_upi_fingerprint;

CREATE UNIQUE INDEX ux_host_upi_fingerprint
    ON host_upi_accounts (host_id, upi_id_fingerprint)
    WHERE archived_at IS NULL;

ALTER TABLE host_bank_accounts
    DROP CONSTRAINT IF EXISTS ux_host_bank_fingerprint;

DROP INDEX IF EXISTS ux_host_bank_fingerprint;

CREATE UNIQUE INDEX ux_host_bank_fingerprint
    ON host_bank_accounts (host_id, account_fingerprint)
    WHERE archived_at IS NULL;
