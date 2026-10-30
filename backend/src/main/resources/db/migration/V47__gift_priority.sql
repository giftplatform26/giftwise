-- Batch 6A: priority is display metadata, not a payment or settlement status.
ALTER TABLE gifts ADD COLUMN priority VARCHAR(6) NOT NULL DEFAULT 'MEDIUM';
ALTER TABLE gifts ADD CONSTRAINT ck_gifts_priority CHECK (priority IN ('HIGH','MEDIUM','LOW'));
CREATE INDEX idx_gifts_event_priority ON gifts(event_id, priority, created_at, id);
