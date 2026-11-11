-- Batch G5: event-management tools. All opt-in (platform + host + event), all off by default.
ALTER TABLE platform_module_controls DROP CONSTRAINT module_code_allowed;
ALTER TABLE platform_module_controls ADD CONSTRAINT module_code_allowed CHECK
 (module_code IN ('GROUP_GIFTING','GUEST_GIFTS','RSVP','GUEST_ATTENDANCE','WHATSAPP_SHARING','GUESTBOOK','SOCIAL_SHARING','MEMORY_ALBUM','AI_INVITATION',
  'CELEBRATION_PLANNING','EVENT_ESSENTIALS','EVENT_REUSE','LIVE_WALL','SHAGUN_REGISTER','GIFT_LEDGER','EVENT_FUNCTIONS','CO_HOSTS','WHATSAPP_MESSAGING',
  'OCCASION_REMINDERS','HOST_REFERRALS','YEARLY_REHOST','MEMORY_BOOK','VENDOR_DIRECTORY','EXPERIENCE_GIFTS',
  'PLANNING_CHECKLIST','EVENT_BUDGET','RUN_SHEET','CATERING_HEADCOUNT','GUEST_CHECKIN','LIVE_UPDATES','RETURN_GIFTS'));
INSERT INTO platform_module_controls(module_code,enabled) VALUES
 ('PLANNING_CHECKLIST',FALSE),('EVENT_BUDGET',FALSE),('RUN_SHEET',FALSE),('CATERING_HEADCOUNT',FALSE),('GUEST_CHECKIN',FALSE),('LIVE_UPDATES',FALSE),('RETURN_GIFTS',FALSE)
 ON CONFLICT (module_code) DO NOTHING;
ALTER TABLE platform_event_module_grants DROP CONSTRAINT event_module_supported;
ALTER TABLE platform_event_module_grants ADD CONSTRAINT event_module_supported CHECK
 (module_code IN ('CELEBRATION_PLANNING','EVENT_ESSENTIALS','EVENT_REUSE','LIVE_WALL','SHAGUN_REGISTER','EVENT_FUNCTIONS','CO_HOSTS','WHATSAPP_MESSAGING',
  'MEMORY_BOOK','EXPERIENCE_GIFTS','PLANNING_CHECKLIST','EVENT_BUDGET','RUN_SHEET','CATERING_HEADCOUNT','GUEST_CHECKIN','LIVE_UPDATES','RETURN_GIFTS'));

-- 1. Planning checklist
CREATE TABLE event_tasks (
 id UUID PRIMARY KEY,
 event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
 title VARCHAR(160) NOT NULL CHECK (length(btrim(title)) > 0),
 category VARCHAR(40),
 due_date DATE,
 assignee_id UUID REFERENCES users(id) ON DELETE SET NULL,
 note VARCHAR(500),
 done_at TIMESTAMPTZ,
 done_by UUID REFERENCES users(id) ON DELETE SET NULL,
 created_by UUID NOT NULL REFERENCES users(id),
 version BIGINT NOT NULL DEFAULT 1,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_event_tasks ON event_tasks(event_id, done_at NULLS FIRST, due_date NULLS LAST);

-- 2. Budget and vendor payments (owner only)
CREATE TABLE event_budget_lines (
 id UUID PRIMARY KEY,
 event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
 category VARCHAR(40) NOT NULL,
 item VARCHAR(120) NOT NULL CHECK (length(btrim(item)) > 0),
 vendor_name VARCHAR(120),
 planned_paise BIGINT NOT NULL DEFAULT 0 CHECK (planned_paise BETWEEN 0 AND 100000000000),
 committed_paise BIGINT NOT NULL DEFAULT 0 CHECK (committed_paise BETWEEN 0 AND 100000000000),
 paid_paise BIGINT NOT NULL DEFAULT 0 CHECK (paid_paise BETWEEN 0 AND 100000000000),
 due_date DATE,
 note VARCHAR(300),
 version BIGINT NOT NULL DEFAULT 1,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_budget_lines ON event_budget_lines(event_id, category);
CREATE TABLE event_budget_settings (
 event_id UUID PRIMARY KEY REFERENCES events(id) ON DELETE CASCADE,
 total_paise BIGINT CHECK (total_paise IS NULL OR total_paise BETWEEN 0 AND 100000000000)
);

-- 3. Day-of run sheet, with a rotatable read-only link for vendors
CREATE TABLE event_run_items (
 id UUID PRIMARY KEY,
 event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
 function_id UUID REFERENCES event_functions(id) ON DELETE SET NULL,
 starts_at TIME NOT NULL,
 run_date DATE,
 title VARCHAR(140) NOT NULL CHECK (length(btrim(title)) > 0),
 owner_name VARCHAR(80),
 location VARCHAR(120),
 note VARCHAR(400),
 done_at TIMESTAMPTZ,
 version BIGINT NOT NULL DEFAULT 1
);
CREATE INDEX idx_run_items ON event_run_items(event_id, run_date NULLS FIRST, starts_at);
CREATE TABLE event_run_sheet_links (
 event_id UUID PRIMARY KEY REFERENCES events(id) ON DELETE CASCADE,
 share_token UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
 shared BOOLEAN NOT NULL DEFAULT FALSE
);

-- 4. Catering headcount: meal choice on each function answer
ALTER TABLE event_function_rsvps ADD COLUMN meal_choice VARCHAR(10) CHECK (meal_choice IN ('VEG','JAIN','NON_VEG','VEGAN','NONE'));

-- 5. Entry check-in
CREATE TABLE event_checkins (
 id UUID PRIMARY KEY,
 event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
 guest_name VARCHAR(120) NOT NULL CHECK (length(btrim(guest_name)) > 0),
 guest_key VARCHAR(120) NOT NULL,
 party_size INTEGER NOT NULL CHECK (party_size BETWEEN 1 AND 30),
 walk_in BOOLEAN NOT NULL DEFAULT FALSE,
 checked_in_by UUID NOT NULL REFERENCES users(id),
 checked_in_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 CONSTRAINT uq_checkin_guest UNIQUE (event_id, guest_key)
);

-- 6. Live updates and livestream link
CREATE TABLE event_live_updates (
 id UUID PRIMARY KEY,
 event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
 message VARCHAR(200) NOT NULL CHECK (length(btrim(message)) > 0),
 posted_by UUID NOT NULL REFERENCES users(id),
 posted_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 removed_at TIMESTAMPTZ
);
CREATE INDEX idx_live_updates ON event_live_updates(event_id, posted_at DESC) WHERE removed_at IS NULL;
CREATE TABLE event_stream_links (
 event_id UUID PRIMARY KEY REFERENCES events(id) ON DELETE CASCADE,
 url VARCHAR(500) NOT NULL CHECK (url ~ '^https://'),
 label VARCHAR(80)
);

-- 7. Return gifts on register entries
ALTER TABLE event_shagun_entries ADD COLUMN return_gift_at TIMESTAMPTZ;
