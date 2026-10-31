-- No row means use application defaults (both disabled). Admin overrides persist across restarts.
CREATE TABLE platform_verification_settings (
 id INTEGER PRIMARY KEY CHECK(id=1),
 mobile_otp_enabled BOOLEAN NOT NULL,
 email_verification_enabled BOOLEAN NOT NULL,
 changed_by UUID NOT NULL REFERENCES users(id),
 changed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
ALTER TABLE account_pending_mobile_verifications ADD COLUMN delivered_at TIMESTAMPTZ;
