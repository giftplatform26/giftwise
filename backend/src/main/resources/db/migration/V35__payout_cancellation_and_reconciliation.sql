-- No provider dispatch, no payout execution, and no automatic verification.
-- Cancellation is final; a released reservation cannot be re-held for the same request.
ALTER TABLE host_payout_requests ADD COLUMN cancelled_at TIMESTAMPTZ;
ALTER TABLE host_payout_requests ADD COLUMN cancellation_reason VARCHAR(160);
ALTER TABLE host_payout_requests ADD CONSTRAINT ck_payout_cancel_pair CHECK
 ((cancelled_at IS NULL AND cancellation_reason IS NULL) OR
  (cancelled_at IS NOT NULL AND cancellation_reason IS NOT NULL));
CREATE TABLE payout_reconciliation_observations (
 id UUID PRIMARY KEY,
 payout_request_id UUID NOT NULL REFERENCES host_payout_requests(id),
 source VARCHAR(32) NOT NULL CHECK (source IN ('INTERNAL_REVIEW','PROVIDER_PREVIEW')),
 observed_state VARCHAR(24) NOT NULL CHECK (observed_state IN ('UNKNOWN','PENDING','FAILED','SUCCEEDED')),
 correlation_reference VARCHAR(160),
 observed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 -- Observations are informational and NEVER change request/reservation state.
 CONSTRAINT ck_provider_observation_reference CHECK
  (source <> 'PROVIDER_PREVIEW' OR correlation_reference IS NOT NULL)
);
CREATE INDEX idx_payout_observations_request ON payout_reconciliation_observations(payout_request_id,observed_at DESC);
