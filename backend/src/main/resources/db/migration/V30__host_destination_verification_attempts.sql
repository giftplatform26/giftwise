-- Record attempts independently of destination state. No plaintext bank or UPI data.
CREATE TABLE host_destination_verification_attempts (
 id UUID PRIMARY KEY,
 host_id UUID NOT NULL REFERENCES users(id),
 destination_id UUID NOT NULL,
 destination_kind VARCHAR(8) NOT NULL CHECK (destination_kind IN ('BANK','UPI')),
 state VARCHAR(16) NOT NULL CHECK (state IN ('PENDING','VERIFIED','REJECTED','UNAVAILABLE')),
 provider VARCHAR(40),
 provider_reference VARCHAR(120),
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT chk_verification_evidence CHECK (state <> 'VERIFIED' OR (provider IS NOT NULL AND length(trim(provider)) > 0 AND provider_reference IS NOT NULL AND length(trim(provider_reference)) > 0))
);
CREATE UNIQUE INDEX ux_host_verification_pending ON host_destination_verification_attempts(host_id,destination_kind,destination_id) WHERE state='PENDING';
CREATE UNIQUE INDEX ux_host_verification_provider_reference ON host_destination_verification_attempts(provider,provider_reference) WHERE provider_reference IS NOT NULL;
CREATE INDEX idx_host_verification_attempt_lookup ON host_destination_verification_attempts(host_id,destination_kind,destination_id,created_at DESC);
