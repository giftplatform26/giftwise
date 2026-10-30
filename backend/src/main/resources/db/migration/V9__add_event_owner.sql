-- Nullable for pre-existing V1 events and repository regression fixtures.
-- All new API-created events must have a non-null owner via service validation.
ALTER TABLE events ADD COLUMN owner_id UUID;
CREATE INDEX idx_events_owner_id ON events(owner_id);
ALTER TABLE events ADD CONSTRAINT fk_events_owner FOREIGN KEY (owner_id) REFERENCES users(id);
