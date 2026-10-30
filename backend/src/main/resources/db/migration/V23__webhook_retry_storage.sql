-- Batch 4A.14D.3C: Durable webhook retry metadata.
-- Original payment and ledger migrations remain unchanged.

ALTER TABLE payment_webhook_events
    ADD COLUMN raw_payload TEXT,
    ADD COLUMN attempt_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN last_error TEXT,
    ADD COLUMN last_attempt_at TIMESTAMPTZ;

ALTER TABLE payment_webhook_events
    ADD CONSTRAINT ck_webhook_attempt_count
    CHECK (attempt_count >= 0);

CREATE INDEX idx_payment_webhook_retry
    ON payment_webhook_events(status, received_at)
    WHERE status IN ('RECEIVED', 'FAILED');
