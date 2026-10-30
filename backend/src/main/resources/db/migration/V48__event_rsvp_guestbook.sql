CREATE TABLE event_rsvps (
 id UUID PRIMARY KEY, event_id UUID NOT NULL REFERENCES events(id), guest_name VARCHAR(120) NOT NULL,
 guest_email VARCHAR(254) NOT NULL, attendance VARCHAR(12) NOT NULL CHECK(attendance IN ('YES','NO','MAYBE')),
 party_size INTEGER NOT NULL CHECK(party_size BETWEEN 0 AND 10),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 CONSTRAINT ux_event_rsvp_email UNIQUE(event_id,guest_email),
 CONSTRAINT chk_event_rsvp_party CHECK ((attendance='YES' AND party_size BETWEEN 1 AND 10) OR (attendance<>'YES' AND party_size=0))
);
CREATE INDEX ix_event_rsvp_event ON event_rsvps(event_id);
CREATE TABLE event_guestbook (
 id UUID PRIMARY KEY, event_id UUID NOT NULL REFERENCES events(id), guest_name VARCHAR(120) NOT NULL,
 message VARCHAR(1000) NOT NULL, status VARCHAR(12) NOT NULL DEFAULT 'PENDING'
 CHECK(status IN ('PENDING','APPROVED','HIDDEN')),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(), moderated_at TIMESTAMPTZ
);
CREATE INDEX ix_event_guestbook_event_status ON event_guestbook(event_id,status,created_at DESC);
CREATE TABLE event_guest_submission_limits (
 event_id UUID NOT NULL REFERENCES events(id), fingerprint VARCHAR(64) NOT NULL,
 submission_date DATE NOT NULL, submission_type VARCHAR(12) NOT NULL,
 count INTEGER NOT NULL CHECK(count BETWEEN 1 AND 100),
 PRIMARY KEY(event_id,fingerprint,submission_date,submission_type)
);
