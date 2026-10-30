-- No verification is inferred from account enablement or QR matching.
ALTER TABLE gift_group_payment_destinations ADD COLUMN enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE gift_group_payment_destinations ADD COLUMN qr_image BYTEA;
ALTER TABLE gift_group_payment_destinations ADD COLUMN qr_source VARCHAR(12) CHECK (qr_source IN ('UPLOADED','GENERATED'));
ALTER TABLE gift_group_payment_destinations ADD CONSTRAINT gift_group_qr_upi_only CHECK(qr_image IS NULL OR kind='UPI');
DROP INDEX IF EXISTS gift_group_one_preferred;
CREATE UNIQUE INDEX gift_group_one_preferred_per_kind ON gift_group_payment_destinations(group_id,kind) WHERE preferred AND status<>'REVOKED';
