
CREATE TABLE account_pending_mobile_verifications (
 challenge_id UUID PRIMARY KEY REFERENCES account_contact_challenges(id) ON DELETE CASCADE,
 account_kind VARCHAR(20) NOT NULL,
 account_id UUID NOT NULL,
 mobile VARCHAR(16) NOT NULL,
 expires_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX account_pending_mobile_account_idx ON account_pending_mobile_verifications(account_kind,account_id);
