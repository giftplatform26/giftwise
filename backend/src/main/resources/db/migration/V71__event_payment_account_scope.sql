-- Existing accounts remain host-wide. Event accounts must belong to the event owner.
ALTER TABLE events ADD CONSTRAINT ux_events_id_owner UNIQUE (id,owner_id);
ALTER TABLE host_upi_accounts ADD COLUMN event_id UUID;
ALTER TABLE host_bank_accounts ADD COLUMN event_id UUID;
ALTER TABLE host_upi_accounts ADD CONSTRAINT fk_upi_event_owner FOREIGN KEY(event_id,host_id) REFERENCES events(id,owner_id);
ALTER TABLE host_bank_accounts ADD CONSTRAINT fk_bank_event_owner FOREIGN KEY(event_id,host_id) REFERENCES events(id,owner_id);
DROP INDEX ux_host_preferred_upi;
DROP INDEX ux_host_preferred_bank;
CREATE UNIQUE INDEX ux_host_preferred_upi ON host_upi_accounts(host_id) WHERE preferred AND event_id IS NULL;
CREATE UNIQUE INDEX ux_host_preferred_bank ON host_bank_accounts(host_id) WHERE preferred AND event_id IS NULL;
CREATE UNIQUE INDEX ux_event_preferred_upi ON host_upi_accounts(event_id) WHERE preferred AND event_id IS NOT NULL;
CREATE UNIQUE INDEX ux_event_preferred_bank ON host_bank_accounts(event_id) WHERE preferred AND event_id IS NOT NULL;
DROP INDEX ux_host_upi_fingerprint;
DROP INDEX ux_host_bank_fingerprint;
CREATE UNIQUE INDEX ux_host_upi_fingerprint ON host_upi_accounts(host_id,upi_id_fingerprint) WHERE archived_at IS NULL AND event_id IS NULL;
CREATE UNIQUE INDEX ux_host_bank_fingerprint ON host_bank_accounts(host_id,account_fingerprint) WHERE archived_at IS NULL AND event_id IS NULL;
CREATE UNIQUE INDEX ux_event_upi_fingerprint ON host_upi_accounts(event_id,upi_id_fingerprint) WHERE archived_at IS NULL AND event_id IS NOT NULL;
CREATE UNIQUE INDEX ux_event_bank_fingerprint ON host_bank_accounts(event_id,account_fingerprint) WHERE archived_at IS NULL AND event_id IS NOT NULL;
