-- Delivery attempts only: no email addresses, usernames, PINs or passwords.
CREATE TABLE username_recovery_attempts (
 id UUID PRIMARY KEY,
 ip_fingerprint CHAR(64) NOT NULL,
 account_kind VARCHAR(20) NOT NULL CHECK(account_kind IN ('GROUP_ADMIN','GROUP_MEMBER')),
 account_id UUID,
 requested_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX username_recovery_ip_time ON username_recovery_attempts(ip_fingerprint,requested_at);
CREATE INDEX username_recovery_account_time ON username_recovery_attempts(account_kind,account_id,requested_at);
