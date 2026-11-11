-- Batch G2: multi-function celebrations (mehendi, sangeet, reception…) and co-hosts / gift-desk helpers.
ALTER TABLE platform_module_controls DROP CONSTRAINT module_code_allowed;
ALTER TABLE platform_module_controls ADD CONSTRAINT module_code_allowed CHECK
 (module_code IN ('GROUP_GIFTING','GUEST_GIFTS','RSVP','GUEST_ATTENDANCE','WHATSAPP_SHARING','GUESTBOOK','SOCIAL_SHARING','MEMORY_ALBUM','AI_INVITATION',
  'CELEBRATION_PLANNING','EVENT_ESSENTIALS','EVENT_REUSE','LIVE_WALL','SHAGUN_REGISTER','GIFT_LEDGER','EVENT_FUNCTIONS','CO_HOSTS'));
INSERT INTO platform_module_controls(module_code,enabled) VALUES ('EVENT_FUNCTIONS',FALSE),('CO_HOSTS',FALSE) ON CONFLICT (module_code) DO NOTHING;
ALTER TABLE platform_event_module_grants DROP CONSTRAINT event_module_supported;
ALTER TABLE platform_event_module_grants ADD CONSTRAINT event_module_supported CHECK
 (module_code IN ('CELEBRATION_PLANNING','EVENT_ESSENTIALS','EVENT_REUSE','LIVE_WALL','SHAGUN_REGISTER','EVENT_FUNCTIONS','CO_HOSTS'));

CREATE TABLE event_functions (
 id UUID PRIMARY KEY,
 event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
 name VARCHAR(80) NOT NULL CHECK (length(btrim(name)) > 0),
 function_date DATE,
 start_time TIME,
 end_time TIME,
 venue_name VARCHAR(120),
 venue_address VARCHAR(300),
 map_url VARCHAR(500) CHECK (map_url IS NULL OR map_url ~ '^https://'),
 dress_code VARCHAR(120),
 notes VARCHAR(600),
 rsvp_enabled BOOLEAN NOT NULL DEFAULT TRUE,
 sort_order INTEGER NOT NULL DEFAULT 0,
 version BIGINT NOT NULL DEFAULT 1,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_event_functions ON event_functions(event_id, sort_order, function_date, start_time);

-- One answer per guest (by e-mail) per function; guests can change their answer.
CREATE TABLE event_function_rsvps (
 id UUID PRIMARY KEY,
 function_id UUID NOT NULL REFERENCES event_functions(id) ON DELETE CASCADE,
 guest_name VARCHAR(120) NOT NULL,
 guest_email VARCHAR(254) NOT NULL,
 attendance VARCHAR(8) NOT NULL CHECK (attendance IN ('YES','NO','MAYBE')),
 party_size INTEGER NOT NULL CHECK (party_size BETWEEN 0 AND 10),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 CONSTRAINT uq_function_rsvp UNIQUE (function_id, guest_email)
);

-- Co-hosts. Payments, receiving accounts, invitation design and deletion stay with the owner.
CREATE TABLE event_co_hosts (
 event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
 user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 role VARCHAR(12) NOT NULL CHECK (role IN ('CO_HOST','GIFT_DESK')),
 invited_by UUID NOT NULL REFERENCES users(id),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 PRIMARY KEY (event_id, user_id)
);
CREATE INDEX idx_co_hosts_user ON event_co_hosts(user_id);
CREATE TABLE event_co_host_invites (
 id UUID PRIMARY KEY,
 event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
 token_hash CHAR(64) NOT NULL UNIQUE,
 role VARCHAR(12) NOT NULL CHECK (role IN ('CO_HOST','GIFT_DESK')),
 label VARCHAR(80) NOT NULL,
 invited_by UUID NOT NULL REFERENCES users(id),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 expires_at TIMESTAMPTZ NOT NULL,
 accepted_by UUID REFERENCES users(id),
 accepted_at TIMESTAMPTZ,
 revoked_at TIMESTAMPTZ
);
CREATE INDEX idx_co_host_invites_event ON event_co_host_invites(event_id, created_at DESC);
