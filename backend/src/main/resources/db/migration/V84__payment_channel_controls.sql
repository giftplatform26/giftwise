CREATE TABLE payment_control_revision(id SMALLINT PRIMARY KEY CHECK(id=1),revision BIGINT NOT NULL);
INSERT INTO payment_control_revision VALUES(1,0);
CREATE TABLE payment_channel_controls (
 scope VARCHAR(10) NOT NULL CHECK(scope IN ('PLATFORM','HOST','EVENT')),
 scope_id UUID NOT NULL, channel VARCHAR(20) NOT NULL CHECK(channel IN ('UPI','BANK','RAZORPAY','ROUTING')),
 enabled BOOLEAN NOT NULL, changed_by UUID REFERENCES users(id), changed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 PRIMARY KEY(scope,scope_id,channel),
 CHECK(scope<>'PLATFORM' OR scope_id='00000000-0000-0000-0000-000000000000')
);
-- Preserve collection availability; routing always requires explicit setup and activation.
INSERT INTO payment_channel_controls(scope,scope_id,channel,enabled) VALUES
 ('PLATFORM','00000000-0000-0000-0000-000000000000','UPI',true),
 ('PLATFORM','00000000-0000-0000-0000-000000000000','BANK',true),
 ('PLATFORM','00000000-0000-0000-0000-000000000000','RAZORPAY',true),
 ('PLATFORM','00000000-0000-0000-0000-000000000000','ROUTING',false);
CREATE TABLE payment_control_history (
 revision BIGINT PRIMARY KEY, actor_id UUID NOT NULL REFERENCES users(id),
 scope VARCHAR(10) NOT NULL, scope_id UUID NOT NULL, channel VARCHAR(20) NOT NULL,
 reason VARCHAR(500) NOT NULL, before_enabled BOOLEAN, after_enabled BOOLEAN,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TRIGGER payment_control_history_immutable BEFORE UPDATE OR DELETE ON payment_control_history
FOR EACH ROW EXECUTE FUNCTION reject_monetization_history_mutation();
ALTER TABLE payment_transactions ADD COLUMN provider_fee_paise BIGINT CHECK(provider_fee_paise>=0);
ALTER TABLE payment_transactions ADD COLUMN provider_tax_paise BIGINT CHECK(provider_tax_paise>=0);
