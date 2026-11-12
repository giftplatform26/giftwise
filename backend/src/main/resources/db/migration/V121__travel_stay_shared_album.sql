-- Batch E2: travel & stay for outstation guests, and a shared photo album guests add to by scanning a QR. Opt-in, off by default.
ALTER TABLE platform_module_controls DROP CONSTRAINT module_code_allowed;
ALTER TABLE platform_module_controls ADD CONSTRAINT module_code_allowed CHECK
 (module_code IN ('GROUP_GIFTING','GUEST_GIFTS','RSVP','GUEST_ATTENDANCE','WHATSAPP_SHARING','GUESTBOOK','SOCIAL_SHARING','MEMORY_ALBUM','AI_INVITATION',
  'CELEBRATION_PLANNING','EVENT_ESSENTIALS','EVENT_REUSE','LIVE_WALL','SHAGUN_REGISTER','GIFT_LEDGER','EVENT_FUNCTIONS','CO_HOSTS','WHATSAPP_MESSAGING',
  'OCCASION_REMINDERS','HOST_REFERRALS','YEARLY_REHOST','MEMORY_BOOK','VENDOR_DIRECTORY','EXPERIENCE_GIFTS',
  'PLANNING_CHECKLIST','EVENT_BUDGET','RUN_SHEET','CATERING_HEADCOUNT','GUEST_CHECKIN','LIVE_UPDATES','RETURN_GIFTS',
  'TRY_WITHOUT_ACCOUNT','STORY_CARDS','PARTNER_ACCOUNTS','GROUP_COLLECTIONS','GUEST_FIRST_FREE','CELEBRATION_SHOWCASE',
  'SEATING_PLAN','GUEST_PASSES','HOST_NOTIFICATIONS',
  'GUEST_HOUSEHOLDS','GUEST_TRAVEL','SHARED_ALBUM'));
INSERT INTO platform_module_controls(module_code,enabled) VALUES ('GUEST_TRAVEL',FALSE),('SHARED_ALBUM',FALSE) ON CONFLICT (module_code) DO NOTHING;
ALTER TABLE platform_event_module_grants DROP CONSTRAINT event_module_supported;
ALTER TABLE platform_event_module_grants ADD CONSTRAINT event_module_supported CHECK
 (module_code IN ('CELEBRATION_PLANNING','EVENT_ESSENTIALS','EVENT_REUSE','LIVE_WALL','SHAGUN_REGISTER','EVENT_FUNCTIONS','CO_HOSTS','WHATSAPP_MESSAGING',
  'MEMORY_BOOK','EXPERIENCE_GIFTS','PLANNING_CHECKLIST','EVENT_BUDGET','RUN_SHEET','CATERING_HEADCOUNT','GUEST_CHECKIN','LIVE_UPDATES','RETURN_GIFTS',
  'STORY_CARDS','PARTNER_ACCOUNTS','GROUP_COLLECTIONS','GUEST_FIRST_FREE','CELEBRATION_SHOWCASE','SEATING_PLAN','GUEST_PASSES',
  'GUEST_HOUSEHOLDS','GUEST_TRAVEL','SHARED_ALBUM'));

-- Travel & stay. Hotels / guest houses the host has arranged.
CREATE TABLE event_stay_options (
 id UUID PRIMARY KEY,
 event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
 name VARCHAR(120) NOT NULL CHECK (length(btrim(name)) > 0),
 address VARCHAR(300),
 phone VARCHAR(16) CHECK (phone ~ '^\+?[0-9]{7,15}$'),
 map_url VARCHAR(500) CHECK (map_url ~ '^https://'),
 check_in DATE,
 check_out DATE,
 rooms_total INTEGER CHECK (rooms_total BETWEEN 0 AND 2000),
 note VARCHAR(300),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 CONSTRAINT stay_dates CHECK (check_out IS NULL OR check_in IS NULL OR check_out >= check_in)
);
CREATE INDEX idx_stay_options ON event_stay_options(event_id, created_at);

-- One row per travelling family. form_token is the guest's private link to fill in their own arrival and departure.
-- Times are local wall-clock times at the venue (no time zone), as guests read them off their tickets.
CREATE TABLE event_guest_travel (
 id UUID PRIMARY KEY,
 event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
 guest_name VARCHAR(120) NOT NULL CHECK (length(btrim(guest_name)) > 0),
 guest_key VARCHAR(120) NOT NULL,
 party_size INTEGER NOT NULL CHECK (party_size BETWEEN 1 AND 30),
 phone VARCHAR(16) CHECK (phone ~ '^\+?[0-9]{7,15}$'),
 form_token UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
 arrival_at TIMESTAMP,
 arrival_mode VARCHAR(6) CHECK (arrival_mode IN ('FLIGHT','TRAIN','BUS','CAR','OTHER')),
 arrival_ref VARCHAR(40),
 arrival_point VARCHAR(120),
 departure_at TIMESTAMP,
 departure_mode VARCHAR(6) CHECK (departure_mode IN ('FLIGHT','TRAIN','BUS','CAR','OTHER')),
 departure_ref VARCHAR(40),
 departure_point VARCHAR(120),
 pickup_needed BOOLEAN NOT NULL DEFAULT FALSE,
 drop_needed BOOLEAN NOT NULL DEFAULT FALSE,
 stay_id UUID REFERENCES event_stay_options(id) ON DELETE SET NULL,
 room_label VARCHAR(40),
 rooms INTEGER NOT NULL DEFAULT 1 CHECK (rooms BETWEEN 0 AND 20),
 guest_note VARCHAR(300),
 host_note VARCHAR(300),
 submitted_at TIMESTAMPTZ,
 version BIGINT NOT NULL DEFAULT 1,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 CONSTRAINT uq_travel_guest UNIQUE (event_id, guest_key),
 CONSTRAINT travel_dates CHECK (departure_at IS NULL OR arrival_at IS NULL OR departure_at >= arrival_at)
);
CREATE INDEX idx_guest_travel_arrival ON event_guest_travel(event_id, arrival_at);

-- Shared photo album. upload_token is in the QR guests scan; rotating it stops new uploads from old printed QRs.
CREATE TABLE event_shared_albums (
 event_id UUID PRIMARY KEY REFERENCES events(id) ON DELETE CASCADE,
 upload_token UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
 uploads_open BOOLEAN NOT NULL DEFAULT TRUE,
 moderation VARCHAR(6) NOT NULL DEFAULT 'AUTO' CHECK (moderation IN ('AUTO','REVIEW')),
 guests_can_view BOOLEAN NOT NULL DEFAULT TRUE,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE event_shared_photos (
 id UUID PRIMARY KEY,
 event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
 uploader_name VARCHAR(80),
 caption VARCHAR(200),
 file_name VARCHAR(64) NOT NULL CHECK (file_name ~ '^[0-9a-f-]{36}\.jpg$'),
 thumb_name VARCHAR(64) NOT NULL CHECK (thumb_name ~ '^[0-9a-f-]{36}\.jpg$'),
 bytes INTEGER NOT NULL CHECK (bytes > 0),
 status VARCHAR(8) NOT NULL CHECK (status IN ('APPROVED','PENDING','HIDDEN')),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_shared_photos ON event_shared_photos(event_id, status, created_at DESC);
