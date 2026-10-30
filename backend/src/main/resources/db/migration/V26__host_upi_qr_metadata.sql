-- V26 stores QR metadata, not image bytes. Uploaded images must be validated and
-- stored through a protected asset service before any metadata is persisted.
-- QR match != verified account ownership; no wallet credit from direct QR scans.
CREATE TABLE host_upi_qr_assets (
    id UUID PRIMARY KEY,
    host_id UUID NOT NULL REFERENCES users(id),
    upi_account_id UUID NOT NULL REFERENCES host_upi_accounts(id),
    source VARCHAR(16) NOT NULL CHECK (source IN ('UPLOADED', 'GENERATED')),
    storage_key VARCHAR(255),
    payload_sha256 VARCHAR(64) NOT NULL CHECK (payload_sha256 ~ '^[0-9a-f]{64}$'),
    verified_payee_match BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_host_upi_qr_source_storage CHECK (
      (source='UPLOADED' AND storage_key IS NOT NULL) OR
      (source='GENERATED' AND storage_key IS NULL)
    ),
    CONSTRAINT chk_host_upi_qr_active CHECK (NOT active OR verified_payee_match)
);
CREATE INDEX idx_host_upi_qr_assets_account ON host_upi_qr_assets(upi_account_id);
CREATE UNIQUE INDEX ux_host_upi_qr_active ON host_upi_qr_assets(upi_account_id) WHERE active;
