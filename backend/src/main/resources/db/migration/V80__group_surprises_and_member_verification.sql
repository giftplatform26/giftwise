CREATE TABLE group_member_email_policies (
 host_id UUID PRIMARY KEY REFERENCES users(id),
 admin_enabled BOOLEAN NOT NULL DEFAULT FALSE,
 host_enabled BOOLEAN NOT NULL DEFAULT FALSE
);
ALTER TABLE gift_groups ADD COLUMN reserved_gift_id UUID;
ALTER TABLE gift_groups ADD COLUMN collection_published_at TIMESTAMPTZ;
ALTER TABLE gift_groups ADD CONSTRAINT fk_reserved_group_gift FOREIGN KEY(reserved_gift_id,event_id) REFERENCES gifts(id,event_id);
CREATE UNIQUE INDEX uq_reserved_group_gift ON gift_groups(reserved_gift_id) WHERE reserved_gift_id IS NOT NULL;
CREATE TABLE group_collection_notifications (
 id UUID PRIMARY KEY, group_id UUID NOT NULL UNIQUE REFERENCES gift_groups(id),
 event_id UUID NOT NULL REFERENCES events(id),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(), read_at TIMESTAMPTZ
);
