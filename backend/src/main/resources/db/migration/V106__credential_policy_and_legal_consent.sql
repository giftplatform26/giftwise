-- No existing usernames or password hashes are rewritten.
ALTER TABLE users DROP CONSTRAINT chk_users_username_format;
ALTER TABLE users ADD CONSTRAINT chk_users_username_format
 CHECK (username IS NULL OR username ~ '^[A-Za-z0-9][A-Za-z0-9_]{3,29}$');
CREATE TABLE user_legal_consents (
 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
 user_id UUID NOT NULL REFERENCES users(id),
 terms_version VARCHAR(40) NOT NULL,
 privacy_version VARCHAR(40) NOT NULL,
 accepted_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_user_legal_consents_user ON user_legal_consents(user_id, accepted_at);
