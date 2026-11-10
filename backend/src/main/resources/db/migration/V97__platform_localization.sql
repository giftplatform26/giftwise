-- Feature off preserves current English flows. Language identifiers are data, not enums.
CREATE TABLE platform_languages (
 code VARCHAR(63) PRIMARY KEY, native_name VARCHAR(120) NOT NULL,
 english_name VARCHAR(120) NOT NULL, direction VARCHAR(3) NOT NULL CHECK(direction IN ('LTR','RTL')),
 enabled BOOLEAN NOT NULL DEFAULT TRUE, version BIGINT NOT NULL DEFAULT 0,
 changed_by UUID REFERENCES users(id), changed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
INSERT INTO platform_languages(code,native_name,english_name,direction) VALUES
 ('en','English','English','LTR'),('ml','മലയാളം','Malayalam','LTR'),('kn','ಕನ್ನಡ','Kannada','LTR'),('te','తెలుగు','Telugu','LTR'),('ta','தமிழ்','Tamil','LTR'),('hi','हिन्दी','Hindi','LTR'),('es','Español','Spanish','LTR'),('ar','العربية','Arabic','RTL');
CREATE TABLE platform_localization_settings (
 id INTEGER PRIMARY KEY CHECK(id=1), enabled BOOLEAN NOT NULL DEFAULT FALSE,
 default_language VARCHAR(63) NOT NULL REFERENCES platform_languages(code),
 fallback_language VARCHAR(63) NOT NULL REFERENCES platform_languages(code),
 version BIGINT NOT NULL DEFAULT 0, changed_by UUID REFERENCES users(id),
 changed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
INSERT INTO platform_localization_settings(id,default_language,fallback_language) VALUES(1,'en','en');
CREATE TABLE user_language_preferences (
 user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
 language_code VARCHAR(63) NOT NULL REFERENCES platform_languages(code),
 changed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE localization_host_controls (
 host_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
 enabled BOOLEAN NOT NULL, changed_by UUID REFERENCES users(id), changed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE localization_event_controls (
 event_id UUID PRIMARY KEY REFERENCES events(id) ON DELETE CASCADE,
 enabled BOOLEAN NOT NULL, changed_by UUID REFERENCES users(id), changed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE localization_sources (
 message_key VARCHAR(120) PRIMARY KEY, source_text TEXT NOT NULL,
 category VARCHAR(16) NOT NULL DEFAULT 'UI' CHECK(category IN ('UI','EMAIL','SMS','NOTIFICATION','VALIDATION')),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE localization_translations (
 language_code VARCHAR(63) NOT NULL REFERENCES platform_languages(code),
 message_key VARCHAR(120) NOT NULL REFERENCES localization_sources(message_key),
 translated_text TEXT NOT NULL CHECK(length(translated_text)<=10000),
 published BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0,
 changed_by UUID REFERENCES users(id), changed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 PRIMARY KEY(language_code,message_key)
);
CREATE INDEX localization_translation_review ON localization_translations(language_code,published,message_key);
-- Snapshot the language at enqueue time; never infer verified payment from translated wording.
ALTER TABLE notification_outbox ADD COLUMN language_code VARCHAR(63) NOT NULL DEFAULT 'en' REFERENCES platform_languages(code);
CREATE TABLE language_delivery_templates (
 language_code VARCHAR(63) NOT NULL REFERENCES platform_languages(code),
 provider VARCHAR(16) NOT NULL CHECK(provider IN ('MSG91')),
 template_id VARCHAR(120) NOT NULL, version BIGINT NOT NULL DEFAULT 0,
 changed_by UUID REFERENCES users(id), changed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 PRIMARY KEY(language_code,provider)
);
