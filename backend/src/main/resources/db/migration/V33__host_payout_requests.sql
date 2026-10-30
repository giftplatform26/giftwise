-- Payout execution deliberately blocked until provider access, settlement eligibility,
-- balance reservation, compliance, and authenticated reconciliation are complete.
CREATE TABLE host_payout_requests (
 id UUID PRIMARY KEY,
 host_id UUID NOT NULL REFERENCES users(id),
 event_id UUID NOT NULL REFERENCES events(id),
 destination_id UUID NOT NULL REFERENCES host_bank_accounts(id),
 amount_paise BIGINT NOT NULL CHECK (amount_paise > 0),
 currency VARCHAR(3) NOT NULL DEFAULT 'INR' CHECK (currency = 'INR'),
 idempotency_key VARCHAR(120) NOT NULL,
 state VARCHAR(24) NOT NULL DEFAULT 'BLOCKED'
   CHECK (state IN ('BLOCKED')),
 reason VARCHAR(160) NOT NULL DEFAULT 'LIVE_PAYOUTS_DISABLED',
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE (host_id, idempotency_key)
);
CREATE INDEX idx_host_payout_requests_event ON host_payout_requests(event_id,created_at DESC);
