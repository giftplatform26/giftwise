CREATE TABLE signup_email_verifications (
 id UUID PRIMARY KEY,
 account_kind VARCHAR(20) NOT NULL CHECK(account_kind IN ('HOST','GROUP_ADMIN','GROUP_MEMBER')),
 scope_id UUID NOT NULL,
 email VARCHAR(254) NOT NULL,
 ip_fingerprint CHAR(64) NOT NULL,
 secret_hash CHAR(64) NOT NULL,
 proof_hash CHAR(64) UNIQUE,
 attempt_count INTEGER NOT NULL DEFAULT 0,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 expires_at TIMESTAMPTZ NOT NULL,
 verified_at TIMESTAMPTZ,
 consumed_at TIMESTAMPTZ
);
CREATE INDEX signup_email_ip_time ON signup_email_verifications(ip_fingerprint,created_at);
CREATE INDEX signup_email_address_time ON signup_email_verifications(email,created_at);
