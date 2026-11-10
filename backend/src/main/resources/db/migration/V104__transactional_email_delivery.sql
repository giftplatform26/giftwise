CREATE TABLE transactional_email_outbox (
 id UUID PRIMARY KEY, source_key VARCHAR(180) NOT NULL UNIQUE,
 template_key VARCHAR(40) NOT NULL CHECK(template_key IN ('registration','event-invitation','contribution-received','payment-success','event-completed')),
 recipient VARCHAR(254) NOT NULL, variables JSONB NOT NULL, rendered_email JSONB, language_code VARCHAR(63) NOT NULL DEFAULT 'en',
 status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK(status IN ('PENDING','SENDING','SENT','FAILED','CANCELLED')),
 attempts INTEGER NOT NULL DEFAULT 0, next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(), sent_at TIMESTAMPTZ, last_error_code VARCHAR(50)
);
CREATE INDEX idx_transactional_email_pending ON transactional_email_outbox(status,next_attempt_at);
