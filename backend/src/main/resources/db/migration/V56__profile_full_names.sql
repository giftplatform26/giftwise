ALTER TABLE users ADD COLUMN full_name VARCHAR(120);
ALTER TABLE gift_group_accounts ADD COLUMN full_name VARCHAR(120);
-- Existing group members already have display_name; no new column required.
