ALTER TABLE gift_groups ADD COLUMN independent_gift BOOLEAN NOT NULL DEFAULT FALSE;
UPDATE gift_groups SET independent_gift=TRUE WHERE selected_gift_id IS NULL;
ALTER TABLE gift_groups ADD CONSTRAINT independent_group_no_wishlist CHECK(NOT independent_gift OR (selected_gift_id IS NULL AND reserved_gift_id IS NULL));
