-- New contact verification fields; existing accounts are preserved unchanged.
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verified_at TIMESTAMPTZ;
ALTER TABLE gift_group_accounts ADD COLUMN IF NOT EXISTS email VARCHAR(254);
ALTER TABLE gift_group_accounts ADD COLUMN IF NOT EXISTS mobile VARCHAR(16);
ALTER TABLE gift_group_accounts ADD COLUMN IF NOT EXISTS email_verified_at TIMESTAMPTZ;
ALTER TABLE gift_group_accounts ADD COLUMN IF NOT EXISTS mobile_verified_at TIMESTAMPTZ;
ALTER TABLE gift_group_members ADD COLUMN IF NOT EXISTS email VARCHAR(254);
ALTER TABLE gift_group_members ADD COLUMN IF NOT EXISTS email_verified_at TIMESTAMPTZ;
ALTER TABLE gift_group_members ADD COLUMN IF NOT EXISTS mobile_verified_at TIMESTAMPTZ;
-- OTP hashes only; no plaintext codes are stored.
CREATE TABLE IF NOT EXISTS account_contact_challenges (
  id UUID PRIMARY KEY,
  account_kind VARCHAR(20) NOT NULL CHECK(account_kind IN ('PLATFORM_ADMIN','HOST','GROUP_ADMIN','GROUP_MEMBER')),
  account_id UUID NOT NULL,
  channel VARCHAR(8) NOT NULL CHECK(channel IN ('EMAIL','SMS')),
  purpose VARCHAR(24) NOT NULL CHECK(purpose IN ('VERIFY_CONTACT','RESET_PASSWORD','RESET_PIN')),
  destination_fingerprint VARCHAR(64) NOT NULL,
  secret_hash VARCHAR(255) NOT NULL,
  expires_at TIMESTAMPTZ NOT NULL,
  consumed_at TIMESTAMPTZ,
  attempt_count INTEGER NOT NULL DEFAULT 0 CHECK(attempt_count BETWEEN 0 AND 10),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS account_contact_challenges_lookup ON account_contact_challenges(account_kind, account_id, purpose, created_at DESC);
