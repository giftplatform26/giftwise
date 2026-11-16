-- How guests can give at an event, chosen by the host when creating it.
-- CONTRIBUTION_AND_GIFTS keeps the existing wishlist + "give toward the celebration" experience (default for every existing event).
-- CONTRIBUTION_ONLY hides the wishlist; guests only give toward the celebration (EVENT_WALLET).
ALTER TABLE events ADD COLUMN gift_mode VARCHAR(30) NOT NULL DEFAULT 'CONTRIBUTION_AND_GIFTS';
ALTER TABLE events ADD CONSTRAINT events_gift_mode_check CHECK (gift_mode IN ('CONTRIBUTION_AND_GIFTS','CONTRIBUTION_ONLY'));

-- Occasion-specific invitation details (e.g. bride and groom names for a wedding), keyed by field name.
-- Validated per occasion in the application; existing events start with no details.
ALTER TABLE events ADD COLUMN occasion_details JSONB NOT NULL DEFAULT '{}'::jsonb;
ALTER TABLE events ADD CONSTRAINT events_occasion_details_object CHECK (jsonb_typeof(occasion_details) = 'object');
