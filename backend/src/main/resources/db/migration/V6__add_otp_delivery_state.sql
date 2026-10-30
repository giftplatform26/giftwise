
-- OTP dispatch lifecycle.
-- Never store the plaintext OTP here.

ALTER TABLE phone_otp_challenges
    ADD COLUMN delivery_status VARCHAR(20)
        NOT NULL DEFAULT 'PENDING',
    ADD COLUMN delivery_updated_at TIMESTAMPTZ
        NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE phone_otp_challenges
    ADD CONSTRAINT chk_otp_delivery_status
        CHECK (
            delivery_status IN (
                                'PENDING',
                                'SENT',
                                'FAILED',
                                'INVALIDATED'
                )
            );

CREATE INDEX ix_otp_user_delivery
    ON phone_otp_challenges (
                             user_id,
                             delivery_status,
                             created_at DESC
        );

-- Only one currently active or pending challenge
-- per user. Expired challenges must be explicitly
-- invalidated before a replacement is created.

CREATE UNIQUE INDEX ux_otp_active_user
    ON phone_otp_challenges (user_id)
    WHERE delivery_status IN ('PENDING', 'SENT');
