ALTER TABLE events ADD COLUMN fulfillment_default VARCHAR(40) NOT NULL DEFAULT 'AFTER_EVENT_CONFIRMATION';
ALTER TABLE events ADD CONSTRAINT ck_event_fulfillment_default CHECK (fulfillment_default IN ('DELIVER_WHEN_FUNDED','AFTER_EVENT_CONFIRMATION'));
CREATE TABLE gifts (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL REFERENCES events(id),
    version BIGINT NOT NULL DEFAULT 0,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    image_key VARCHAR(512),
    target_amount NUMERIC(15,2) NOT NULL CHECK (target_amount > 0),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','CANCELLED','CLOSED_UNFUNDED')),
    fulfillment_override VARCHAR(40) CHECK (fulfillment_override IN ('DELIVER_WHEN_FUNDED','AFTER_EVENT_CONFIRMATION')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_gift_name_nonempty CHECK (length(btrim(name)) > 0)
);
CREATE INDEX idx_gifts_event_created ON gifts(event_id, created_at);
