-- V27: store uploaded image bytes in PostgreSQL rather than trusting client file paths.
-- The raw QR may reveal a UPI ID. Read access must be host-scoped.
ALTER TABLE host_upi_qr_assets ADD COLUMN image_png BYTEA;
ALTER TABLE host_upi_qr_assets ADD CONSTRAINT chk_upi_qr_png_size
  CHECK (image_png IS NULL OR octet_length(image_png) BETWEEN 100 AND 2000000);
-- Generated QR codes need no stored bytes: they can be deterministically regenerated.
ALTER TABLE host_upi_qr_assets ADD CONSTRAINT chk_upi_qr_upload_bytes
  CHECK (source <> 'UPLOADED' OR image_png IS NOT NULL);
