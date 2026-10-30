-- Pending email ownership proof. Never update an account contact until verified.
CREATE TABLE account_pending_email_verifications (
 challenge_id UUID PRIMARY KEY REFERENCES account_contact_challenges(id) ON DELETE CASCADE,
 account_kind VARCHAR(20) NOT NULL CHECK(account_kind IN ('GROUP_ADMIN','GROUP_MEMBER')),
 account_id UUID NOT NULL,
 email VARCHAR(254) NOT NULL,
 expires_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX account_pending_email_account ON account_pending_email_verifications(account_kind,account_id);
