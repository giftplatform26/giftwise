-- Batch G1: live venue wall, shagun register and personal gift ledger.
-- All three are opt-in: platform switch + host privilege, and (for the event features) an event switch.
ALTER TABLE platform_module_controls DROP CONSTRAINT module_code_allowed;
ALTER TABLE platform_module_controls ADD CONSTRAINT module_code_allowed CHECK
 (module_code IN ('GROUP_GIFTING','GUEST_GIFTS','RSVP','GUEST_ATTENDANCE','WHATSAPP_SHARING','GUESTBOOK','SOCIAL_SHARING','MEMORY_ALBUM','AI_INVITATION',
  'CELEBRATION_PLANNING','EVENT_ESSENTIALS','EVENT_REUSE','LIVE_WALL','SHAGUN_REGISTER','GIFT_LEDGER'));
INSERT INTO platform_module_controls(module_code,enabled) VALUES ('LIVE_WALL',FALSE),('SHAGUN_REGISTER',FALSE),('GIFT_LEDGER',FALSE)
 ON CONFLICT (module_code) DO NOTHING;
ALTER TABLE platform_event_module_grants DROP CONSTRAINT event_module_supported;
ALTER TABLE platform_event_module_grants ADD CONSTRAINT event_module_supported CHECK
 (module_code IN ('CELEBRATION_PLANNING','EVENT_ESSENTIALS','EVENT_REUSE','LIVE_WALL','SHAGUN_REGISTER'));

-- Live wall: a separate, rotatable token so the screen at the venue never carries host credentials.
CREATE TABLE event_live_walls (
 event_id UUID PRIMARY KEY REFERENCES events(id) ON DELETE CASCADE,
 wall_token UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
 active BOOLEAN NOT NULL DEFAULT FALSE,
 headline VARCHAR(120),
 theme VARCHAR(12) NOT NULL DEFAULT 'NIGHT' CHECK (theme IN ('NIGHT','IVORY','FESTIVE')),
 show_messages BOOLEAN NOT NULL DEFAULT TRUE,
 show_photos BOOLEAN NOT NULL DEFAULT TRUE,
 show_join_qr BOOLEAN NOT NULL DEFAULT TRUE,
 slide_seconds SMALLINT NOT NULL DEFAULT 8 CHECK (slide_seconds BETWEEN 4 AND 60),
 version BIGINT NOT NULL DEFAULT 0,
 updated_by UUID REFERENCES users(id),
 updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Shagun register: gifts handed over in person (cash, envelopes, items) recorded by the host.
-- These are the host's own notes, never payment confirmations, and never touch the payment ledger.
CREATE TABLE event_shagun_entries (
 id UUID PRIMARY KEY,
 event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
 giver_name VARCHAR(120) NOT NULL CHECK (length(btrim(giver_name)) > 0),
 giver_key VARCHAR(120) NOT NULL,
 giver_phone VARCHAR(16) CHECK (giver_phone IS NULL OR giver_phone ~ '^\+?[0-9]{7,15}$'),
 side VARCHAR(60),
 kind VARCHAR(12) NOT NULL CHECK (kind IN ('CASH','ENVELOPE','UPI','GIFT_ITEM','OTHER')),
 amount_paise BIGINT CHECK (amount_paise IS NULL OR amount_paise BETWEEN 100 AND 100000000000),
 item_description VARCHAR(200),
 note VARCHAR(300),
 client_key VARCHAR(64) NOT NULL,
 recorded_by UUID NOT NULL REFERENCES users(id),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 deleted_at TIMESTAMPTZ,
 version BIGINT NOT NULL DEFAULT 1,
 CONSTRAINT shagun_amount_or_item CHECK (amount_paise IS NOT NULL OR item_description IS NOT NULL),
 CONSTRAINT uq_shagun_client_key UNIQUE (event_id, client_key)
);
CREATE INDEX idx_shagun_event ON event_shagun_entries(event_id, created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_shagun_giver ON event_shagun_entries(giver_key) WHERE deleted_at IS NULL;

-- Personal ledger: gifts this user gave at other people's celebrations.
CREATE TABLE user_gifts_given (
 id UUID PRIMARY KEY,
 user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 recipient_name VARCHAR(120) NOT NULL CHECK (length(btrim(recipient_name)) > 0),
 recipient_key VARCHAR(120) NOT NULL,
 recipient_phone VARCHAR(16) CHECK (recipient_phone IS NULL OR recipient_phone ~ '^\+?[0-9]{7,15}$'),
 occasion VARCHAR(80),
 occasion_date DATE,
 kind VARCHAR(12) NOT NULL CHECK (kind IN ('CASH','ENVELOPE','UPI','GIFT_ITEM','OTHER')),
 amount_paise BIGINT CHECK (amount_paise IS NULL OR amount_paise BETWEEN 100 AND 100000000000),
 item_description VARCHAR(200),
 note VARCHAR(300),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 deleted_at TIMESTAMPTZ,
 version BIGINT NOT NULL DEFAULT 1,
 CONSTRAINT given_amount_or_item CHECK (amount_paise IS NOT NULL OR item_description IS NOT NULL)
);
CREATE INDEX idx_given_user ON user_gifts_given(user_id, occasion_date DESC NULLS LAST, created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_given_recipient ON user_gifts_given(user_id, recipient_key) WHERE deleted_at IS NULL;
