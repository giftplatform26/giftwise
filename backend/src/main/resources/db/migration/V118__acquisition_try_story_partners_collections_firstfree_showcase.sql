-- Batch G6: customer acquisition. All opt-in and off by default.
ALTER TABLE platform_module_controls DROP CONSTRAINT module_code_allowed;
ALTER TABLE platform_module_controls ADD CONSTRAINT module_code_allowed CHECK
 (module_code IN ('GROUP_GIFTING','GUEST_GIFTS','RSVP','GUEST_ATTENDANCE','WHATSAPP_SHARING','GUESTBOOK','SOCIAL_SHARING','MEMORY_ALBUM','AI_INVITATION',
  'CELEBRATION_PLANNING','EVENT_ESSENTIALS','EVENT_REUSE','LIVE_WALL','SHAGUN_REGISTER','GIFT_LEDGER','EVENT_FUNCTIONS','CO_HOSTS','WHATSAPP_MESSAGING',
  'OCCASION_REMINDERS','HOST_REFERRALS','YEARLY_REHOST','MEMORY_BOOK','VENDOR_DIRECTORY','EXPERIENCE_GIFTS',
  'PLANNING_CHECKLIST','EVENT_BUDGET','RUN_SHEET','CATERING_HEADCOUNT','GUEST_CHECKIN','LIVE_UPDATES','RETURN_GIFTS',
  'TRY_WITHOUT_ACCOUNT','STORY_CARDS','PARTNER_ACCOUNTS','GROUP_COLLECTIONS','GUEST_FIRST_FREE','CELEBRATION_SHOWCASE'));
INSERT INTO platform_module_controls(module_code,enabled) VALUES
 ('TRY_WITHOUT_ACCOUNT',FALSE),('STORY_CARDS',FALSE),('PARTNER_ACCOUNTS',FALSE),('GROUP_COLLECTIONS',FALSE),('GUEST_FIRST_FREE',FALSE),('CELEBRATION_SHOWCASE',FALSE)
 ON CONFLICT (module_code) DO NOTHING;
ALTER TABLE platform_event_module_grants DROP CONSTRAINT event_module_supported;
ALTER TABLE platform_event_module_grants ADD CONSTRAINT event_module_supported CHECK
 (module_code IN ('CELEBRATION_PLANNING','EVENT_ESSENTIALS','EVENT_REUSE','LIVE_WALL','SHAGUN_REGISTER','EVENT_FUNCTIONS','CO_HOSTS','WHATSAPP_MESSAGING',
  'MEMORY_BOOK','EXPERIENCE_GIFTS','PLANNING_CHECKLIST','EVENT_BUDGET','RUN_SHEET','CATERING_HEADCOUNT','GUEST_CHECKIN','LIVE_UPDATES','RETURN_GIFTS',
  'STORY_CARDS','PARTNER_ACCOUNTS','GROUP_COLLECTIONS','GUEST_FIRST_FREE','CELEBRATION_SHOWCASE'));

-- 4. Planner and venue partners
CREATE TABLE partner_profiles (
 user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
 slug VARCHAR(40) NOT NULL UNIQUE CHECK (slug ~ '^[a-z0-9][a-z0-9-]{2,39}$'),
 business_name VARCHAR(120) NOT NULL CHECK (length(btrim(business_name)) > 0),
 kind VARCHAR(16) NOT NULL CHECK (kind IN ('PLANNER','VENUE','DECORATOR','CATERER','PHOTOGRAPHER','OTHER')),
 city VARCHAR(80),
 phone VARCHAR(16) CHECK (phone IS NULL OR phone ~ '^\+[1-9][0-9]{7,14}$'),
 website VARCHAR(300) CHECK (website IS NULL OR website ~ '^https://'),
 brand_color CHAR(7) NOT NULL DEFAULT '#683C55' CHECK (brand_color ~ '^#[0-9A-Fa-f]{6}$'),
 bio VARCHAR(500),
 logo_png BYTEA CHECK (logo_png IS NULL OR octet_length(logo_png) <= 400000),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
-- Events a partner manages; a one-time link hands ownership to the client, the partner stays on as PARTNER co-host.
CREATE TABLE event_partner_links (
 event_id UUID PRIMARY KEY REFERENCES events(id) ON DELETE CASCADE,
 partner_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 client_label VARCHAR(120),
 handover_token_hash CHAR(64) UNIQUE,
 handover_expires_at TIMESTAMPTZ,
 client_id UUID REFERENCES users(id) ON DELETE SET NULL,
 handed_over_at TIMESTAMPTZ,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_partner_links_partner ON event_partner_links(partner_id, created_at DESC);
ALTER TABLE event_co_hosts DROP CONSTRAINT IF EXISTS event_co_hosts_role_check;
ALTER TABLE event_co_hosts ADD CONSTRAINT event_co_hosts_role_check CHECK (role IN ('CO_HOST','GIFT_DESK','PARTNER'));

-- 5. Office and group gifts without a celebration: a light event of kind COLLECTION.
ALTER TABLE events ADD COLUMN kind VARCHAR(12) NOT NULL DEFAULT 'CELEBRATION' CHECK (kind IN ('CELEBRATION','COLLECTION'));
CREATE TABLE event_collections (
 event_id UUID PRIMARY KEY REFERENCES events(id) ON DELETE CASCADE,
 recipient_name VARCHAR(120) NOT NULL,
 occasion_label VARCHAR(60) NOT NULL,
 target_paise BIGINT CHECK (target_paise IS NULL OR target_paise BETWEEN 10000 AND 100000000000),
 closes_on DATE,
 show_progress BOOLEAN NOT NULL DEFAULT TRUE,
 organiser_name VARCHAR(120)
);

-- 8. First celebration free for guests who become hosts: one claim per user, ever.
CREATE TABLE guest_offer_claims (
 user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
 source_event_id UUID REFERENCES events(id) ON DELETE SET NULL,
 order_id UUID REFERENCES commercial_orders(id),
 claimed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 9. Showcase of real celebrations: host consent per event, platform admin approval before anything is public.
CREATE TABLE event_showcase (
 event_id UUID PRIMARY KEY REFERENCES events(id) ON DELETE CASCADE,
 display_title VARCHAR(120) NOT NULL CHECK (length(btrim(display_title)) > 0),
 blurb VARCHAR(300),
 city VARCHAR(80),
 featured_wish_ids UUID[] NOT NULL DEFAULT '{}',
 status VARCHAR(10) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','APPROVED','REJECTED','WITHDRAWN')),
 review_note VARCHAR(300),
 submitted_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 reviewed_by UUID REFERENCES users(id),
 reviewed_at TIMESTAMPTZ,
 CONSTRAINT showcase_max_wishes CHECK (cardinality(featured_wish_ids) <= 6)
);
CREATE INDEX idx_showcase_status ON event_showcase(status, reviewed_at DESC);
