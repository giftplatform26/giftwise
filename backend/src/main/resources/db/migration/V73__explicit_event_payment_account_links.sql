-- Host accounts are reusable, but each event needs an explicit enabled/preferred link.
ALTER TABLE host_bank_accounts ADD CONSTRAINT ux_bank_account_owner UNIQUE(id,host_id);
ALTER TABLE host_upi_accounts ADD CONSTRAINT ux_upi_account_owner UNIQUE(id,host_id);
CREATE TABLE event_payment_account_links (
 event_id UUID NOT NULL,
 host_id UUID NOT NULL,
 bank_account_id UUID,
 upi_account_id UUID,
 destination_id UUID GENERATED ALWAYS AS (COALESCE(bank_account_id,upi_account_id)) STORED,
 destination_kind TEXT GENERATED ALWAYS AS (CASE WHEN bank_account_id IS NOT NULL THEN 'BANK' ELSE 'UPI' END) STORED,
 enabled BOOLEAN NOT NULL DEFAULT TRUE,
 preferred BOOLEAN NOT NULL DEFAULT FALSE,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 PRIMARY KEY(event_id,destination_kind,destination_id),
 FOREIGN KEY(event_id,host_id) REFERENCES events(id,owner_id),
 FOREIGN KEY(bank_account_id,host_id) REFERENCES host_bank_accounts(id,host_id),
 FOREIGN KEY(upi_account_id,host_id) REFERENCES host_upi_accounts(id,host_id),
 CHECK ((bank_account_id IS NOT NULL) <> (upi_account_id IS NOT NULL)),
 CHECK (NOT preferred OR enabled)
);
CREATE UNIQUE INDEX ux_event_link_preferred ON event_payment_account_links(event_id,destination_kind) WHERE preferred;
-- Preserve explicit event assignments only. Never infer links from host-wide preferences.
INSERT INTO event_payment_account_links(event_id,host_id,bank_account_id,enabled,preferred)
 SELECT event_id,host_id,id,enabled,preferred FROM host_bank_accounts WHERE event_id IS NOT NULL AND archived_at IS NULL;
INSERT INTO event_payment_account_links(event_id,host_id,upi_account_id,enabled,preferred)
 SELECT event_id,host_id,id,enabled,preferred FROM host_upi_accounts WHERE event_id IS NOT NULL AND archived_at IS NULL;

-- Disabling, archiving or invalidating an account clears every event preference for it.
CREATE FUNCTION clear_event_account_preferences() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 IF NOT NEW.enabled OR NEW.archived_at IS NOT NULL OR NEW.verification_status IN ('FAILED','REVOKED') THEN
  UPDATE event_payment_account_links SET preferred=FALSE
   WHERE host_id=NEW.host_id AND destination_id=NEW.id AND destination_kind=TG_ARGV[0];
 END IF;
 RETURN NEW;
END;
$$;
CREATE TRIGGER trg_bank_event_preferences AFTER UPDATE OF enabled,archived_at,verification_status ON host_bank_accounts
 FOR EACH ROW EXECUTE FUNCTION clear_event_account_preferences('BANK');
CREATE TRIGGER trg_upi_event_preferences AFTER UPDATE OF enabled,archived_at,verification_status ON host_upi_accounts
 FOR EACH ROW EXECUTE FUNCTION clear_event_account_preferences('UPI');
