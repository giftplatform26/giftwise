-- Batch E1: guest list with households (families). Opt-in at platform, host and event level; off by default.
-- When on, households also feed the seating planner, guest passes and check-in through the "expected guests" list.
ALTER TABLE platform_module_controls DROP CONSTRAINT module_code_allowed;
ALTER TABLE platform_module_controls ADD CONSTRAINT module_code_allowed CHECK
 (module_code IN ('GROUP_GIFTING','GUEST_GIFTS','RSVP','GUEST_ATTENDANCE','WHATSAPP_SHARING','GUESTBOOK','SOCIAL_SHARING','MEMORY_ALBUM','AI_INVITATION',
  'CELEBRATION_PLANNING','EVENT_ESSENTIALS','EVENT_REUSE','LIVE_WALL','SHAGUN_REGISTER','GIFT_LEDGER','EVENT_FUNCTIONS','CO_HOSTS','WHATSAPP_MESSAGING',
  'OCCASION_REMINDERS','HOST_REFERRALS','YEARLY_REHOST','MEMORY_BOOK','VENDOR_DIRECTORY','EXPERIENCE_GIFTS',
  'PLANNING_CHECKLIST','EVENT_BUDGET','RUN_SHEET','CATERING_HEADCOUNT','GUEST_CHECKIN','LIVE_UPDATES','RETURN_GIFTS',
  'TRY_WITHOUT_ACCOUNT','STORY_CARDS','PARTNER_ACCOUNTS','GROUP_COLLECTIONS','GUEST_FIRST_FREE','CELEBRATION_SHOWCASE',
  'SEATING_PLAN','GUEST_PASSES','HOST_NOTIFICATIONS',
  'GUEST_HOUSEHOLDS'));
INSERT INTO platform_module_controls(module_code,enabled) VALUES ('GUEST_HOUSEHOLDS',FALSE) ON CONFLICT (module_code) DO NOTHING;
ALTER TABLE platform_event_module_grants DROP CONSTRAINT event_module_supported;
ALTER TABLE platform_event_module_grants ADD CONSTRAINT event_module_supported CHECK
 (module_code IN ('CELEBRATION_PLANNING','EVENT_ESSENTIALS','EVENT_REUSE','LIVE_WALL','SHAGUN_REGISTER','EVENT_FUNCTIONS','CO_HOSTS','WHATSAPP_MESSAGING',
  'MEMORY_BOOK','EXPERIENCE_GIFTS','PLANNING_CHECKLIST','EVENT_BUDGET','RUN_SHEET','CATERING_HEADCOUNT','GUEST_CHECKIN','LIVE_UPDATES','RETURN_GIFTS',
  'STORY_CARDS','PARTNER_ACCOUNTS','GROUP_COLLECTIONS','GUEST_FIRST_FREE','CELEBRATION_SHOWCASE','SEATING_PLAN','GUEST_PASSES',
  'GUEST_HOUSEHOLDS'));

-- One row per family / household. name_key is the normalised name used to match seating, passes and check-in.
CREATE TABLE event_households (
 id UUID PRIMARY KEY,
 event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
 name VARCHAR(120) NOT NULL CHECK (length(btrim(name)) > 0),
 name_key VARCHAR(120) NOT NULL CHECK (length(name_key) > 0),
 side VARCHAR(10) CHECK (side IN ('BRIDE','GROOM','HOST','FAMILY','FRIENDS','WORK','OTHER')),
 group_tag VARCHAR(40),
 phone VARCHAR(16) CHECK (phone ~ '^\+?[0-9]{7,15}$'),
 email VARCHAR(160),
 city VARCHAR(80),
 outstation BOOLEAN NOT NULL DEFAULT FALSE,
 expected_size INTEGER NOT NULL DEFAULT 1 CHECK (expected_size BETWEEN 1 AND 30),
 invite_status VARCHAR(10) NOT NULL DEFAULT 'NOT_SENT' CHECK (invite_status IN ('NOT_SENT','SENT')),
 rsvp VARCHAR(8) NOT NULL DEFAULT 'PENDING' CHECK (rsvp IN ('PENDING','YES','NO','MAYBE')),
 note VARCHAR(300),
 version BIGINT NOT NULL DEFAULT 1,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 CONSTRAINT uq_household_name UNIQUE (event_id, name_key)
);
CREATE INDEX idx_households_event ON event_households(event_id, side, name_key);

-- People in a household. When a household lists members, its party size is the number attending.
CREATE TABLE event_household_members (
 id UUID PRIMARY KEY,
 household_id UUID NOT NULL REFERENCES event_households(id) ON DELETE CASCADE,
 event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
 name VARCHAR(120) NOT NULL CHECK (length(btrim(name)) > 0),
 age_group VARCHAR(6) NOT NULL DEFAULT 'ADULT' CHECK (age_group IN ('ADULT','CHILD','INFANT')),
 meal VARCHAR(10) CHECK (meal IN ('VEG','JAIN','NON_VEG','VEGAN')),
 attending BOOLEAN NOT NULL DEFAULT TRUE,
 sort_order INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX idx_household_members ON event_household_members(household_id, sort_order);
