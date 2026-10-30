CREATE TABLE gift_photos (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL REFERENCES events(id),
    actor_id UUID NOT NULL REFERENCES users(id),
    content BYTEA NOT NULL,
    width INTEGER NOT NULL CHECK (width BETWEEN 160 AND 1200),
    height INTEGER NOT NULL CHECK (height BETWEEN 160 AND 1200),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_gift_photos_event ON gift_photos(event_id);
