-- Batch 5O.1: message preferences, event thank-you templates, and durable delivery outbox.
-- No payment or ledger mutation. External delivery remains disabled until configured.
CREATE TABLE IF NOT EXISTS event_thank_you_settings (
    event_id UUID PRIMARY KEY REFERENCES events(id) ON DELETE CASCADE,
    host_display_name VARCHAR(200) NOT NULL,
    email_subject VARCHAR(200) NOT NULL DEFAULT 'Thank you for celebrating with us',
    email_body TEXT NOT NULL DEFAULT 'Dear {{guestName}}, thank you for your thoughtful contribution to {{eventName}}. With love, {{hostName}}',
    sms_body TEXT NOT NULL DEFAULT 'Thank you {{guestName}} for celebrating {{eventName}} with us! - {{hostName}}',
    card_template VARCHAR(60) NOT NULL DEFAULT 'CLASSIC',
    auto_send BOOLEAN NOT NULL DEFAULT TRUE,
    email_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    sms_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    whatsapp_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT event_thanks_host_nonempty CHECK (length(btrim(host_display_name)) BETWEEN 1 AND 200),
    CONSTRAINT event_thanks_email_subject_nonempty CHECK (length(btrim(email_subject)) BETWEEN 1 AND 200),
    CONSTRAINT event_thanks_email_body_length CHECK (length(email_body) BETWEEN 1 AND 10000),
    CONSTRAINT event_thanks_sms_body_length CHECK (length(sms_body) BETWEEN 1 AND 1000)
);
-- Keep per-event host display name separate from account identity.
-- Existing events receive a safe fallback; do not infer legal or payment account names.
INSERT INTO event_thank_you_settings(event_id,host_display_name)
SELECT id, 'Event Host' FROM events
ON CONFLICT (event_id) DO NOTHING;

CREATE TABLE IF NOT EXISTS notification_outbox (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL REFERENCES events(id),
    notification_kind VARCHAR(40) NOT NULL,
    recipient_kind VARCHAR(12) NOT NULL CHECK (recipient_kind IN ('GUEST','HOST')),
    channel VARCHAR(12) NOT NULL CHECK (channel IN ('IN_APP','EMAIL','SMS','WHATSAPP')),
    -- recipient address is deliberately NOT stored in plaintext in this foundational schema.
    -- Later delivery adapter must use protected destination storage and consent checks.
    recipient_reference VARCHAR(160),
    source_kind VARCHAR(32) NOT NULL CHECK (source_kind IN ('MANUAL_CLAIM','VERIFIED_CAPTURE')),
    source_id UUID NOT NULL,
    payment_evidence VARCHAR(20) NOT NULL CHECK (payment_evidence IN ('GUEST_REPORTED','CAPTURED')),
    subject VARCHAR(200),
    message TEXT NOT NULL,
    card_template VARCHAR(60),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
       CHECK (status IN ('PENDING','SENDING','SENT','FAILED','CANCELLED')),
    attempts INTEGER NOT NULL DEFAULT 0 CHECK (attempts >= 0),
    next_attempt_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMPTZ,
    last_error_code VARCHAR(100),
    UNIQUE (event_id,notification_kind,recipient_kind,channel,source_kind,source_id),
    CHECK (length(message) BETWEEN 1 AND 10000)
);
CREATE INDEX IF NOT EXISTS idx_notification_outbox_event_created
 ON notification_outbox(event_id,created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notification_outbox_delivery
 ON notification_outbox(status,next_attempt_at) WHERE status IN ('PENDING','FAILED');
