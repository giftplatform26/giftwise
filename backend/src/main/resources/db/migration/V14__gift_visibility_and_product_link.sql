-- Existing gifts were created before publication controls existed.
-- Keep them private until their owners explicitly publish them.
ALTER TABLE gifts ADD COLUMN visibility VARCHAR(12) NOT NULL DEFAULT 'DRAFT';
ALTER TABLE gifts ADD COLUMN product_url VARCHAR(2048);
ALTER TABLE gifts ADD CONSTRAINT ck_gifts_visibility CHECK (visibility IN ('DRAFT', 'PUBLIC'));
CREATE INDEX idx_gifts_event_visibility ON gifts(event_id, visibility);
