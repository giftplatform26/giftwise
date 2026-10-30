-- Immutable quote records are informational, not payment or ledger entries.
CREATE TABLE hybrid_checkout_quotes (
 id UUID PRIMARY KEY,
 event_id UUID NOT NULL REFERENCES events(id),
 gift_id UUID REFERENCES gifts(id),
 contribution_paise BIGINT NOT NULL CHECK (contribution_paise > 0),
 processing_fee_paise BIGINT NOT NULL DEFAULT 0 CHECK (processing_fee_paise >= 0),
 total_paise BIGINT NOT NULL CHECK (total_paise = contribution_paise + processing_fee_paise),
 method VARCHAR(20) NOT NULL CHECK (method IN ('RAZORPAY_CHECKOUT')),
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
