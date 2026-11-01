ALTER TABLE platform_module_controls DROP CONSTRAINT module_code_allowed;
ALTER TABLE platform_module_controls ADD CONSTRAINT module_code_allowed CHECK
 (module_code IN ('GROUP_GIFTING','GUEST_GIFTS','RSVP','GUEST_ATTENDANCE','WHATSAPP_SHARING','GUESTBOOK','SOCIAL_SHARING','MEMORY_ALBUM','AI_INVITATION'));
INSERT INTO platform_module_controls(module_code,enabled) VALUES('AI_INVITATION',FALSE);
CREATE TABLE platform_event_ai_design_settings (
 event_id UUID PRIMARY KEY REFERENCES events(id) ON DELETE CASCADE,
 enabled BOOLEAN NOT NULL DEFAULT FALSE,
 changed_by UUID REFERENCES users(id), changed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
