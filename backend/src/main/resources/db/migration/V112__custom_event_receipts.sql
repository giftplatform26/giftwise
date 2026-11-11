-- Custom guest receipts designed by the host per event, behind a platform switch (off by default).
INSERT INTO platform_growth_features(code,enabled) VALUES ('CUSTOM_RECEIPTS',FALSE) ON CONFLICT (code) DO NOTHING;

CREATE TABLE event_receipt_templates (
 event_id UUID PRIMARY KEY REFERENCES events(id),
 enabled BOOLEAN NOT NULL DEFAULT FALSE,
 layout VARCHAR(16) NOT NULL DEFAULT 'CLASSIC' CHECK (layout IN ('CLASSIC','ELEGANT','FESTIVE','MINIMAL')),
 accent_color CHAR(7) NOT NULL DEFAULT '#6336B5' CHECK (accent_color ~ '^#[0-9A-Fa-f]{6}$'),
 title VARCHAR(60) NOT NULL DEFAULT 'Gift receipt',
 header_text VARCHAR(120),
 header_subtext VARCHAR(200),
 thank_you_message VARCHAR(400),
 footer_text VARCHAR(400),
 signature_name VARCHAR(80),
 reference_prefix VARCHAR(12) NOT NULL DEFAULT 'GIFT' CHECK (reference_prefix ~ '^[A-Z0-9]{2,12}$'),
 show_guest_name BOOLEAN NOT NULL DEFAULT TRUE,
 show_gift_name BOOLEAN NOT NULL DEFAULT TRUE,
 show_event_date BOOLEAN NOT NULL DEFAULT TRUE,
 show_payment_method BOOLEAN NOT NULL DEFAULT TRUE,
 show_payment_code BOOLEAN NOT NULL DEFAULT TRUE,
 logo_png BYTEA CHECK (logo_png IS NULL OR octet_length(logo_png) <= 400000),
 version BIGINT NOT NULL DEFAULT 0,
 updated_by UUID REFERENCES users(id),
 updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Receipt numbers are unique per event, assigned once when the payment is reported, and never change.
CREATE TABLE event_receipt_counters (
 event_id UUID PRIMARY KEY REFERENCES events(id),
 last_number BIGINT NOT NULL DEFAULT 0 CHECK (last_number >= 0)
);
ALTER TABLE guest_direct_transfer_claims ADD COLUMN receipt_number VARCHAR(24);

WITH numbered AS (
 SELECT id, row_number() OVER (PARTITION BY event_id ORDER BY created_at, id) AS n FROM guest_direct_transfer_claims
)
UPDATE guest_direct_transfer_claims c SET receipt_number = 'GIFT-' || lpad(numbered.n::text, 6, '0')
FROM numbered WHERE numbered.id = c.id;
INSERT INTO event_receipt_counters(event_id, last_number)
SELECT event_id, count(*) FROM guest_direct_transfer_claims GROUP BY event_id;

CREATE UNIQUE INDEX ux_direct_claim_receipt_number ON guest_direct_transfer_claims(event_id, receipt_number) WHERE receipt_number IS NOT NULL;
