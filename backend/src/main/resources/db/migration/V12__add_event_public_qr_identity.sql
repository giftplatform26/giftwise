ALTER TABLE events ADD COLUMN public_token UUID;
UPDATE events SET public_token = gen_random_uuid() WHERE public_token IS NULL;
ALTER TABLE events ALTER COLUMN public_token SET DEFAULT gen_random_uuid();
ALTER TABLE events ALTER COLUMN public_token SET NOT NULL;
ALTER TABLE events ADD CONSTRAINT uq_events_public_token UNIQUE (public_token);
ALTER TABLE events ADD COLUMN qr_theme VARCHAR(40) NOT NULL DEFAULT 'MODERN_MINIMAL';
ALTER TABLE events ADD CONSTRAINT ck_events_qr_theme CHECK (qr_theme IN ('MODERN_MINIMAL','ELEGANT_GOLD','BIRTHDAY_CELEBRATION','BABY_SHOWER','ANNIVERSARY','HOUSEWARMING'));
