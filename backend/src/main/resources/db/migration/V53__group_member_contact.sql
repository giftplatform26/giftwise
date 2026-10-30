-- Member contact is private; only group admins can retrieve it.
ALTER TABLE gift_group_members ADD COLUMN mobile VARCHAR(20);
CREATE UNIQUE INDEX gift_group_members_unique_name_ci ON gift_group_members(group_id,lower(display_name));
