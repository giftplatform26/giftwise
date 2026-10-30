-- Count requests without storing invitation prompts, generated text, or provider credentials.
CREATE TABLE invitation_ai_requests (
 id UUID PRIMARY KEY,
 host_id UUID NOT NULL REFERENCES users(id),
 event_id UUID NOT NULL REFERENCES events(id),
 status VARCHAR(16) NOT NULL CHECK(status IN ('PENDING','COMPLETED','FAILED')),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_invitation_ai_host_time ON invitation_ai_requests(host_id,created_at DESC);
