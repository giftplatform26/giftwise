-- Existing rows start at version zero; Hibernate increments on updates.
ALTER TABLE events ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
