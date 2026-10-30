-- Existing events remain valid; new event requests must include address/contact details.
ALTER TABLE events ADD COLUMN address_line1 VARCHAR(200);
ALTER TABLE events ADD COLUMN address_city VARCHAR(120);
ALTER TABLE events ADD COLUMN address_state VARCHAR(120);
ALTER TABLE events ADD COLUMN address_postal_code VARCHAR(20);
ALTER TABLE events ADD COLUMN address_country VARCHAR(80);
ALTER TABLE events ADD COLUMN contact_name VARCHAR(150);
ALTER TABLE events ADD COLUMN contact_phone VARCHAR(30);
ALTER TABLE events ADD COLUMN contact_email VARCHAR(254);
