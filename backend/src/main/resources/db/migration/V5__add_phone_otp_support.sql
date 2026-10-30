
-- Optional phone number for existing customers.
-- Phone numbers use E.164 international format.

ALTER TABLE users
    ADD COLUMN phone_number VARCHAR(16),
    ADD COLUMN phone_verified_at TIMESTAMPTZ;

ALTER TABLE users
    ADD CONSTRAINT chk_users_phone_format
        CHECK (
            phone_number IS NULL
                OR phone_number ~ '^\+[1-9][0-9]{1,14}$'
    );

ALTER TABLE users
    ADD CONSTRAINT chk_users_phone_verification
        CHECK (
            phone_verified_at IS NULL
                OR phone_number IS NOT NULL
            );

CREATE UNIQUE INDEX ux_users_phone_number
    ON users (phone_number)
    WHERE phone_number IS NOT NULL;

-- Each challenge is associated with an existing user.
-- OTPs are stored as keyed digests, never plaintext.

CREATE TABLE phone_otp_challenges (
                                      id UUID PRIMARY KEY,

                                      user_id UUID NOT NULL
                                          REFERENCES users(id) ON DELETE CASCADE,

                                      phone_number VARCHAR(16) NOT NULL,

                                      otp_digest VARCHAR(128) NOT NULL,

                                      created_at TIMESTAMPTZ NOT NULL
                                                                     DEFAULT CURRENT_TIMESTAMP,

                                      expires_at TIMESTAMPTZ NOT NULL,

                                      consumed_at TIMESTAMPTZ,

                                      attempt_count INTEGER NOT NULL DEFAULT 0,

                                      CONSTRAINT chk_otp_phone_format
                                          CHECK (
                                              phone_number ~ '^\+[1-9][0-9]{1,14}$'
),

    CONSTRAINT chk_otp_attempt_count
        CHECK (attempt_count BETWEEN 0 AND 5),

    CONSTRAINT chk_otp_expiration
        CHECK (expires_at > created_at),

    CONSTRAINT chk_otp_consumption
        CHECK (
            consumed_at IS NULL
            OR consumed_at >= created_at
        )
);

CREATE INDEX ix_phone_otp_user_created
    ON phone_otp_challenges (
                             user_id,
                             created_at DESC
        );

CREATE INDEX ix_phone_otp_expiration
    ON phone_otp_challenges (expires_at);
