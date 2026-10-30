-- Preserve reported/unverified semantics for both guest and member submissions.
ALTER TABLE gift_groups ADD COLUMN contribution_access_mode VARCHAR(16) NOT NULL DEFAULT 'BOTH' CHECK (contribution_access_mode IN ('BOTH','GUESTS_ONLY','MEMBERS_ONLY'));
ALTER TABLE gift_group_guest_reports ADD COLUMN member_id UUID REFERENCES gift_group_members(id);
CREATE INDEX gift_group_reports_member_history_idx ON gift_group_guest_reports(member_id,created_at DESC) WHERE member_id IS NOT NULL;
