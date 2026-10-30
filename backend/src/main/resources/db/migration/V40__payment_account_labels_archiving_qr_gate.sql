-- Account labels are host-private; archived rows remain for reconciliation and audit.
ALTER TABLE host_upi_accounts ADD COLUMN account_label VARCHAR(50);
ALTER TABLE host_bank_accounts ADD COLUMN account_label VARCHAR(50);
ALTER TABLE host_upi_accounts ADD COLUMN archived_at TIMESTAMPTZ;
ALTER TABLE host_bank_accounts ADD COLUMN archived_at TIMESTAMPTZ;
-- Historical preferences without a saved valid QR cannot remain eligible.
UPDATE host_upi_accounts u SET preferred=FALSE WHERE preferred=TRUE AND NOT EXISTS (
 SELECT 1 FROM host_upi_qr_images q WHERE q.host_id=u.host_id AND q.upi_account_id=u.id
 AND q.source IN ('UPLOADED','GENERATED') AND q.payload_sha256 IS NOT NULL
);
ALTER TABLE host_upi_accounts ADD CONSTRAINT chk_host_upi_archived_disabled CHECK (archived_at IS NULL OR (NOT enabled AND NOT preferred));
ALTER TABLE host_bank_accounts ADD CONSTRAINT chk_host_bank_archived_disabled CHECK (archived_at IS NULL OR (NOT enabled AND NOT preferred));
