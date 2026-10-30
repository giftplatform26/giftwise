-- Privileges are grants, not financial/payment verification.
CREATE TABLE IF NOT EXISTS platform_module_controls (
 module_code VARCHAR(40) PRIMARY KEY,
 enabled BOOLEAN NOT NULL DEFAULT FALSE,
 changed_by UUID REFERENCES users(id),
 changed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 CONSTRAINT module_code_allowed CHECK(module_code IN ('GROUP_GIFTING','RSVP','GUEST_ATTENDANCE','WHATSAPP_SHARING','GUESTBOOK'))
);
INSERT INTO platform_module_controls(module_code,enabled) VALUES
 ('GROUP_GIFTING',TRUE),('RSVP',FALSE),('GUEST_ATTENDANCE',FALSE),('WHATSAPP_SHARING',FALSE),('GUESTBOOK',FALSE)
ON CONFLICT(module_code) DO NOTHING;
CREATE TABLE IF NOT EXISTS platform_host_module_grants (
 host_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 module_code VARCHAR(40) NOT NULL REFERENCES platform_module_controls(module_code),
 enabled BOOLEAN NOT NULL DEFAULT FALSE,
 changed_by UUID REFERENCES users(id),
 changed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 PRIMARY KEY(host_id,module_code)
);
CREATE TABLE IF NOT EXISTS platform_event_message_settings (
 event_id UUID PRIMARY KEY REFERENCES events(id) ON DELETE CASCADE,
 display_mode VARCHAR(20) NOT NULL DEFAULT 'AUTO' CHECK(display_mode IN ('AUTO','APPROVAL')),
 changed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS platform_admin_action_audit (
 id UUID PRIMARY KEY,
 actor_id UUID NOT NULL REFERENCES users(id),
 action VARCHAR(60) NOT NULL,
 target_id UUID,
 module_code VARCHAR(40),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
