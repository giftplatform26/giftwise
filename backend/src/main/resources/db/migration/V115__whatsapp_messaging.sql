-- Batch G3: WhatsApp invitations, reminders and thank-yous with reply-to-RSVP.
-- Default provider is a local outbox (nothing leaves the server) until WhatsApp Business credentials are configured.
ALTER TABLE platform_module_controls DROP CONSTRAINT module_code_allowed;
ALTER TABLE platform_module_controls ADD CONSTRAINT module_code_allowed CHECK
 (module_code IN ('GROUP_GIFTING','GUEST_GIFTS','RSVP','GUEST_ATTENDANCE','WHATSAPP_SHARING','GUESTBOOK','SOCIAL_SHARING','MEMORY_ALBUM','AI_INVITATION',
  'CELEBRATION_PLANNING','EVENT_ESSENTIALS','EVENT_REUSE','LIVE_WALL','SHAGUN_REGISTER','GIFT_LEDGER','EVENT_FUNCTIONS','CO_HOSTS','WHATSAPP_MESSAGING'));
INSERT INTO platform_module_controls(module_code,enabled) VALUES ('WHATSAPP_MESSAGING',FALSE) ON CONFLICT (module_code) DO NOTHING;
ALTER TABLE platform_event_module_grants DROP CONSTRAINT event_module_supported;
ALTER TABLE platform_event_module_grants ADD CONSTRAINT event_module_supported CHECK
 (module_code IN ('CELEBRATION_PLANNING','EVENT_ESSENTIALS','EVENT_REUSE','LIVE_WALL','SHAGUN_REGISTER','EVENT_FUNCTIONS','CO_HOSTS','WHATSAPP_MESSAGING'));

-- Guests the host can message. Phone is E.164 (e.g. +919876543210).
CREATE TABLE event_guest_contacts (
 id UUID PRIMARY KEY,
 event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
 name VARCHAR(120) NOT NULL CHECK (length(btrim(name)) > 0),
 phone VARCHAR(16) NOT NULL CHECK (phone ~ '^\+[1-9][0-9]{7,14}$'),
 opted_out_at TIMESTAMPTZ,
 reply_attendance VARCHAR(8) CHECK (reply_attendance IN ('YES','NO','MAYBE')),
 reply_party_size INTEGER CHECK (reply_party_size BETWEEN 0 AND 10),
 replied_at TIMESTAMPTZ,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 CONSTRAINT uq_event_contact_phone UNIQUE (event_id, phone)
);
CREATE INDEX idx_contacts_phone ON event_guest_contacts(phone);

CREATE TABLE whatsapp_messages (
 id UUID PRIMARY KEY,
 event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
 contact_id UUID NOT NULL REFERENCES event_guest_contacts(id) ON DELETE CASCADE,
 kind VARCHAR(12) NOT NULL CHECK (kind IN ('INVITATION','REMINDER','THANK_YOU')),
 body TEXT NOT NULL,
 template_params TEXT NOT NULL,
 provider VARCHAR(16) NOT NULL,
 status VARCHAR(12) NOT NULL DEFAULT 'QUEUED' CHECK (status IN ('QUEUED','SENDING','SENT','DELIVERED','READ','FAILED')),
 attempts INTEGER NOT NULL DEFAULT 0,
 provider_message_id VARCHAR(128) UNIQUE,
 error_code VARCHAR(60),
 queued_by UUID NOT NULL REFERENCES users(id),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 sent_at TIMESTAMPTZ,
 updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_wa_queue ON whatsapp_messages(status, next_attempt_at) WHERE status IN ('QUEUED','SENDING');
CREATE INDEX idx_wa_event ON whatsapp_messages(event_id, created_at DESC);
-- One message of each kind per guest per day, so a double click never spams anyone.
CREATE UNIQUE INDEX ux_wa_once_per_day ON whatsapp_messages(contact_id, kind, ((created_at AT TIME ZONE 'UTC')::date));

-- Raw inbound replies we could not attach to a guest are kept briefly for support, then purged.
CREATE TABLE whatsapp_inbound (
 id UUID PRIMARY KEY,
 provider_message_id VARCHAR(128) NOT NULL UNIQUE,
 phone VARCHAR(16) NOT NULL,
 text VARCHAR(500),
 matched_contacts INTEGER NOT NULL DEFAULT 0,
 received_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
