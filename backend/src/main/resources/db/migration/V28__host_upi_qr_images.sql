-- Dedicated server-owned image store. No user-supplied paths or public URLs.
CREATE TABLE host_upi_qr_images (
 host_id UUID NOT NULL REFERENCES users(id),
 upi_account_id UUID NOT NULL REFERENCES host_upi_accounts(id),
 image_png BYTEA,
 payload_sha256 CHAR(64) NOT NULL CHECK (payload_sha256 ~ '^[0-9a-f]{64}$'),
 source VARCHAR(16) NOT NULL CHECK (source IN ('UPLOADED','GENERATED')),
 updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY (host_id, upi_account_id),
 CONSTRAINT chk_upi_qr_image_source CHECK ((source='UPLOADED' AND image_png IS NOT NULL) OR (source='GENERATED' AND image_png IS NULL))
);
