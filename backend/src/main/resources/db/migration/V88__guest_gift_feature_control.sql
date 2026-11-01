-- Guest-added gifts use the same audited platform and host module controls.
ALTER TABLE platform_module_controls DROP CONSTRAINT module_code_allowed;
ALTER TABLE platform_module_controls ADD CONSTRAINT module_code_allowed CHECK
 (module_code IN ('GROUP_GIFTING','GUEST_GIFTS','RSVP','GUEST_ATTENDANCE','WHATSAPP_SHARING','GUESTBOOK','SOCIAL_SHARING'));
INSERT INTO platform_module_controls(module_code,enabled) VALUES('GUEST_GIFTS',FALSE);

-- Preserve eligibility for existing group-gifting hosts when the platform switch is enabled.
INSERT INTO platform_host_module_grants(host_id,module_code,enabled,changed_by)
 SELECT host_id,'GUEST_GIFTS',enabled,changed_by FROM platform_host_module_grants
 WHERE module_code='GROUP_GIFTING';
