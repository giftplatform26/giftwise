ALTER TABLE invitation_gallery_assets
ADD COLUMN tier VARCHAR(20) NOT NULL DEFAULT 'FREE';

ALTER TABLE invitation_gallery_assets
ADD CONSTRAINT chk_invitation_gallery_asset_tier
CHECK (tier IN ('FREE', 'PREMIUM'));

CREATE INDEX idx_invitation_gallery_tier
ON invitation_gallery_assets (tier, published);
