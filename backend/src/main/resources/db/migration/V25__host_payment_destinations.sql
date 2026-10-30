CREATE TABLE host_upi_accounts (
    id UUID PRIMARY KEY,
    host_id UUID NOT NULL REFERENCES users(id),
    upi_id_encrypted TEXT NOT NULL,
    upi_id_fingerprint VARCHAR(64) NOT NULL,
    upi_id_masked VARCHAR(120) NOT NULL,
    verification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    preferred BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_host_upi_verification
        CHECK (verification_status IN
            ('PENDING', 'VERIFIED', 'FAILED', 'REVOKED')),

    CONSTRAINT chk_host_upi_preferred
        CHECK (
            NOT preferred OR
            (enabled AND verification_status = 'VERIFIED')
        ),

    CONSTRAINT ux_host_upi_fingerprint
        UNIQUE (host_id, upi_id_fingerprint)
);

CREATE UNIQUE INDEX ux_host_preferred_upi
    ON host_upi_accounts(host_id)
    WHERE preferred = TRUE;

CREATE INDEX idx_host_upi_accounts_host
    ON host_upi_accounts(host_id);


CREATE TABLE host_bank_accounts (
    id UUID PRIMARY KEY,
    host_id UUID NOT NULL REFERENCES users(id),
    account_number_encrypted TEXT NOT NULL,
    account_fingerprint VARCHAR(64) NOT NULL,
    account_number_masked VARCHAR(32) NOT NULL,
    ifsc_encrypted TEXT NOT NULL,
    ifsc_masked VARCHAR(20) NOT NULL,
    account_holder_encrypted TEXT NOT NULL,
    verification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    preferred BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_host_bank_verification
        CHECK (verification_status IN
            ('PENDING', 'VERIFIED', 'FAILED', 'REVOKED')),

    CONSTRAINT chk_host_bank_preferred
        CHECK (
            NOT preferred OR
            (enabled AND verification_status = 'VERIFIED')
        ),

    CONSTRAINT ux_host_bank_fingerprint
        UNIQUE (host_id, account_fingerprint)
);

CREATE UNIQUE INDEX ux_host_preferred_bank
    ON host_bank_accounts(host_id)
    WHERE preferred = TRUE;

CREATE INDEX idx_host_bank_accounts_host
    ON host_bank_accounts(host_id);
