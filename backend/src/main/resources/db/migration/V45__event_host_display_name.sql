ALTER TABLE events ADD COLUMN host_display_name VARCHAR(200);
UPDATE events SET host_display_name = COALESCE(NULLIF(BTRIM(contact_name), ''), 'Event Host');
ALTER TABLE events ALTER COLUMN host_display_name SET NOT NULL;
ALTER TABLE events ADD CONSTRAINT events_host_display_name_valid CHECK (length(BTRIM(host_display_name)) BETWEEN 1 AND 200);
UPDATE event_thank_you_settings t SET host_display_name = e.host_display_name FROM events e WHERE t.event_id = e.id;
