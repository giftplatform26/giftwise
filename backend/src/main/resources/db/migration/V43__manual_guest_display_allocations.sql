-- Provisional presentation allocations for guest-reported manual transfers.
-- No payment capture, settlement, or withdrawable wallet credit is implied.
CREATE TABLE guest_manual_display_allocations (
 claim_id UUID PRIMARY KEY REFERENCES guest_direct_transfer_claims(id),
 event_id UUID NOT NULL REFERENCES events(id),
 gift_id UUID NULL REFERENCES gifts(id),
 gift_paise BIGINT NOT NULL DEFAULT 0 CHECK (gift_paise >= 0),
 wallet_paise BIGINT NOT NULL DEFAULT 0 CHECK (wallet_paise >= 0),
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CHECK (gift_id IS NOT NULL OR gift_paise = 0)
);
CREATE INDEX idx_guest_manual_display_gift ON guest_manual_display_allocations(event_id,gift_id);
CREATE INDEX idx_guest_manual_display_event ON guest_manual_display_allocations(event_id);
COMMENT ON TABLE guest_manual_display_allocations IS 'Provisional gift progress and event-wallet intent only; not confirmed money or a ledger entry';
