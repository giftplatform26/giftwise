ALTER TABLE events ADD COLUMN delivery_same_as_event BOOLEAN NOT NULL DEFAULT TRUE,
 ADD COLUMN delivery_address_line1 VARCHAR(200), ADD COLUMN delivery_city VARCHAR(120),
 ADD COLUMN delivery_state VARCHAR(120), ADD COLUMN delivery_postal_code VARCHAR(20), ADD COLUMN delivery_country VARCHAR(80);
ALTER TABLE events ADD CONSTRAINT valid_delivery_address CHECK(delivery_same_as_event OR
 (NULLIF(trim(delivery_address_line1),'') IS NOT NULL AND NULLIF(trim(delivery_city),'') IS NOT NULL
 AND NULLIF(trim(delivery_state),'') IS NOT NULL AND NULLIF(trim(delivery_postal_code),'') IS NOT NULL AND NULLIF(trim(delivery_country),'') IS NOT NULL));
CREATE TABLE guest_gifts (
 id UUID PRIMARY KEY, event_id UUID NOT NULL REFERENCES events(id),
 added_by UUID NOT NULL REFERENCES gift_group_accounts(id),
 name VARCHAR(160) NOT NULL, description VARCHAR(500), product_url VARCHAR(1000),
 selected_by UUID REFERENCES gift_group_accounts(id), contribution_type VARCHAR(20),
 group_id UUID REFERENCES gift_groups(id), published_at TIMESTAMPTZ,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 CHECK ((selected_by IS NULL AND contribution_type IS NULL AND group_id IS NULL AND published_at IS NULL)
 OR (selected_by IS NOT NULL AND contribution_type IS NOT NULL AND ((contribution_type='INDIVIDUAL' AND group_id IS NULL) OR (contribution_type='GROUP' AND group_id IS NOT NULL))))
);
CREATE INDEX guest_gifts_event ON guest_gifts(event_id,created_at,id);
