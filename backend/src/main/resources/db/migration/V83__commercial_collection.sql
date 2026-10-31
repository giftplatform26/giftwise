CREATE TABLE commercial_policy (
 id SMALLINT PRIMARY KEY CHECK(id=1), revision BIGINT NOT NULL DEFAULT 0,
 free_active_events INTEGER NOT NULL CHECK(free_active_events BETWEEN 0 AND 1000),
 host_plan_days INTEGER NOT NULL CHECK(host_plan_days BETWEEN 1 AND 3660),
 vendor_days INTEGER NOT NULL CHECK(vendor_days BETWEEN 1 AND 3660),
 sponsor_days INTEGER NOT NULL CHECK(sponsor_days BETWEEN 1 AND 3660)
);
INSERT INTO commercial_policy VALUES(1,0,1,365,30,30);
CREATE TABLE commercial_setup_history (
 id UUID PRIMARY KEY, actor_id UUID NOT NULL REFERENCES users(id),
 resource_type VARCHAR(40) NOT NULL, resource_id VARCHAR(100) NOT NULL,
 reason VARCHAR(500) NOT NULL, before_config JSONB, after_config JSONB NOT NULL,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TRIGGER commercial_setup_history_immutable BEFORE UPDATE OR DELETE ON commercial_setup_history
FOR EACH ROW EXECUTE FUNCTION reject_monetization_history_mutation();
CREATE TABLE commercial_placements (
 id UUID PRIMARY KEY, owner_id UUID NOT NULL REFERENCES users(id),
 stream VARCHAR(40) NOT NULL CHECK(stream IN ('VENDOR_LISTING','SPONSORED_GIFT')),
 title VARCHAR(160) NOT NULL, description VARCHAR(1000) NOT NULL, url VARCHAR(2048) NOT NULL,
 category VARCHAR(80) NOT NULL, occasion VARCHAR(50) NOT NULL,
 moderation VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK(moderation IN ('PENDING','APPROVED','REJECTED')),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE commercial_orders (
 id UUID PRIMARY KEY, buyer_kind VARCHAR(20) NOT NULL CHECK(buyer_kind IN ('HOST','GROUP_ADMIN')),
 buyer_id UUID NOT NULL, stream VARCHAR(40) NOT NULL CHECK(stream IN ('HOST_PLAN','PREMIUM_TEMPLATE','VENDOR_LISTING','SPONSORED_GIFT','GROUP_GIFT')),
 resource_id UUID NOT NULL, amount_paise BIGINT NOT NULL CHECK(amount_paise>=0),
 pricing_revision BIGINT NOT NULL REFERENCES platform_monetization_history(revision),
 policy_revision BIGINT NOT NULL, duration_days INTEGER NOT NULL CHECK(duration_days BETWEEN 0 AND 3660),
 idempotency_key VARCHAR(120) NOT NULL, provider_order_id VARCHAR(100) UNIQUE,
 provider_payment_id VARCHAR(100) UNIQUE, status VARCHAR(20) NOT NULL CHECK(status IN ('CREATED','PENDING','CAPTURED','FREE')),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(), paid_at TIMESTAMPTZ,
 UNIQUE(buyer_kind,buyer_id,idempotency_key),
 CHECK((status='CAPTURED' AND amount_paise>0 AND provider_payment_id IS NOT NULL AND paid_at IS NOT NULL)
    OR (status='FREE' AND amount_paise=0 AND paid_at IS NOT NULL)
    OR status IN ('CREATED','PENDING'))
);
CREATE TABLE commercial_entitlements (
 order_id UUID PRIMARY KEY REFERENCES commercial_orders(id), buyer_kind VARCHAR(20) NOT NULL,
 buyer_id UUID NOT NULL, stream VARCHAR(40) NOT NULL, resource_id UUID NOT NULL,
 starts_at TIMESTAMPTZ NOT NULL, expires_at TIMESTAMPTZ,
 CHECK(expires_at IS NULL OR expires_at>starts_at)
);
CREATE INDEX commercial_entitlements_lookup ON commercial_entitlements(buyer_kind,buyer_id,stream,resource_id);
CREATE TABLE commercial_revenue (
 order_id UUID PRIMARY KEY REFERENCES commercial_orders(id), amount_paise BIGINT NOT NULL CHECK(amount_paise>0),
 currency VARCHAR(3) NOT NULL CHECK(currency='INR'), created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE FUNCTION preserve_commercial_order_pricing() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 IF ROW(NEW.buyer_kind,NEW.buyer_id,NEW.stream,NEW.resource_id,NEW.amount_paise,NEW.pricing_revision,NEW.policy_revision,NEW.duration_days,NEW.idempotency_key)
 IS DISTINCT FROM ROW(OLD.buyer_kind,OLD.buyer_id,OLD.stream,OLD.resource_id,OLD.amount_paise,OLD.pricing_revision,OLD.policy_revision,OLD.duration_days,OLD.idempotency_key)
 THEN RAISE EXCEPTION 'Commercial order pricing is immutable'; END IF;
 IF OLD.status IN ('CAPTURED','FREE') AND NEW IS DISTINCT FROM OLD THEN RAISE EXCEPTION 'Completed commercial orders are immutable'; END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER commercial_order_immutable BEFORE UPDATE ON commercial_orders
FOR EACH ROW EXECUTE FUNCTION preserve_commercial_order_pricing();
CREATE TABLE affiliate_partners (
 id UUID PRIMARY KEY, name VARCHAR(100) NOT NULL, product_domain VARCHAR(253) NOT NULL UNIQUE,
 enabled BOOLEAN NOT NULL DEFAULT false, parameter_name VARCHAR(60) NOT NULL,
 parameter_value VARCHAR(500) NOT NULL, click_parameter VARCHAR(60),
 redirect_template VARCHAR(2048), revision BIGINT NOT NULL DEFAULT 0
);
CREATE TABLE affiliate_clicks (
 id UUID PRIMARY KEY, partner_id UUID NOT NULL REFERENCES affiliate_partners(id),
 event_id UUID NOT NULL REFERENCES events(id), gift_id UUID NOT NULL REFERENCES gifts(id),
 destination_url TEXT NOT NULL CHECK(length(destination_url)<=16384), created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE affiliate_commissions (
 id UUID PRIMARY KEY, partner_id UUID NOT NULL REFERENCES affiliate_partners(id),
 click_id UUID REFERENCES affiliate_clicks(id), external_reference VARCHAR(160) NOT NULL,
 amount_paise BIGINT NOT NULL CHECK(amount_paise>0), state VARCHAR(20) NOT NULL CHECK(state IN ('CONFIRMED','PAID','REVERSED')),
 revision BIGINT NOT NULL DEFAULT 0, changed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 UNIQUE(partner_id,external_reference)
);
CREATE TABLE commercial_placement_clicks (
 id UUID PRIMARY KEY, placement_id UUID NOT NULL REFERENCES commercial_placements(id), created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
