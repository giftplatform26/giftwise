-- Existing accounts retain password login until PIN enrollment.
ALTER TABLE gift_group_accounts ADD COLUMN pin_hash VARCHAR(100);
ALTER TABLE gift_group_members ADD COLUMN pin_hash VARCHAR(100);
