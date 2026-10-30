-- Policy controls are not proof of verified email/mobile ownership.
CREATE TABLE account_registration_policies (
 scope VARCHAR(12) NOT NULL CHECK(scope IN ('PLATFORM','HOST','EVENT')),
 scope_id UUID NOT NULL,
 require_email BOOLEAN NOT NULL DEFAULT TRUE,
 require_mobile BOOLEAN NOT NULL DEFAULT TRUE,
 verify_email BOOLEAN NOT NULL DEFAULT TRUE,
 verify_mobile BOOLEAN NOT NULL DEFAULT TRUE,
 allow_group_pin BOOLEAN NOT NULL DEFAULT TRUE,
 changed_by UUID REFERENCES users(id),
 changed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 PRIMARY KEY(scope,scope_id)
);
-- UUID zero is the platform singleton; no FK because HOST and EVENT have different parents.
INSERT INTO account_registration_policies(scope,scope_id)
VALUES('PLATFORM','00000000-0000-0000-0000-000000000000')
ON CONFLICT DO NOTHING;
CREATE TABLE account_recovery_challenges (
 id UUID PRIMARY KEY,
 account_kind VARCHAR(20) NOT NULL CHECK(account_kind IN ('PLATFORM_ADMIN','HOST','GROUP_ADMIN','GROUP_MEMBER')),
 account_id UUID NOT NULL,
 secret_hash VARCHAR(128) NOT NULL,
 expires_at TIMESTAMPTZ NOT NULL,
 consumed_at TIMESTAMPTZ,
 attempt_count INTEGER NOT NULL DEFAULT 0 CHECK(attempt_count BETWEEN 0 AND 10),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX account_recovery_challenges_lookup ON account_recovery_challenges(account_kind,account_id,created_at DESC);
