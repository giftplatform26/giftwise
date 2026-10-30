-- Align DB preferred constraints with host-confirmed OR provider-verified preference selection.
-- This does NOT independently verify ownership or confirm guest payments.
-- Existing partial unique indexes enforce at most one preferred destination per type/host.
ALTER TABLE host_upi_accounts DROP CONSTRAINT chk_host_upi_preferred;
ALTER TABLE host_upi_accounts ADD CONSTRAINT chk_host_upi_preferred
    CHECK (NOT preferred OR (enabled AND
        (verification_status = 'VERIFIED' OR host_confirmed_at IS NOT NULL)));

ALTER TABLE host_bank_accounts DROP CONSTRAINT chk_host_bank_preferred;
ALTER TABLE host_bank_accounts ADD CONSTRAINT chk_host_bank_preferred
    CHECK (NOT preferred OR (enabled AND
        (verification_status = 'VERIFIED' OR host_confirmed_at IS NOT NULL)));
