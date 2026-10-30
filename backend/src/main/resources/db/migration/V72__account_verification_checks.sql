CREATE TABLE host_account_verification_checks (
 id UUID PRIMARY KEY,
 host_id UUID NOT NULL REFERENCES users(id),
 destination_id UUID NOT NULL,
 destination_kind VARCHAR(8) NOT NULL CHECK(destination_kind IN ('BANK','UPI')),
 environment VARCHAR(16) NOT NULL CHECK(environment IN ('SANDBOX','PRODUCTION')),
 outcome VARCHAR(24) NOT NULL CHECK(outcome IN ('PENDING','VERIFIED','SANDBOX_VALID','REJECTED','NAME_MISMATCH','INCONCLUSIVE','UNAVAILABLE','STALE')),
 provider_status VARCHAR(40),
 provider_reference VARCHAR(120),
 name_matched BOOLEAN NOT NULL DEFAULT FALSE,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 completed_at TIMESTAMPTZ,
 CHECK(outcome <> 'VERIFIED' OR (environment='PRODUCTION' AND provider_reference IS NOT NULL AND name_matched))
);
CREATE INDEX idx_host_account_checks ON host_account_verification_checks(host_id,destination_kind,destination_id,created_at DESC);
CREATE UNIQUE INDEX ux_account_check_pending ON host_account_verification_checks(host_id,destination_kind,destination_id) WHERE outcome='PENDING';
CREATE UNIQUE INDEX ux_account_check_evidence ON host_account_verification_checks(environment,provider_reference) WHERE provider_reference IS NOT NULL AND environment='PRODUCTION';
