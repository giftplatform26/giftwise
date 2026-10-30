-- Separate group bookkeeping. No rows here represent verified money.
ALTER TABLE gift_groups ADD COLUMN creator_direct_payments_enabled BOOLEAN NOT NULL DEFAULT FALSE;
CREATE TABLE gift_group_invitations (
 id UUID PRIMARY KEY, group_id UUID NOT NULL REFERENCES gift_groups(id),
 token_hash VARCHAR(64) NOT NULL UNIQUE, expires_at TIMESTAMPTZ NOT NULL,
 revoked_at TIMESTAMPTZ, max_uses INTEGER NOT NULL DEFAULT 1 CHECK(max_uses BETWEEN 1 AND 100),
 used_count INTEGER NOT NULL DEFAULT 0 CHECK(used_count>=0), created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX gift_group_invitations_group_idx ON gift_group_invitations(group_id);
CREATE TABLE gift_group_members (
 id UUID PRIMARY KEY, group_id UUID NOT NULL REFERENCES gift_groups(id),
 display_name VARCHAR(120) NOT NULL, username VARCHAR(40) NOT NULL,
 password_hash VARCHAR(100) NOT NULL,
 status VARCHAR(16) NOT NULL DEFAULT 'PENDING' CHECK(status IN ('PENDING','APPROVED','REJECTED','REMOVED')),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(), reviewed_at TIMESTAMPTZ,
 UNIQUE(group_id,username), CHECK(username=lower(username))
);
CREATE INDEX gift_group_members_status_idx ON gift_group_members(group_id,status);
CREATE TABLE gift_group_payment_reports (
 id UUID PRIMARY KEY, group_id UUID NOT NULL REFERENCES gift_groups(id),
 member_id UUID NOT NULL REFERENCES gift_group_members(id),
 amount_paise BIGINT NOT NULL CHECK(amount_paise>0),
 method VARCHAR(16) NOT NULL CHECK(method IN ('UPI','BANK')),
 transaction_reference VARCHAR(120) NOT NULL,
 state VARCHAR(24) NOT NULL DEFAULT 'REPORTED_UNVERIFIED'
   CHECK(state IN ('REPORTED_UNVERIFIED','VERIFIED','REJECTED')),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(), verified_at TIMESTAMPTZ,
 UNIQUE(group_id,member_id,method,transaction_reference),
 CHECK((state='VERIFIED' AND verified_at IS NOT NULL) OR (state<>'VERIFIED' AND verified_at IS NULL))
);
CREATE INDEX gift_group_payment_reports_group_idx ON gift_group_payment_reports(group_id,state);
-- A creator's UPI/bank destination must be encrypted before being persisted.
-- No destination table is added until the encryption adapter and verification policy are wired.
