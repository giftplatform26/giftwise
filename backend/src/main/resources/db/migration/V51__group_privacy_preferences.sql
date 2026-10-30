ALTER TABLE gift_groups ADD COLUMN join_policy VARCHAR(20) NOT NULL DEFAULT 'APPROVAL';
ALTER TABLE gift_groups ADD COLUMN member_visibility VARCHAR(20) NOT NULL DEFAULT 'ADMIN_ONLY';
ALTER TABLE gift_groups ADD COLUMN amount_visibility VARCHAR(20) NOT NULL DEFAULT 'ADMIN_ONLY';
ALTER TABLE gift_groups ADD CONSTRAINT gift_group_join_policy_ck CHECK (join_policy IN ('APPROVAL','INVITE_ONLY'));
ALTER TABLE gift_groups ADD CONSTRAINT gift_group_member_visibility_ck CHECK (member_visibility IN ('ADMIN_ONLY','MEMBERS'));
ALTER TABLE gift_groups ADD CONSTRAINT gift_group_amount_visibility_ck CHECK (amount_visibility IN ('ADMIN_ONLY','OWN_ONLY'));
