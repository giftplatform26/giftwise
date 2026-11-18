-- Direct UPI intent payments: guest -> host, no gateway, no platform collection account.
-- Everything here is reporting and reconciliation evidence. None of it posts to the wallet ledger
-- or to provider-confirmed contributions. A UPI app response is stored as UNTRUSTED evidence only.

-- One row per "Pay using UPI" tap. Amount, payee and references are fixed by the server here and
-- are the only values a later report can use (amount/recipient tampering protection).
CREATE TABLE guest_upi_payment_attempts (
 id UUID PRIMARY KEY,
 event_id UUID NOT NULL REFERENCES events(id),
 destination_id UUID NOT NULL REFERENCES host_upi_accounts(id),
 gift_id UUID NULL REFERENCES gifts(id),
 amount_paise BIGINT NOT NULL CHECK (amount_paise BETWEEN 100 AND 100000000),
 -- Short code in the UPI note (tn) so hosts can match their statement; becomes the claim's payment_code.
 payment_code VARCHAR(12) NOT NULL CHECK (payment_code ~ '^GW-[A-HJ-NP-Z2-9]{6}$'),
 -- UPI "tr" parameter: unique per attempt across the platform.
 transaction_ref VARCHAR(35) NOT NULL CHECK (transaction_ref ~ '^GWT[A-Z0-9]{12,32}$'),
 client_key VARCHAR(120) NOT NULL,
 status VARCHAR(24) NOT NULL DEFAULT 'INITIATED'
   CHECK (status IN ('INITIATED','PENDING','FAILED','PAYMENT_REPORTED','EXPIRED')),
 platform VARCHAR(16) NOT NULL DEFAULT 'UNKNOWN' CHECK (platform IN ('ANDROID','IOS','DESKTOP','UNKNOWN')),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 expires_at TIMESTAMPTZ NOT NULL,
 status_changed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 claim_id UUID NULL UNIQUE REFERENCES guest_direct_transfer_claims(id),
 -- Untrusted UPI app response (only native shells receive one; browsers never do).
 response_status VARCHAR(16) NULL CHECK (response_status IS NULL OR response_status IN ('SUCCESS','FAILURE','SUBMITTED','UNKNOWN')),
 response_code VARCHAR(16) NULL,
 response_txn_id VARCHAR(64) NULL,
 response_approval_ref VARCHAR(64) NULL,
 response_amount_paise BIGINT NULL,
 response_amount_mismatch BOOLEAN NOT NULL DEFAULT FALSE,
 response_source VARCHAR(40) NULL,
 response_sha256 CHAR(64) NULL,
 response_received_at TIMESTAMPTZ NULL,
 CONSTRAINT uq_upi_attempt_client_key UNIQUE (event_id, client_key),
 CONSTRAINT uq_upi_attempt_tr UNIQUE (transaction_ref),
 CONSTRAINT uq_upi_attempt_event_code UNIQUE (event_id, payment_code),
 CONSTRAINT chk_upi_attempt_reported_has_claim CHECK (status <> 'PAYMENT_REPORTED' OR claim_id IS NOT NULL)
);
CREATE INDEX idx_upi_attempt_event ON guest_upi_payment_attempts(event_id, created_at DESC);
COMMENT ON TABLE guest_upi_payment_attempts IS
 'UPI intent attempts. App responses are untrusted; never proof that the host was credited. Never posts to the ledger.';

ALTER TABLE guest_direct_transfer_claims ADD COLUMN attempt_id UUID NULL UNIQUE REFERENCES guest_upi_payment_attempts(id);
ALTER TABLE guest_direct_transfer_claims ADD COLUMN reported_via VARCHAR(16) NOT NULL DEFAULT 'MANUAL'
  CHECK (reported_via IN ('MANUAL','UPI_INTENT'));

-- Append-only history of every payment status transition (attempt and claim), visible to the event host
-- for their own payments. The platform-wide audit_log stays administrator-only.
CREATE TABLE guest_payment_status_events (
 id BIGSERIAL PRIMARY KEY,
 event_id UUID NOT NULL REFERENCES events(id),
 attempt_id UUID NULL REFERENCES guest_upi_payment_attempts(id),
 claim_id UUID NULL REFERENCES guest_direct_transfer_claims(id),
 previous_status VARCHAR(32) NULL,
 new_status VARCHAR(32) NOT NULL,
 actor_type VARCHAR(16) NOT NULL CHECK (actor_type IN ('GUEST','HOST','SYSTEM','PLATFORM_VERIFIER')),
 actor_id UUID NULL REFERENCES users(id),
 evidence VARCHAR(32) NULL,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 CONSTRAINT chk_payment_status_event_subject CHECK (attempt_id IS NOT NULL OR claim_id IS NOT NULL)
);
CREATE INDEX idx_payment_status_events_claim ON guest_payment_status_events(claim_id, id);
CREATE INDEX idx_payment_status_events_attempt ON guest_payment_status_events(attempt_id, id);
CREATE FUNCTION reject_payment_status_event_change() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN RAISE EXCEPTION 'Payment status history is append-only'; END $$;
CREATE TRIGGER payment_status_events_immutable BEFORE UPDATE OR DELETE ON guest_payment_status_events
 FOR EACH ROW EXECUTE FUNCTION reject_payment_status_event_change();

-- Optional guest screenshot. Supporting evidence for the host only; never treated as verification.
-- Stored re-encoded (metadata stripped). The same image cannot back two claims in one event.
CREATE TABLE guest_payment_evidence (
 claim_id UUID PRIMARY KEY REFERENCES guest_direct_transfer_claims(id),
 event_id UUID NOT NULL REFERENCES events(id),
 content_type VARCHAR(32) NOT NULL CHECK (content_type IN ('image/png','image/jpeg')),
 size_bytes INTEGER NOT NULL CHECK (size_bytes BETWEEN 1 AND 5000000),
 original_sha256 CHAR(64) NOT NULL,
 image BYTEA NOT NULL,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 CONSTRAINT uq_payment_evidence_event_image UNIQUE (event_id, original_sha256)
);
