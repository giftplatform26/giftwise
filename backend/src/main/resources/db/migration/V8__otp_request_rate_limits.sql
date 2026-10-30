-- PostgreSQL-backed fixed-window OTP request quotas.
CREATE TABLE otp_request_rate_limits (
    scope VARCHAR(16) NOT NULL,
    subject_key VARCHAR(128) NOT NULL,
    window_start TIMESTAMPTZ NOT NULL,
    request_count INTEGER NOT NULL CHECK (request_count >= 0),
    PRIMARY KEY (scope, subject_key)
);
CREATE INDEX ix_otp_request_rate_limits_window
    ON otp_request_rate_limits (window_start);
