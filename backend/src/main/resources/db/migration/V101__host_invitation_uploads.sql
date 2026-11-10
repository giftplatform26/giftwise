CREATE TABLE host_invitation_upload_controls (
 host_id UUID PRIMARY KEY REFERENCES users(id),
 enabled BOOLEAN NOT NULL DEFAULT FALSE,
 requires_payment BOOLEAN NOT NULL DEFAULT TRUE,
 changed_by UUID NOT NULL REFERENCES users(id),
 changed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE host_invitation_uploads (
 id UUID PRIMARY KEY,
 host_id UUID NOT NULL REFERENCES users(id),
 category VARCHAR(20) NOT NULL CHECK(category IN ('BACKGROUND','DECORATION')),
 title VARCHAR(120) NOT NULL,
 image_png BYTEA NOT NULL,
 width INTEGER NOT NULL CHECK(width BETWEEN 32 AND 4096),
 height INTEGER NOT NULL CHECK(height BETWEEN 32 AND 4096),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX host_invitation_uploads_owner ON host_invitation_uploads(host_id,created_at DESC);
