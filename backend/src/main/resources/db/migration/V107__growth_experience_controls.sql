CREATE TABLE platform_growth_settings (
 id INTEGER PRIMARY KEY CHECK (id=1),
 version BIGINT NOT NULL DEFAULT 0,
 changed_by UUID REFERENCES users(id),
 changed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
INSERT INTO platform_growth_settings(id) VALUES(1);
CREATE TABLE platform_growth_features (
 code VARCHAR(40) PRIMARY KEY,
 enabled BOOLEAN NOT NULL
);
INSERT INTO platform_growth_features(code,enabled) VALUES
 ('GUIDED_CREATION',TRUE),('OCCASION_STARTERS',TRUE),('SMART_DASHBOARD',TRUE),
 ('POLISHED_GUEST_VIEW',TRUE),('PUBLIC_DEMO',TRUE),('SHARE_PREVIEWS',FALSE),
 ('GUEST_DISCOVERY',FALSE),('PUBLIC_KEEPSAKES',FALSE),('INTERACTION_ANALYTICS',TRUE);
ALTER TABLE events ADD COLUMN share_preview_enabled BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE events ADD COLUMN keepsake_enabled BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE gift_photos ADD COLUMN thumbnail BYTEA;
CREATE INDEX application_interactions_funnel ON application_interactions(action,created_at DESC)
 WHERE kind IN ('OPERATION','REQUEST');
