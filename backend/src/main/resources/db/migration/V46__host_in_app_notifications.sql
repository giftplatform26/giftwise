-- Host in-app inbox; delivery state is separate from read state.
ALTER TABLE notification_outbox DROP CONSTRAINT IF EXISTS notification_outbox_source_kind_check;
ALTER TABLE notification_outbox ADD CONSTRAINT notification_outbox_source_kind_check
  CHECK (source_kind IN ('MANUAL_CLAIM','VERIFIED_CAPTURE','RAZORPAY_ORDER'));
ALTER TABLE notification_outbox DROP CONSTRAINT IF EXISTS notification_outbox_payment_evidence_check;
ALTER TABLE notification_outbox ADD CONSTRAINT notification_outbox_payment_evidence_check
  CHECK (payment_evidence IN ('GUEST_REPORTED','CAPTURED','CHECKOUT_PENDING'));
ALTER TABLE notification_outbox ADD COLUMN read_at TIMESTAMPTZ;
CREATE INDEX idx_notification_host_unread ON notification_outbox(event_id,created_at DESC)
 WHERE recipient_kind='HOST' AND channel='IN_APP' AND read_at IS NULL;
