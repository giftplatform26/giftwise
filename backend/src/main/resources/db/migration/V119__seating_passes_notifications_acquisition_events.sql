-- Batch G7: seating planner, personal guest passes, host notifications and acquisition measurement. All opt-in.
ALTER TABLE platform_module_controls DROP CONSTRAINT module_code_allowed;
ALTER TABLE platform_module_controls ADD CONSTRAINT module_code_allowed CHECK
 (module_code IN ('GROUP_GIFTING','GUEST_GIFTS','RSVP','GUEST_ATTENDANCE','WHATSAPP_SHARING','GUESTBOOK','SOCIAL_SHARING','MEMORY_ALBUM','AI_INVITATION',
  'CELEBRATION_PLANNING','EVENT_ESSENTIALS','EVENT_REUSE','LIVE_WALL','SHAGUN_REGISTER','GIFT_LEDGER','EVENT_FUNCTIONS','CO_HOSTS','WHATSAPP_MESSAGING',
  'OCCASION_REMINDERS','HOST_REFERRALS','YEARLY_REHOST','MEMORY_BOOK','VENDOR_DIRECTORY','EXPERIENCE_GIFTS',
  'PLANNING_CHECKLIST','EVENT_BUDGET','RUN_SHEET','CATERING_HEADCOUNT','GUEST_CHECKIN','LIVE_UPDATES','RETURN_GIFTS',
  'TRY_WITHOUT_ACCOUNT','STORY_CARDS','PARTNER_ACCOUNTS','GROUP_COLLECTIONS','GUEST_FIRST_FREE','CELEBRATION_SHOWCASE',
  'SEATING_PLAN','GUEST_PASSES','HOST_NOTIFICATIONS'));
INSERT INTO platform_module_controls(module_code,enabled) VALUES ('SEATING_PLAN',FALSE),('GUEST_PASSES',FALSE),('HOST_NOTIFICATIONS',FALSE)
 ON CONFLICT (module_code) DO NOTHING;
ALTER TABLE platform_event_module_grants DROP CONSTRAINT event_module_supported;
ALTER TABLE platform_event_module_grants ADD CONSTRAINT event_module_supported CHECK
 (module_code IN ('CELEBRATION_PLANNING','EVENT_ESSENTIALS','EVENT_REUSE','LIVE_WALL','SHAGUN_REGISTER','EVENT_FUNCTIONS','CO_HOSTS','WHATSAPP_MESSAGING',
  'MEMORY_BOOK','EXPERIENCE_GIFTS','PLANNING_CHECKLIST','EVENT_BUDGET','RUN_SHEET','CATERING_HEADCOUNT','GUEST_CHECKIN','LIVE_UPDATES','RETURN_GIFTS',
  'STORY_CARDS','PARTNER_ACCOUNTS','GROUP_COLLECTIONS','GUEST_FIRST_FREE','CELEBRATION_SHOWCASE','SEATING_PLAN','GUEST_PASSES'));

-- Seating planner
CREATE TABLE event_seating_tables (
 id UUID PRIMARY KEY,
 event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
 name VARCHAR(60) NOT NULL CHECK (length(btrim(name)) > 0),
 capacity INTEGER NOT NULL CHECK (capacity BETWEEN 1 AND 200),
 shape VARCHAR(8) NOT NULL DEFAULT 'ROUND' CHECK (shape IN ('ROUND','LONG')),
 sort_order INTEGER NOT NULL DEFAULT 0,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_seating_tables ON event_seating_tables(event_id, sort_order);
CREATE TABLE event_seat_assignments (
 id UUID PRIMARY KEY,
 event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
 table_id UUID NOT NULL REFERENCES event_seating_tables(id) ON DELETE CASCADE,
 guest_name VARCHAR(120) NOT NULL,
 guest_key VARCHAR(120) NOT NULL,
 party_size INTEGER NOT NULL CHECK (party_size BETWEEN 1 AND 30),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 CONSTRAINT uq_seat_guest UNIQUE (event_id, guest_key)
);
CREATE TABLE event_seating_settings (
 event_id UUID PRIMARY KEY REFERENCES events(id) ON DELETE CASCADE,
 guest_lookup BOOLEAN NOT NULL DEFAULT FALSE
);

-- Personal guest passes: one QR per family for fast check-in. The short code is what door staff type if the camera can't scan.
CREATE TABLE event_guest_passes (
 id UUID PRIMARY KEY,
 event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
 guest_name VARCHAR(120) NOT NULL,
 guest_key VARCHAR(120) NOT NULL,
 party_size INTEGER NOT NULL CHECK (party_size BETWEEN 1 AND 30),
 pass_token UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
 short_code CHAR(6) NOT NULL CHECK (short_code ~ '^[A-HJ-NP-Z2-9]{6}$'),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 revoked_at TIMESTAMPTZ,
 CONSTRAINT uq_pass_guest UNIQUE (event_id, guest_key),
 CONSTRAINT uq_pass_code UNIQUE (event_id, short_code)
);

-- Host notifications: one digest email per host per day at most.
CREATE TABLE host_digest_log (
 user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 sent_on DATE NOT NULL,
 items INTEGER NOT NULL,
 PRIMARY KEY (user_id, sent_on)
);

-- Acquisition measurement: business events only (no tracking cookies), so results don't depend on analytics consent.
CREATE TABLE acquisition_events (
 id UUID PRIMARY KEY,
 kind VARCHAR(30) NOT NULL CHECK (kind IN ('TRY_STARTED','TRY_CLAIMED','STORY_CARD_SHARED','OFFER_CLAIMED','HANDOVER_ACCEPTED','COLLECTION_CREATED','SHOWCASE_APPROVED','PASS_SCANNED')),
 user_id UUID REFERENCES users(id) ON DELETE SET NULL,
 event_id UUID REFERENCES events(id) ON DELETE SET NULL,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_acquisition_events ON acquisition_events(kind, created_at DESC);
CREATE TABLE acquisition_event_limits (
 fingerprint CHAR(64) NOT NULL,
 kind VARCHAR(30) NOT NULL,
 day DATE NOT NULL,
 count INTEGER NOT NULL,
 PRIMARY KEY (fingerprint, kind, day)
);

-- Allow the daily host digest in the email outbox.
ALTER TABLE transactional_email_outbox DROP CONSTRAINT IF EXISTS transactional_email_outbox_template_key_check;
ALTER TABLE transactional_email_outbox ADD CONSTRAINT transactional_email_outbox_template_key_check
 CHECK (template_key IN ('registration','event-invitation','contribution-received','payment-success','event-completed','attention-digest'));
