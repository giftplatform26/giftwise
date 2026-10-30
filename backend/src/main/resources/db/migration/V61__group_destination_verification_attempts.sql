-- Destination verification is distinct from receipt of a guest payment.
-- Do not store plaintext account numbers, VPA, names, or provider response bodies.
CREATE TABLE gift_group_destination_verification_attempts (
 id UUID PRIMARY KEY,
 group_id UUID NOT NULL REFERENCES gift_groups(id) ON DELETE CASCADE,
 destination_id UUID NOT NULL REFERENCES gift_group_payment_destinations(id) ON DELETE CASCADE,
 requested_by UUID NOT NULL REFERENCES users(id),
 provider VARCHAR(24) NOT NULL CHECK (provider IN ('CASHFREE','RAZORPAYX')),
 environment VARCHAR(12) NOT NULL CHECK (environment IN ('SANDBOX','PRODUCTION')),
 request_reference VARCHAR(120) NOT NULL UNIQUE,
 provider_reference VARCHAR(120),
 state VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (state IN ('PENDING','REJECTED','UNAVAILABLE','VERIFIED')),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 completed_at TIMESTAMPTZ,
 CONSTRAINT group_verification_completion CHECK (
   (state='PENDING' AND completed_at IS NULL) OR
   (state<>'PENDING' AND completed_at IS NOT NULL)),
 CONSTRAINT group_verification_verified_evidence CHECK (
   state<>'VERIFIED' OR (environment='PRODUCTION' AND provider_reference IS NOT NULL AND length(trim(provider_reference))>0))
);
CREATE INDEX group_destination_verification_history_idx
 ON gift_group_destination_verification_attempts(group_id,destination_id,created_at DESC);
CREATE UNIQUE INDEX group_destination_one_pending_verification_idx
 ON gift_group_destination_verification_attempts(destination_id) WHERE state='PENDING';
CREATE UNIQUE INDEX group_destination_verification_provider_reference_idx
 ON gift_group_destination_verification_attempts(provider,environment,provider_reference)
 WHERE provider_reference IS NOT NULL;
-- Enforce the destination/group binding, even against direct SQL writes.
CREATE OR REPLACE FUNCTION gift_check_group_destination_verification_binding()
RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 IF NOT EXISTS (SELECT 1 FROM gift_group_payment_destinations d
                WHERE d.id=NEW.destination_id AND d.group_id=NEW.group_id) THEN
  RAISE EXCEPTION 'Verification destination does not belong to group';
 END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER group_destination_verification_binding
 BEFORE INSERT OR UPDATE OF group_id,destination_id
 ON gift_group_destination_verification_attempts
 FOR EACH ROW EXECUTE FUNCTION gift_check_group_destination_verification_binding();
