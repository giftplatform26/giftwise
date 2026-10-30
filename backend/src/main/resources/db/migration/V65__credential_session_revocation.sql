-- Revocation epochs for every account type. Existing accounts default to epoch zero.
-- This schema does not revoke sessions until all authentication paths enforce it.
CREATE TABLE credential_session_versions (
  account_kind VARCHAR(20) NOT NULL CHECK (account_kind IN ('PLATFORM_ADMIN','HOST','GROUP_ADMIN','GROUP_MEMBER')),
  account_id UUID NOT NULL,
  version BIGINT NOT NULL DEFAULT 0 CHECK (version >= 0),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (account_kind, account_id)
);
