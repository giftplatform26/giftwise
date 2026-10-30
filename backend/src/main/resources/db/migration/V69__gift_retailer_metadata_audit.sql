CREATE TABLE gift_retailer_metadata_audit (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL REFERENCES events(id),
    actor_id UUID NOT NULL REFERENCES users(id),
    gift_id UUID REFERENCES gifts(id),
    source_url VARCHAR(2048) NOT NULL,
    metadata JSONB NOT NULL,
    fetched_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    saved_at TIMESTAMPTZ
);
CREATE INDEX idx_gift_retailer_metadata_gift ON gift_retailer_metadata_audit(gift_id, saved_at);
