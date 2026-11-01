-- Link the special gift to the host's existing manual transfer report.
-- Transfer reports remain unverified and never credit a confirmed balance.
CREATE TABLE guest_gift_host_contributions (
 guest_gift_id UUID PRIMARY KEY REFERENCES guest_gifts(id),
 claim_id UUID NOT NULL UNIQUE REFERENCES guest_direct_transfer_claims(id),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
