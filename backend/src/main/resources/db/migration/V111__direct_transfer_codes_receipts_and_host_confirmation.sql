-- Direct UPI/bank transfers: easier matching and host confirmation, without any payment provider.
--  * payment_code   : short code (e.g. GW-7K3P9Q) placed in the UPI/bank payment note, shown on the host's statement.
--  * reference_tail : last 6 characters of the guest's UTR so the host can match their bank statement
--                     (the full reference stays hashed for duplicate detection).
--  * receipt_token  : unguessable link for the guest's receipt page.
--  * HOST_CONFIRMED : the host confirmed the money arrived. This is the host's statement, not provider verification.
ALTER TABLE guest_direct_transfer_claims ADD COLUMN payment_code VARCHAR(12);
ALTER TABLE guest_direct_transfer_claims ADD COLUMN reference_tail VARCHAR(8);
ALTER TABLE guest_direct_transfer_claims ADD COLUMN receipt_token UUID;
ALTER TABLE guest_direct_transfer_claims ADD COLUMN host_decided_at TIMESTAMPTZ;
ALTER TABLE guest_direct_transfer_claims ADD COLUMN host_decided_by UUID REFERENCES users(id);
ALTER TABLE guest_direct_transfer_claims ADD CONSTRAINT chk_direct_claim_payment_code CHECK (payment_code IS NULL OR payment_code ~ '^GW-[A-HJ-NP-Z2-9]{6}$');
ALTER TABLE guest_direct_transfer_claims DROP CONSTRAINT IF EXISTS guest_direct_transfer_claims_status_check;
ALTER TABLE guest_direct_transfer_claims ADD CONSTRAINT guest_direct_transfer_claims_status_check
  CHECK (status IN ('AWAITING_VERIFICATION','HOST_CONFIRMED','REJECTED','EXPIRED'));
CREATE UNIQUE INDEX ux_direct_claim_receipt_token ON guest_direct_transfer_claims(receipt_token) WHERE receipt_token IS NOT NULL;
CREATE UNIQUE INDEX ux_direct_claim_event_code ON guest_direct_transfer_claims(event_id,payment_code) WHERE payment_code IS NOT NULL;
CREATE INDEX idx_direct_claim_event_status ON guest_direct_transfer_claims(event_id,status,created_at DESC);
