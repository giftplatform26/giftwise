-- Data foundations only. Existing checkout stays INR-only and tenant authorization
-- must be implemented before tenant-specific routes, billing or promotions are enabled.
CREATE TABLE platform_tenants (
 id UUID PRIMARY KEY DEFAULT gen_random_uuid(), slug VARCHAR(63) NOT NULL UNIQUE,
 display_name VARCHAR(120) NOT NULL, enabled BOOLEAN NOT NULL DEFAULT false,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 CHECK(slug ~ '^[a-z0-9][a-z0-9-]{1,62}$'), CHECK(length(btrim(display_name))>0)
);
CREATE TABLE tenant_branding (
 tenant_id UUID PRIMARY KEY REFERENCES platform_tenants(id),
 application_name VARCHAR(80) NOT NULL, brand_color VARCHAR(7) NOT NULL DEFAULT '#5b21b6',
 logo_id UUID REFERENCES platform_brand_assets(id), favicon_id UUID REFERENCES platform_brand_assets(id),
 support_email VARCHAR(254), footer_text VARCHAR(240), revision BIGINT NOT NULL DEFAULT 0,
 CHECK(brand_color ~ '^#[0-9a-fA-F]{6}$'), CHECK(length(btrim(application_name))>0)
);
CREATE TABLE tenant_memberships (
 tenant_id UUID NOT NULL REFERENCES platform_tenants(id), user_id UUID NOT NULL REFERENCES users(id),
 role VARCHAR(20) NOT NULL CHECK(role IN ('OWNER','ADMIN','MEMBER')),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(), PRIMARY KEY(tenant_id,user_id)
);
CREATE TABLE tenant_events (
 event_id UUID PRIMARY KEY REFERENCES events(id), tenant_id UUID NOT NULL REFERENCES platform_tenants(id)
);
CREATE INDEX idx_tenant_events ON tenant_events(tenant_id,event_id);
CREATE TABLE supported_currencies (
 code VARCHAR(3) PRIMARY KEY CHECK(code ~ '^[A-Z]{3}$'), minor_units SMALLINT NOT NULL CHECK(minor_units BETWEEN 0 AND 4),
 checkout_enabled BOOLEAN NOT NULL DEFAULT false
);
INSERT INTO supported_currencies(code,minor_units,checkout_enabled) VALUES
 ('INR',2,true),('USD',2,false),('EUR',2,false),('GBP',2,false),('AED',2,false),('JPY',0,false);
CREATE TABLE promotional_coupons (
 id UUID PRIMARY KEY DEFAULT gen_random_uuid(), tenant_id UUID REFERENCES platform_tenants(id),
 code VARCHAR(40) NOT NULL CHECK(code ~ '^[A-Z0-9_]{3,40}$'),
 discount_kind VARCHAR(16) NOT NULL CHECK(discount_kind IN ('PERCENT','FIXED')),
 percentage SMALLINT, amount_minor BIGINT, currency VARCHAR(3) REFERENCES supported_currencies(code),
 starts_at TIMESTAMPTZ NOT NULL, ends_at TIMESTAMPTZ NOT NULL,
 max_redemptions INTEGER CHECK(max_redemptions>0), per_user_limit INTEGER NOT NULL DEFAULT 1 CHECK(per_user_limit>0),
 enabled BOOLEAN NOT NULL DEFAULT false, created_by UUID REFERENCES users(id),
 CHECK(ends_at>starts_at),
 CHECK((discount_kind='PERCENT' AND percentage IS NOT NULL AND percentage BETWEEN 1 AND 100 AND amount_minor IS NULL AND currency IS NULL)
    OR (discount_kind='FIXED' AND percentage IS NULL AND amount_minor IS NOT NULL AND amount_minor>0 AND currency IS NOT NULL))
);
CREATE UNIQUE INDEX ux_coupon_tenant_code ON promotional_coupons(tenant_id,code) WHERE tenant_id IS NOT NULL;
CREATE UNIQUE INDEX ux_coupon_platform_code ON promotional_coupons(code) WHERE tenant_id IS NULL;
CREATE TABLE coupon_redemptions (
 id UUID PRIMARY KEY DEFAULT gen_random_uuid(), coupon_id UUID NOT NULL REFERENCES promotional_coupons(id),
 user_id UUID NOT NULL REFERENCES users(id), source_reference VARCHAR(180) NOT NULL UNIQUE,
 discount_minor BIGINT NOT NULL CHECK(discount_minor>=0), currency VARCHAR(3) NOT NULL REFERENCES supported_currencies(code),
 status VARCHAR(16) NOT NULL CHECK(status IN ('RESERVED','APPLIED','RELEASED')),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(), expires_at TIMESTAMPTZ,
 CHECK(expires_at IS NULL OR expires_at>created_at)
);
CREATE INDEX idx_coupon_redemption_limits ON coupon_redemptions(coupon_id,user_id,status);
CREATE TABLE referral_codes (
 id UUID PRIMARY KEY DEFAULT gen_random_uuid(), tenant_id UUID REFERENCES platform_tenants(id),
 referrer_id UUID NOT NULL REFERENCES users(id), code VARCHAR(40) NOT NULL UNIQUE CHECK(code ~ '^[A-Z0-9_]{3,40}$'),
 enabled BOOLEAN NOT NULL DEFAULT false, created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 UNIQUE(id,referrer_id)
);
CREATE TABLE user_referrals (
 id UUID PRIMARY KEY DEFAULT gen_random_uuid(), referral_code_id UUID NOT NULL,
 referrer_id UUID NOT NULL REFERENCES users(id), referred_user_id UUID NOT NULL REFERENCES users(id),
 status VARCHAR(20) NOT NULL DEFAULT 'ATTRIBUTED' CHECK(status IN ('ATTRIBUTED','QUALIFIED','REWARDED','CANCELLED')),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(), UNIQUE(referred_user_id),
 FOREIGN KEY(referral_code_id,referrer_id) REFERENCES referral_codes(id,referrer_id),
 CHECK(referrer_id<>referred_user_id)
);
CREATE TABLE subscription_plans (
 id UUID PRIMARY KEY DEFAULT gen_random_uuid(), code VARCHAR(50) NOT NULL UNIQUE CHECK(length(btrim(code))>0), name VARCHAR(120) NOT NULL CHECK(length(btrim(name))>0),
 price_minor BIGINT NOT NULL CHECK(price_minor>=0), currency VARCHAR(3) NOT NULL REFERENCES supported_currencies(code),
 billing_period VARCHAR(10) NOT NULL CHECK(billing_period IN ('MONTH','YEAR')),
 entitlements JSONB NOT NULL DEFAULT '{}'::jsonb CHECK(jsonb_typeof(entitlements)='object'),
 enabled BOOLEAN NOT NULL DEFAULT false, version BIGINT NOT NULL DEFAULT 1 CHECK(version>0)
);
CREATE TABLE user_subscriptions (
 id UUID PRIMARY KEY DEFAULT gen_random_uuid(), user_id UUID NOT NULL REFERENCES users(id),
 tenant_id UUID REFERENCES platform_tenants(id), plan_id UUID NOT NULL REFERENCES subscription_plans(id),
 provider VARCHAR(40) NOT NULL, provider_subscription_id VARCHAR(180) NOT NULL,
 status VARCHAR(20) NOT NULL CHECK(status IN ('PENDING','ACTIVE','PAST_DUE','CANCELLED','EXPIRED')),
 period_start TIMESTAMPTZ, period_end TIMESTAMPTZ,
 price_minor BIGINT NOT NULL CHECK(price_minor>=0), currency VARCHAR(3) NOT NULL REFERENCES supported_currencies(code),
 entitlement_snapshot JSONB NOT NULL DEFAULT '{}'::jsonb CHECK(jsonb_typeof(entitlement_snapshot)='object'),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(), UNIQUE(provider,provider_subscription_id),
 FOREIGN KEY(tenant_id,user_id) REFERENCES tenant_memberships(tenant_id,user_id),
 CHECK(length(btrim(provider))>0 AND length(btrim(provider_subscription_id))>0),
 CHECK((period_start IS NULL AND period_end IS NULL) OR (period_start IS NOT NULL AND period_end IS NOT NULL AND period_end>period_start))
);
CREATE INDEX idx_user_subscriptions ON user_subscriptions(user_id,status);
CREATE TABLE gift_recommendations (
 id UUID PRIMARY KEY DEFAULT gen_random_uuid(), event_id UUID NOT NULL REFERENCES events(id),
 requested_by UUID NOT NULL REFERENCES users(id), model_reference VARCHAR(120) NOT NULL,
 recommendations JSONB NOT NULL CHECK(jsonb_typeof(recommendations)='array'),
 status VARCHAR(16) NOT NULL DEFAULT 'DRAFT' CHECK(status IN ('DRAFT','ACCEPTED','DISMISSED')),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_gift_recommendations ON gift_recommendations(event_id,created_at DESC);
