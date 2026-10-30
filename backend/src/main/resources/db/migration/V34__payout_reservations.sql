-- No money movement is performed by this migration.
-- Reservations are an internal hold against independently-settled HOST_AVAILABLE funds.
CREATE TABLE payout_reservations (
 id UUID PRIMARY KEY,
 payout_request_id UUID NOT NULL UNIQUE REFERENCES host_payout_requests(id),
 event_id UUID NOT NULL REFERENCES events(id),
 amount_paise BIGINT NOT NULL CHECK (amount_paise > 0),
 state VARCHAR(16) NOT NULL DEFAULT 'HELD' CHECK (state IN ('HELD','RELEASED')),
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 released_at TIMESTAMPTZ,
 CONSTRAINT ck_payout_reservation_release CHECK
  ((state = 'HELD' AND released_at IS NULL) OR
   (state = 'RELEASED' AND released_at IS NOT NULL))
);
CREATE INDEX idx_payout_reservations_event_state ON payout_reservations(event_id,state);
