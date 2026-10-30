-- Host confirmation is a declaration, NOT independent ownership verification.
ALTER TABLE host_upi_accounts ADD COLUMN host_confirmed_at TIMESTAMPTZ;
ALTER TABLE host_bank_accounts ADD COLUMN host_confirmed_at TIMESTAMPTZ;
