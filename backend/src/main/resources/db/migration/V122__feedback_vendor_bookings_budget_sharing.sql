-- Batch E3: guest feedback after the event, vendor quotes & bookings, and sharing the budget with family. Opt-in, off by default.
ALTER TABLE platform_module_controls DROP CONSTRAINT module_code_allowed;
ALTER TABLE platform_module_controls ADD CONSTRAINT module_code_allowed CHECK
 (module_code IN ('GROUP_GIFTING','GUEST_GIFTS','RSVP','GUEST_ATTENDANCE','WHATSAPP_SHARING','GUESTBOOK','SOCIAL_SHARING','MEMORY_ALBUM','AI_INVITATION',
  'CELEBRATION_PLANNING','EVENT_ESSENTIALS','EVENT_REUSE','LIVE_WALL','SHAGUN_REGISTER','GIFT_LEDGER','EVENT_FUNCTIONS','CO_HOSTS','WHATSAPP_MESSAGING',
  'OCCASION_REMINDERS','HOST_REFERRALS','YEARLY_REHOST','MEMORY_BOOK','VENDOR_DIRECTORY','EXPERIENCE_GIFTS',
  'PLANNING_CHECKLIST','EVENT_BUDGET','RUN_SHEET','CATERING_HEADCOUNT','GUEST_CHECKIN','LIVE_UPDATES','RETURN_GIFTS',
  'TRY_WITHOUT_ACCOUNT','STORY_CARDS','PARTNER_ACCOUNTS','GROUP_COLLECTIONS','GUEST_FIRST_FREE','CELEBRATION_SHOWCASE',
  'SEATING_PLAN','GUEST_PASSES','HOST_NOTIFICATIONS',
  'GUEST_HOUSEHOLDS','GUEST_TRAVEL','SHARED_ALBUM','GUEST_FEEDBACK','VENDOR_BOOKINGS','BUDGET_SHARING'));
INSERT INTO platform_module_controls(module_code,enabled) VALUES ('GUEST_FEEDBACK',FALSE),('VENDOR_BOOKINGS',FALSE),('BUDGET_SHARING',FALSE) ON CONFLICT (module_code) DO NOTHING;
ALTER TABLE platform_event_module_grants DROP CONSTRAINT event_module_supported;
ALTER TABLE platform_event_module_grants ADD CONSTRAINT event_module_supported CHECK
 (module_code IN ('CELEBRATION_PLANNING','EVENT_ESSENTIALS','EVENT_REUSE','LIVE_WALL','SHAGUN_REGISTER','EVENT_FUNCTIONS','CO_HOSTS','WHATSAPP_MESSAGING',
  'MEMORY_BOOK','EXPERIENCE_GIFTS','PLANNING_CHECKLIST','EVENT_BUDGET','RUN_SHEET','CATERING_HEADCOUNT','GUEST_CHECKIN','LIVE_UPDATES','RETURN_GIFTS',
  'STORY_CARDS','PARTNER_ACCOUNTS','GROUP_COLLECTIONS','GUEST_FIRST_FREE','CELEBRATION_SHOWCASE','SEATING_PLAN','GUEST_PASSES',
  'GUEST_HOUSEHOLDS','GUEST_TRAVEL','SHARED_ALBUM','GUEST_FEEDBACK','VENDOR_BOOKINGS','BUDGET_SHARING'));

-- Guest feedback. Collected only while the host has it open; guests answer from the invitation, no account needed.
CREATE TABLE event_feedback_settings (
 event_id UUID PRIMARY KEY REFERENCES events(id) ON DELETE CASCADE,
 collecting BOOLEAN NOT NULL DEFAULT FALSE,
 question VARCHAR(200)
);
CREATE TABLE event_feedback (
 id UUID PRIMARY KEY,
 event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
 guest_name VARCHAR(80),
 overall SMALLINT NOT NULL CHECK (overall BETWEEN 1 AND 5),
 food SMALLINT CHECK (food BETWEEN 1 AND 5),
 venue SMALLINT CHECK (venue BETWEEN 1 AND 5),
 arrangements SMALLINT CHECK (arrangements BETWEEN 1 AND 5),
 hospitality SMALLINT CHECK (hospitality BETWEEN 1 AND 5),
 comment VARCHAR(1000),
 answer VARCHAR(500),
 hidden BOOLEAN NOT NULL DEFAULT FALSE,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_event_feedback ON event_feedback(event_id, created_at DESC);

-- Vendor quotes & bookings. A planning record only: GiftWise never pays vendors.
CREATE TABLE event_vendors (
 id UUID PRIMARY KEY,
 event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
 directory_vendor_id UUID REFERENCES platform_vendors(id) ON DELETE SET NULL,
 name VARCHAR(120) NOT NULL CHECK (length(btrim(name)) > 0),
 category VARCHAR(40) NOT NULL,
 contact_name VARCHAR(80),
 phone VARCHAR(16) CHECK (phone ~ '^\+?[0-9]{7,15}$'),
 email VARCHAR(160),
 status VARCHAR(10) NOT NULL DEFAULT 'ENQUIRED' CHECK (status IN ('ENQUIRED','QUOTED','BOOKED','CANCELLED')),
 booked_paise BIGINT NOT NULL DEFAULT 0 CHECK (booked_paise BETWEEN 0 AND 100000000000),
 note VARCHAR(500),
 budget_line_id UUID REFERENCES event_budget_lines(id) ON DELETE SET NULL,
 version BIGINT NOT NULL DEFAULT 1,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_event_vendors ON event_vendors(event_id, category);
CREATE TABLE event_vendor_quotes (
 id UUID PRIMARY KEY,
 vendor_id UUID NOT NULL REFERENCES event_vendors(id) ON DELETE CASCADE,
 event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
 amount_paise BIGINT NOT NULL CHECK (amount_paise BETWEEN 0 AND 100000000000),
 details VARCHAR(600),
 valid_until DATE,
 status VARCHAR(8) NOT NULL DEFAULT 'RECEIVED' CHECK (status IN ('RECEIVED','ACCEPTED','REJECTED')),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_vendor_quotes ON event_vendor_quotes(vendor_id, created_at);
CREATE TABLE event_vendor_payments (
 id UUID PRIMARY KEY,
 vendor_id UUID NOT NULL REFERENCES event_vendors(id) ON DELETE CASCADE,
 event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
 label VARCHAR(60) NOT NULL CHECK (length(btrim(label)) > 0),
 amount_paise BIGINT NOT NULL CHECK (amount_paise BETWEEN 1 AND 100000000000),
 due_date DATE,
 paid_on DATE,
 note VARCHAR(200),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_vendor_payments ON event_vendor_payments(vendor_id, due_date NULLS LAST);

-- Budget sharing: family members the owner lets see (VIEW) or change (EDIT) the budget and vendor bookings.
CREATE TABLE event_budget_members (
 event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
 user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 access VARCHAR(4) NOT NULL CHECK (access IN ('VIEW','EDIT')),
 added_by UUID NOT NULL REFERENCES users(id),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 PRIMARY KEY (event_id, user_id)
);
CREATE INDEX idx_budget_members_user ON event_budget_members(user_id);
CREATE TABLE event_budget_invites (
 id UUID PRIMARY KEY,
 event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
 token_hash CHAR(64) NOT NULL UNIQUE,
 access VARCHAR(4) NOT NULL CHECK (access IN ('VIEW','EDIT')),
 label VARCHAR(80) NOT NULL,
 invited_by UUID NOT NULL REFERENCES users(id),
 expires_at TIMESTAMPTZ NOT NULL,
 accepted_by UUID REFERENCES users(id) ON DELETE SET NULL,
 accepted_at TIMESTAMPTZ,
 revoked_at TIMESTAMPTZ,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_budget_invites ON event_budget_invites(event_id, created_at DESC);
