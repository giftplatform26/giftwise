-- Preserve uploaded and generated QR records independently per host UPI account.
-- PostgreSQL can reuse the existing PK constraint index after dropping the constraint.
ALTER TABLE host_upi_qr_images DROP CONSTRAINT host_upi_qr_images_pkey;
ALTER TABLE host_upi_qr_images ADD CONSTRAINT host_upi_qr_images_pkey
  PRIMARY KEY (host_id, upi_account_id, source);
