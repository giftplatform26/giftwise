-- Separate from event-host payout destinations. No automatic verification or ledger credit.
CREATE TABLE gift_group_payment_permissions (
 group_id UUID PRIMARY KEY REFERENCES gift_groups(id) ON DELETE CASCADE,
 switching_enabled BOOLEAN NOT NULL DEFAULT FALSE,
 changed_by UUID REFERENCES users(id),
 changed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE gift_group_payment_destinations (
 id UUID PRIMARY KEY,
 group_id UUID NOT NULL REFERENCES gift_groups(id) ON DELETE CASCADE,
 kind VARCHAR(8) NOT NULL CHECK (kind IN ('UPI','BANK')),
 label VARCHAR(80) NOT NULL,
 upi_ciphertext TEXT,
 upi_fingerprint VARCHAR(64),
 account_ciphertext TEXT,
 account_fingerprint VARCHAR(64),
 ifsc_ciphertext TEXT,
 holder_ciphertext TEXT,
 status VARCHAR(24) NOT NULL DEFAULT 'REGISTERED' CHECK(status IN ('REGISTERED','VERIFIED','REVOKED')),
 preferred BOOLEAN NOT NULL DEFAULT FALSE,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 CONSTRAINT gift_group_destination_fields CHECK (
  (kind='UPI' AND upi_ciphertext IS NOT NULL AND upi_fingerprint IS NOT NULL AND account_ciphertext IS NULL)
  OR (kind='BANK' AND account_ciphertext IS NOT NULL AND account_fingerprint IS NOT NULL AND ifsc_ciphertext IS NOT NULL AND holder_ciphertext IS NOT NULL AND upi_ciphertext IS NULL))
);
CREATE UNIQUE INDEX gift_group_dest_unique_upi ON gift_group_payment_destinations(group_id,upi_fingerprint) WHERE upi_fingerprint IS NOT NULL AND status<>'REVOKED';
CREATE UNIQUE INDEX gift_group_dest_unique_bank ON gift_group_payment_destinations(group_id,account_fingerprint) WHERE account_fingerprint IS NOT NULL AND status<>'REVOKED';
CREATE UNIQUE INDEX gift_group_one_preferred ON gift_group_payment_destinations(group_id) WHERE preferred AND status<>'REVOKED';
CREATE TABLE gift_group_payment_audit (
 id UUID PRIMARY KEY, group_id UUID NOT NULL REFERENCES gift_groups(id),
 actor_type VARCHAR(20) NOT NULL, actor_id UUID NOT NULL,
 action VARCHAR(40) NOT NULL, destination_id UUID,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
