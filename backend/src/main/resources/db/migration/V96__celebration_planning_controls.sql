-- Additive, opt-in rollout: existing events retain their gifting behavior.
ALTER TABLE platform_module_controls DROP CONSTRAINT module_code_allowed;
ALTER TABLE platform_module_controls ADD CONSTRAINT module_code_allowed CHECK
 (module_code IN ('GROUP_GIFTING','GUEST_GIFTS','RSVP','GUEST_ATTENDANCE','WHATSAPP_SHARING','GUESTBOOK','SOCIAL_SHARING','MEMORY_ALBUM','AI_INVITATION','CELEBRATION_PLANNING','EVENT_ESSENTIALS','EVENT_REUSE'));
INSERT INTO platform_module_controls(module_code,enabled) VALUES
 ('CELEBRATION_PLANNING',FALSE),('EVENT_ESSENTIALS',FALSE),('EVENT_REUSE',FALSE);
CREATE TABLE platform_event_module_grants (
 event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
 module_code VARCHAR(40) NOT NULL REFERENCES platform_module_controls(module_code),
 enabled BOOLEAN NOT NULL DEFAULT FALSE,
 changed_by UUID REFERENCES users(id), changed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 PRIMARY KEY(event_id,module_code),
 CONSTRAINT event_module_supported CHECK(module_code IN ('CELEBRATION_PLANNING','EVENT_ESSENTIALS','EVENT_REUSE'))
);
ALTER TABLE events ADD COLUMN gifting_enabled BOOLEAN NOT NULL DEFAULT TRUE;
CREATE TABLE event_celebration_settings (
 event_id UUID PRIMARY KEY REFERENCES events(id) ON DELETE CASCADE,
 welcome_message VARCHAR(500) NOT NULL DEFAULT '',
 share_venue BOOLEAN NOT NULL DEFAULT FALSE,
 start_time TIME, timezone VARCHAR(80) NOT NULL DEFAULT 'Asia/Kolkata',
 duration_minutes INTEGER NOT NULL DEFAULT 120 CHECK(duration_minutes BETWEEN 15 AND 1440),
 arrival_notes VARCHAR(1000) NOT NULL DEFAULT '',
 version BIGINT NOT NULL DEFAULT 0,
 changed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Shared locks allow concurrent contributions; settings changes take the exclusive
-- partner lock before the event row lock. This closes the unpublish/change-mode
-- race without serializing provider network calls for the same event.
CREATE FUNCTION enforce_event_gifting_policy() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE gifting BOOLEAN;
BEGIN
 PERFORM pg_advisory_xact_lock_shared(1195984468,hashtext(NEW.event_id::text));
 SELECT gifting_enabled INTO gifting FROM events WHERE id=NEW.event_id;
 IF gifting IS FALSE THEN
  RAISE EXCEPTION 'Gifting is not enabled for this event'
   USING ERRCODE='23514',CONSTRAINT='event_gifting_open';
 END IF;
 RETURN NEW;
END;
$$;
CREATE TRIGGER payment_order_gifting_policy BEFORE INSERT ON payment_orders
 FOR EACH ROW EXECUTE FUNCTION enforce_event_gifting_policy();
CREATE TRIGGER direct_claim_gifting_policy BEFORE INSERT ON guest_direct_transfer_claims
 FOR EACH ROW EXECUTE FUNCTION enforce_event_gifting_policy();
CREATE TRIGGER group_gifting_policy BEFORE INSERT ON gift_groups
 FOR EACH ROW EXECUTE FUNCTION enforce_event_gifting_policy();
CREATE TRIGGER guest_gift_gifting_policy BEFORE INSERT ON guest_gifts
 FOR EACH ROW EXECUTE FUNCTION enforce_event_gifting_policy();
