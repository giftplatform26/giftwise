-- Batch G4: occasion reminders, receipt-to-ledger, referrals, yearly re-hosting, memory book, vendor directory,
-- experience and charity wishlist items. All off until switched on by the platform admin.
ALTER TABLE platform_module_controls DROP CONSTRAINT module_code_allowed;
ALTER TABLE platform_module_controls ADD CONSTRAINT module_code_allowed CHECK
 (module_code IN ('GROUP_GIFTING','GUEST_GIFTS','RSVP','GUEST_ATTENDANCE','WHATSAPP_SHARING','GUESTBOOK','SOCIAL_SHARING','MEMORY_ALBUM','AI_INVITATION',
  'CELEBRATION_PLANNING','EVENT_ESSENTIALS','EVENT_REUSE','LIVE_WALL','SHAGUN_REGISTER','GIFT_LEDGER','EVENT_FUNCTIONS','CO_HOSTS','WHATSAPP_MESSAGING',
  'OCCASION_REMINDERS','HOST_REFERRALS','YEARLY_REHOST','MEMORY_BOOK','VENDOR_DIRECTORY','EXPERIENCE_GIFTS'));
INSERT INTO platform_module_controls(module_code,enabled) VALUES
 ('OCCASION_REMINDERS',FALSE),('HOST_REFERRALS',FALSE),('YEARLY_REHOST',FALSE),('MEMORY_BOOK',FALSE),('VENDOR_DIRECTORY',FALSE),('EXPERIENCE_GIFTS',FALSE)
 ON CONFLICT (module_code) DO NOTHING;
ALTER TABLE platform_event_module_grants DROP CONSTRAINT event_module_supported;
ALTER TABLE platform_event_module_grants ADD CONSTRAINT event_module_supported CHECK
 (module_code IN ('CELEBRATION_PLANNING','EVENT_ESSENTIALS','EVENT_REUSE','LIVE_WALL','SHAGUN_REGISTER','EVENT_FUNCTIONS','CO_HOSTS','WHATSAPP_MESSAGING',
  'MEMORY_BOOK','EXPERIENCE_GIFTS'));

-- Occasion reminders: dates the user wants to remember (birthdays, anniversaries).
CREATE TABLE user_occasion_reminders (
 id UUID PRIMARY KEY,
 user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 person_name VARCHAR(120) NOT NULL CHECK (length(btrim(person_name)) > 0),
 person_key VARCHAR(120) NOT NULL,
 occasion VARCHAR(40) NOT NULL,
 month SMALLINT NOT NULL CHECK (month BETWEEN 1 AND 12),
 day SMALLINT NOT NULL CHECK (day BETWEEN 1 AND 31),
 since_year SMALLINT CHECK (since_year IS NULL OR since_year BETWEEN 1900 AND 2100),
 note VARCHAR(200),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_reminders_user ON user_occasion_reminders(user_id, month, day);

-- A guest saving their receipt into their own ledger: one ledger row per receipt per user.
ALTER TABLE user_gifts_given ADD COLUMN source_ref VARCHAR(80);
CREATE UNIQUE INDEX ux_given_source ON user_gifts_given(user_id, source_ref) WHERE source_ref IS NOT NULL;

-- Referrals reuse the V105 foundations; remember when a reward was granted.
ALTER TABLE user_referrals ADD COLUMN rewarded_at TIMESTAMPTZ;
ALTER TABLE user_referrals ADD COLUMN rewarded_by UUID REFERENCES users(id);

-- Yearly re-hosting: remember which event a draft was repeated from, so we suggest it once a year.
ALTER TABLE events ADD COLUMN repeated_from UUID REFERENCES events(id);
CREATE INDEX idx_events_repeated_from ON events(repeated_from) WHERE repeated_from IS NOT NULL;

-- Vendor directory curated by the platform admin.
CREATE TABLE platform_vendors (
 id UUID PRIMARY KEY,
 name VARCHAR(120) NOT NULL CHECK (length(btrim(name)) > 0),
 category VARCHAR(24) NOT NULL CHECK (category IN ('DECOR','CATERING','PHOTOGRAPHY','MAKEUP','MEHENDI','MUSIC','VENUE','PLANNER','INVITATIONS','GIFTS','OTHER')),
 city VARCHAR(80) NOT NULL,
 description VARCHAR(600),
 phone VARCHAR(16) CHECK (phone IS NULL OR phone ~ '^\+[1-9][0-9]{7,14}$'),
 website VARCHAR(300) CHECK (website IS NULL OR website ~ '^https://'),
 instagram VARCHAR(60) CHECK (instagram IS NULL OR instagram ~ '^[A-Za-z0-9._]{1,30}$'),
 price_hint VARCHAR(60),
 verified BOOLEAN NOT NULL DEFAULT FALSE,
 active BOOLEAN NOT NULL DEFAULT TRUE,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_vendors_browse ON platform_vendors(active, lower(city), category);
CREATE TABLE user_vendor_shortlist (
 user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 vendor_id UUID NOT NULL REFERENCES platform_vendors(id) ON DELETE CASCADE,
 note VARCHAR(200),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 PRIMARY KEY (user_id, vendor_id)
);

-- Experience and charity wishlist items. Charity items link to the charity's own donation page; GiftWise never
-- takes money for them, enforced below.
CREATE TABLE gift_kinds (
 gift_id UUID PRIMARY KEY REFERENCES gifts(id) ON DELETE CASCADE,
 kind VARCHAR(12) NOT NULL CHECK (kind IN ('ITEM','EXPERIENCE','CHARITY')),
 charity_name VARCHAR(120),
 charity_url VARCHAR(500) CHECK (charity_url IS NULL OR charity_url ~ '^https://'),
 updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 CONSTRAINT charity_needs_link CHECK (kind <> 'CHARITY' OR (charity_url IS NOT NULL AND charity_name IS NOT NULL))
);
CREATE FUNCTION reject_charity_gift_payment() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 IF NEW.gift_id IS NOT NULL AND EXISTS (SELECT 1 FROM gift_kinds WHERE gift_id=NEW.gift_id AND kind='CHARITY') THEN
  RAISE EXCEPTION 'Charity wishes are donated directly to the charity' USING ERRCODE='23514', CONSTRAINT='charity_gift_no_payment';
 END IF;
 RETURN NEW;
END;
$$;
CREATE TRIGGER payment_order_charity_guard BEFORE INSERT ON payment_orders FOR EACH ROW EXECUTE FUNCTION reject_charity_gift_payment();
CREATE TRIGGER direct_claim_charity_guard BEFORE INSERT ON guest_direct_transfer_claims FOR EACH ROW EXECUTE FUNCTION reject_charity_gift_payment();
