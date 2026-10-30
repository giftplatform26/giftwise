CREATE TABLE qr_invitation_designs (
 id UUID PRIMARY KEY,
 event_id UUID NOT NULL REFERENCES events(id),
 name VARCHAR(100) NOT NULL,
 style VARCHAR(30) NOT NULL,
 greeting VARCHAR(180),
 image_png BYTEA,
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT ck_invitation_name CHECK (length(btrim(name)) > 0),
 CONSTRAINT ck_invitation_style CHECK (style IN ('PHOTO_FRAME','FLORAL','CELEBRATION','MINIMAL'))
);
CREATE INDEX idx_qr_invitation_event ON qr_invitation_designs(event_id,created_at);
