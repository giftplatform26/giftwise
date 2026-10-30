CREATE TABLE invitation_gallery_assets (
 id UUID PRIMARY KEY,
 category VARCHAR(20) NOT NULL CHECK (category IN ('BACKGROUND','DECORATION')),
 occasion VARCHAR(50) NOT NULL,
 title VARCHAR(120) NOT NULL,
 content_type VARCHAR(20) NOT NULL CHECK (content_type = 'image/png'),
 image_png BYTEA NOT NULL,
 width INTEGER NOT NULL CHECK (width BETWEEN 32 AND 4096),
 height INTEGER NOT NULL CHECK (height BETWEEN 32 AND 4096),
 published BOOLEAN NOT NULL DEFAULT FALSE,
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_invitation_gallery_published ON invitation_gallery_assets (category, occasion, published);
