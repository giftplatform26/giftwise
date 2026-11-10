CREATE TABLE platform_brand_assets (
 id UUID PRIMARY KEY, image_bytes BYTEA NOT NULL, created_by UUID NOT NULL REFERENCES users(id),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(), CHECK(octet_length(image_bytes)<=5242880)
);
CREATE TABLE platform_branding (
 id INTEGER PRIMARY KEY CHECK(id=1), application_name VARCHAR(80) NOT NULL DEFAULT 'GiftWise',
 brand_color VARCHAR(7) NOT NULL DEFAULT '#5b21b6', footer_text VARCHAR(240) NOT NULL DEFAULT '© 2026 GiftWise. All rights reserved.',
 support_email VARCHAR(254) NOT NULL DEFAULT '', social_links TEXT NOT NULL DEFAULT '',
 logo_id UUID REFERENCES platform_brand_assets(id), favicon_id UUID REFERENCES platform_brand_assets(id),
 revision BIGINT NOT NULL DEFAULT 0, updated_by UUID REFERENCES users(id), updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
INSERT INTO platform_branding(id) VALUES(1);
CREATE TABLE user_profile_preferences (
 user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
 timezone VARCHAR(80) NOT NULL DEFAULT 'Asia/Kolkata', event_notifications BOOLEAN NOT NULL DEFAULT true,
 contribution_notifications BOOLEAN NOT NULL DEFAULT true, marketing_notifications BOOLEAN NOT NULL DEFAULT false,
 picture_png BYTEA, updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), CHECK(octet_length(picture_png)<=5242880)
);
CREATE TABLE account_legal_consents (
 id UUID PRIMARY KEY, account_kind VARCHAR(30) NOT NULL CHECK(account_kind IN ('GROUP_ADMIN','GROUP_MEMBER')),
 account_id UUID NOT NULL, terms_version VARCHAR(40) NOT NULL, privacy_version VARCHAR(40) NOT NULL,
 accepted_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_account_consents ON account_legal_consents(account_kind, account_id, accepted_at);
