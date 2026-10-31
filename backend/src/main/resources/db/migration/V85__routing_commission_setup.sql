-- Route/VPA remain disabled. Normal checkout controls are deliberately preserved.
ALTER TABLE payment_control_history ALTER COLUMN actor_id DROP NOT NULL;
WITH changes AS (
 SELECT scope,scope_id,enabled,
   (SELECT revision FROM payment_control_revision WHERE id=1)+row_number() OVER(ORDER BY scope,scope_id) AS next_revision
 FROM payment_channel_controls WHERE channel='ROUTING' AND enabled
)
INSERT INTO payment_control_history(revision,actor_id,scope,scope_id,channel,reason,before_enabled,after_enabled)
SELECT next_revision,NULL,scope,scope_id,'ROUTING','System migration: Route/VPA disabled; normal Razorpay checkout retained',true,false FROM changes;
UPDATE payment_control_revision SET revision=GREATEST(revision,COALESCE((SELECT MAX(revision) FROM payment_control_history),0)) WHERE id=1;
UPDATE payment_channel_controls SET enabled=false,changed_by=NULL,changed_at=now() WHERE channel='ROUTING' AND enabled;

CREATE TABLE routing_commission_settings (
 id SMALLINT PRIMARY KEY CHECK(id=1), revision BIGINT NOT NULL CHECK(revision>=0),
 rate_bps INTEGER NOT NULL CHECK(rate_bps BETWEEN 0 AND 10000),
 changed_by UUID REFERENCES users(id), changed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
INSERT INTO routing_commission_settings(id,revision,rate_bps) VALUES(1,0,100);
CREATE TABLE routing_commission_history (
 revision BIGINT PRIMARY KEY, actor_id UUID REFERENCES users(id),
 before_rate_bps INTEGER CHECK(before_rate_bps BETWEEN 0 AND 10000),
 after_rate_bps INTEGER NOT NULL CHECK(after_rate_bps BETWEEN 0 AND 10000),
 reason VARCHAR(500) NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
INSERT INTO routing_commission_history(revision,after_rate_bps,reason) VALUES(0,100,'Initial platform routing commission setup: 1%; routing disabled');
CREATE TRIGGER routing_commission_history_immutable BEFORE UPDATE OR DELETE ON routing_commission_history
FOR EACH ROW EXECUTE FUNCTION reject_monetization_history_mutation();
