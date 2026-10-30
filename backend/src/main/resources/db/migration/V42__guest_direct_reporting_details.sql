-- Manual guest transfer reporting only. No payment capture or wallet allocation.
ALTER TABLE guest_direct_transfer_claims ADD COLUMN guest_email VARCHAR(320);
ALTER TABLE guest_direct_transfer_claims ADD COLUMN guest_phone VARCHAR(30);
CREATE INDEX idx_guest_direct_report_method_status ON guest_direct_transfer_claims(method,status,created_at DESC);
COMMENT ON TABLE guest_direct_transfer_claims IS 'Unverified guest-declared UPI/bank transfer reports; never post to confirmed contribution or wallet ledger';
