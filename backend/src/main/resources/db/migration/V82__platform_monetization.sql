-- Prices are managed by platform administrators. Seeds are deliberately inactive.
CREATE TABLE platform_monetization_settings (
 id SMALLINT PRIMARY KEY CHECK(id=1), enabled BOOLEAN NOT NULL,
 revision BIGINT NOT NULL CHECK(revision>=0), changed_by UUID REFERENCES users(id),
 changed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
INSERT INTO platform_monetization_settings(id,enabled,revision) VALUES(1,false,0);
CREATE TABLE platform_monetization_streams (
 code VARCHAR(40) PRIMARY KEY CHECK(code IN ('CONTRIBUTION','WALLET','AFFILIATE','PREMIUM_TEMPLATE','HOST_PLAN','VENDOR_LISTING','SPONSORED_GIFT','GROUP_GIFT')),
 enabled BOOLEAN NOT NULL,
 fee_mode VARCHAR(20) NOT NULL CHECK(fee_mode IN ('FIXED','PERCENTAGE')),
 fixed_paise BIGINT NOT NULL CHECK(fixed_paise BETWEEN 0 AND 100000000),
 rate_bps INTEGER NOT NULL CHECK(rate_bps BETWEEN 0 AND 10000),
 cap_paise BIGINT NOT NULL CHECK(cap_paise BETWEEN 0 AND 100000000),
 fee_bearer VARCHAR(20) NOT NULL CHECK(fee_bearer IN ('GUEST','HOST'))
);
INSERT INTO platform_monetization_streams VALUES
 ('CONTRIBUTION',false,'PERCENTAGE',100,100,0,'GUEST'),
 ('WALLET',false,'PERCENTAGE',100,100,0,'HOST'),
 ('AFFILIATE',false,'PERCENTAGE',0,0,0,'GUEST'),
 ('PREMIUM_TEMPLATE',false,'FIXED',9900,0,0,'GUEST'),
 ('HOST_PLAN',false,'FIXED',29900,0,0,'GUEST'),
 ('VENDOR_LISTING',false,'FIXED',99900,0,0,'GUEST'),
 ('SPONSORED_GIFT',false,'FIXED',0,0,0,'GUEST'),
 ('GROUP_GIFT',false,'PERCENTAGE',9900,100,0,'GUEST');
CREATE TABLE platform_monetization_history (
 revision BIGINT PRIMARY KEY, actor_id UUID REFERENCES users(id),
 reason VARCHAR(500) NOT NULL, before_config JSONB, after_config JSONB NOT NULL,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
INSERT INTO platform_monetization_history(revision,reason,after_config)
SELECT 0,'Initial inactive setup',jsonb_build_object('enabled',false,'streams',(SELECT jsonb_agg(to_jsonb(s) ORDER BY code) FROM platform_monetization_streams s));
ALTER TABLE payment_orders ADD COLUMN requested_amount_paise BIGINT;
UPDATE payment_orders SET requested_amount_paise=amount_paise;
ALTER TABLE payment_orders ALTER COLUMN requested_amount_paise SET NOT NULL;
-- Legacy SQL callers may omit this field; keep the invariant through a trigger.
CREATE FUNCTION payment_order_requested_amount() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 IF NEW.requested_amount_paise IS NULL THEN NEW.requested_amount_paise=NEW.amount_paise; END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER payment_order_requested_amount BEFORE INSERT ON payment_orders
FOR EACH ROW EXECUTE FUNCTION payment_order_requested_amount();
ALTER TABLE payment_orders ADD COLUMN platform_fee_paise BIGINT NOT NULL DEFAULT 0;
ALTER TABLE payment_orders ADD COLUMN host_receives_paise BIGINT;
UPDATE payment_orders SET host_receives_paise=amount_paise;
-- COALESCE permits old SQL writers while new checkouts always save explicit snapshots.
ALTER TABLE payment_orders ADD COLUMN monetization_revision BIGINT REFERENCES platform_monetization_history(revision);
ALTER TABLE payment_orders ADD CONSTRAINT ck_order_fee_snapshot CHECK(
 requested_amount_paise>0 AND platform_fee_paise>=0 AND
 COALESCE(host_receives_paise,amount_paise)>0 AND
 amount_paise=COALESCE(host_receives_paise,amount_paise)+platform_fee_paise AND
 requested_amount_paise IN (amount_paise,COALESCE(host_receives_paise,amount_paise)));
ALTER TABLE wallet_ledger_entries DROP CONSTRAINT ck_wallet_account_code;
ALTER TABLE wallet_ledger_entries ADD CONSTRAINT ck_wallet_account_code CHECK(account_code IN
 ('PAYMENT_CLEARING','GIFT_RESERVED','HOST_PENDING','HOST_AVAILABLE','REFUND_PENDING','PLATFORM_REVENUE'));

-- Preserve order pricing and administrative history after creation.
CREATE FUNCTION reject_monetization_history_mutation() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN RAISE EXCEPTION 'Monetization history is append-only'; END $$;
CREATE TRIGGER monetization_history_immutable BEFORE UPDATE OR DELETE ON platform_monetization_history
FOR EACH ROW EXECUTE FUNCTION reject_monetization_history_mutation();
CREATE FUNCTION preserve_payment_order_pricing() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 IF ROW(NEW.amount_paise,NEW.requested_amount_paise,NEW.platform_fee_paise,NEW.host_receives_paise,NEW.monetization_revision)
    IS DISTINCT FROM ROW(OLD.amount_paise,OLD.requested_amount_paise,OLD.platform_fee_paise,OLD.host_receives_paise,OLD.monetization_revision)
 THEN RAISE EXCEPTION 'Payment order pricing is immutable'; END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER payment_order_pricing_immutable BEFORE UPDATE ON payment_orders
FOR EACH ROW EXECUTE FUNCTION preserve_payment_order_pricing();
