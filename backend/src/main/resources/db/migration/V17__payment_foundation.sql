-- Batch 4A: Payment foundation
-- All monetary values are integer minor units (INR paise).

CREATE TABLE payment_orders (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL REFERENCES events(id),
    gift_id UUID REFERENCES gifts(id),
    provider VARCHAR(20) NOT NULL DEFAULT 'RAZORPAY',
    provider_order_id VARCHAR(100) UNIQUE,
    contribution_type VARCHAR(30) NOT NULL,
    amount_paise BIGINT NOT NULL CHECK (amount_paise > 0),
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    status VARCHAR(30) NOT NULL DEFAULT 'CREATED',
    idempotency_key VARCHAR(120) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_payment_order_type CHECK (
        contribution_type IN (
            'GIFT_PARTIAL',
            'GIFT_FULL',
            'EVENT_WALLET'
        )
    ),
    CONSTRAINT ck_payment_order_gift CHECK (
        (contribution_type = 'EVENT_WALLET' AND gift_id IS NULL)
        OR
        (contribution_type IN ('GIFT_PARTIAL','GIFT_FULL')
         AND gift_id IS NOT NULL)
    ),
    CONSTRAINT ck_payment_order_status CHECK (
        status IN (
            'CREATED',
            'PENDING',
            'AUTHORIZED',
            'CAPTURED',
            'FAILED',
            'CANCELLED'
        )
    ),
    CONSTRAINT ck_payment_order_currency CHECK (currency = 'INR')
);

CREATE TABLE payment_transactions (
    id UUID PRIMARY KEY,
    payment_order_id UUID NOT NULL REFERENCES payment_orders(id),
    provider_payment_id VARCHAR(100) NOT NULL UNIQUE,
    amount_paise BIGINT NOT NULL CHECK (amount_paise > 0),
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    status VARCHAR(30) NOT NULL,
    verified_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_payment_transaction_status CHECK (
        status IN (
            'AUTHORIZED',
            'CAPTURED',
            'FAILED',
            'REFUNDED',
            'PARTIALLY_REFUNDED'
        )
    )
);

CREATE TABLE contributions (
    id UUID PRIMARY KEY,
    payment_order_id UUID NOT NULL UNIQUE
        REFERENCES payment_orders(id),
    event_id UUID NOT NULL REFERENCES events(id),
    gift_id UUID REFERENCES gifts(id),
    guest_name VARCHAR(200) NOT NULL
        CHECK (length(btrim(guest_name)) > 0),
    guest_email VARCHAR(320),
    guest_phone VARCHAR(30),
    amount_paise BIGINT NOT NULL CHECK (amount_paise > 0),
    allocation_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_contribution_allocation CHECK (
        allocation_status IN (
            'PENDING',
            'GIFT_ALLOCATED',
            'WALLET_ALLOCATED',
            'REALLOCATED',
            'REFUNDED'
        )
    )
);

CREATE TABLE wallet_ledger_entries (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL REFERENCES events(id),
    contribution_id UUID REFERENCES contributions(id),
    entry_group_id UUID NOT NULL,
    account_code VARCHAR(40) NOT NULL,
    amount_paise BIGINT NOT NULL CHECK (amount_paise <> 0),
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    entry_type VARCHAR(40) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_wallet_account_code CHECK (
        account_code IN (
            'PAYMENT_CLEARING',
            'GIFT_RESERVED',
            'HOST_AVAILABLE',
            'REFUND_PENDING'
        )
    )
);

CREATE TABLE payment_webhook_events (
    id UUID PRIMARY KEY,
    provider VARCHAR(20) NOT NULL DEFAULT 'RAZORPAY',
    provider_event_id VARCHAR(150) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'RECEIVED',
    received_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMPTZ,
    UNIQUE (provider, provider_event_id),
    CONSTRAINT ck_webhook_status CHECK (
        status IN ('RECEIVED','PROCESSED','FAILED')
    )
);

CREATE INDEX idx_payment_orders_event
    ON payment_orders(event_id, created_at);

CREATE INDEX idx_payment_orders_gift
    ON payment_orders(gift_id);

CREATE INDEX idx_contributions_event
    ON contributions(event_id, created_at);

CREATE INDEX idx_contributions_gift
    ON contributions(gift_id);

CREATE INDEX idx_wallet_ledger_event
    ON wallet_ledger_entries(event_id, created_at);

CREATE INDEX idx_wallet_ledger_group
    ON wallet_ledger_entries(entry_group_id);
