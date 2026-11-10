-- Self-service account deletion. Accounts are anonymised and disabled rather than
-- physically removed, because payments, audits and settlement records reference users(id)
-- and must be retained. No existing rows are changed by this migration.
ALTER TABLE users ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;
ALTER TABLE users ADD CONSTRAINT chk_users_deleted_disabled CHECK (deleted_at IS NULL OR enabled = FALSE);
