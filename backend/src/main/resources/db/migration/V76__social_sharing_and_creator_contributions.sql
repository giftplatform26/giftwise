-- New social sharing requires an explicit platform toggle and host grant.
ALTER TABLE platform_module_controls DROP CONSTRAINT module_code_allowed;
ALTER TABLE platform_module_controls ADD CONSTRAINT module_code_allowed CHECK
 (module_code IN ('GROUP_GIFTING','RSVP','GUEST_ATTENDANCE','WHATSAPP_SHARING','GUESTBOOK','SOCIAL_SHARING'));
INSERT INTO platform_module_controls(module_code,enabled) VALUES('SOCIAL_SHARING',FALSE);
ALTER TABLE gift_group_guest_reports ADD COLUMN creator_admin_id UUID REFERENCES gift_group_accounts(id);
ALTER TABLE gift_groups ADD CONSTRAINT uq_group_creator_identity UNIQUE(id,admin_id);
ALTER TABLE gift_group_guest_reports ADD CONSTRAINT fk_group_report_creator
 FOREIGN KEY(group_id,creator_admin_id) REFERENCES gift_groups(id,admin_id);
ALTER TABLE gift_group_guest_reports ADD CONSTRAINT group_report_contributor_identity
 CHECK (member_id IS NULL OR creator_admin_id IS NULL);
CREATE INDEX group_reports_creator_idx ON gift_group_guest_reports(group_id,creator_admin_id,created_at DESC)
 WHERE creator_admin_id IS NOT NULL;
