-- Manual reports are not settlement evidence and never credit a financial ledger.
ALTER TABLE gift_groups ADD COLUMN suggested_contribution_paise BIGINT CHECK (suggested_contribution_paise BETWEEN 100 AND 1000000000);
ALTER TABLE gift_groups ADD COLUMN manual_contributions_enabled BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE gift_groups ADD COLUMN guest_upi_destination_id UUID REFERENCES gift_group_payment_destinations(id);
ALTER TABLE gift_groups ADD COLUMN guest_bank_destination_id UUID REFERENCES gift_group_payment_destinations(id);
CREATE TABLE gift_group_guest_reports (
 id UUID PRIMARY KEY,
 group_id UUID NOT NULL REFERENCES gift_groups(id),
 destination_id UUID NOT NULL REFERENCES gift_group_payment_destinations(id),
 guest_name VARCHAR(120) NOT NULL,
 guest_contact VARCHAR(120),
 amount_paise BIGINT NOT NULL CHECK (amount_paise BETWEEN 100 AND 1000000000),
 method VARCHAR(8) NOT NULL CHECK(method IN ('UPI','BANK')),
 transaction_reference VARCHAR(120) NOT NULL,
 submission_key UUID NOT NULL,
 state VARCHAR(32) NOT NULL DEFAULT 'REPORTED_UNVERIFIED' CHECK(state IN ('REPORTED_UNVERIFIED','VERIFIED','REJECTED')),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 UNIQUE(group_id,submission_key),
 UNIQUE(group_id,method,transaction_reference)
);
CREATE INDEX gift_group_guest_reports_group_date_idx ON gift_group_guest_reports(group_id,created_at DESC);
