
-- A consumed challenge is no longer active.
-- Keep the uniqueness guarantee for unconsumed
-- PENDING and SENT challenges.

DROP INDEX IF EXISTS ux_otp_active_user;

CREATE UNIQUE INDEX ux_otp_active_user
    ON phone_otp_challenges (user_id)
    WHERE consumed_at IS NULL
      AND delivery_status IN ('PENDING', 'SENT');
