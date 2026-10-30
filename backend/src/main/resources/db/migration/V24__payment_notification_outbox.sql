-- Notification records are created only for captured and allocated payments.
-- Delivery channels are separate from recording; no SMS/email is sent by this migration.
CREATE TABLE payment_notifications (
    id UUID PRIMARY KEY,
    contribution_id UUID NOT NULL REFERENCES contributions(id),
    recipient_type VARCHAR(10) NOT NULL CHECK (recipient_type IN ('GUEST','HOST')),
    recipient_address VARCHAR(320),
    event_id UUID NOT NULL REFERENCES events(id),
    gift_amount_paise BIGINT NOT NULL CHECK (gift_amount_paise >= 0),
    wallet_amount_paise BIGINT NOT NULL CHECK (wallet_amount_paise >= 0),
    status VARCHAR(15) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','SENT','FAILED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMPTZ,
    UNIQUE(contribution_id, recipient_type)
);
CREATE INDEX idx_payment_notifications_pending ON payment_notifications(created_at) WHERE status = 'PENDING';
