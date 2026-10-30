-- Guest reports are untrusted and must never affect contribution or ledger balances.
CREATE TABLE guest_direct_transfer_claims (
 id UUID PRIMARY KEY, event_id UUID NOT NULL REFERENCES events(id),
 gift_id UUID NULL REFERENCES gifts(id), destination_id UUID NOT NULL,
 method VARCHAR(8) NOT NULL CHECK(method IN ('UPI','BANK')),
 amount_paise BIGINT NOT NULL CHECK(amount_paise BETWEEN 100 AND 100000000),
 guest_name VARCHAR(200) NOT NULL, reference_hash CHAR(64) NOT NULL,
 client_key VARCHAR(120) NOT NULL, status VARCHAR(32) NOT NULL DEFAULT 'AWAITING_VERIFICATION'
   CHECK(status IN ('AWAITING_VERIFICATION','REJECTED','EXPIRED')),
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT uq_direct_claim_key UNIQUE(event_id,client_key),
 CONSTRAINT uq_direct_claim_reference UNIQUE(method,reference_hash)
);
CREATE INDEX idx_direct_claim_event ON guest_direct_transfer_claims(event_id,created_at DESC);
