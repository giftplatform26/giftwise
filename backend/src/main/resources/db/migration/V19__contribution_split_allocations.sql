-- Batch 4A.12
-- One verified contribution may be split between
-- gift funding and the host event wallet.
-- Allocation rows represent financial distribution,
-- not independent payments or spendable ledger credits.

CREATE TABLE contribution_allocations (
    id UUID PRIMARY KEY,

    contribution_id UUID NOT NULL
        REFERENCES contributions(id),

    event_id UUID NOT NULL
        REFERENCES events(id),

    gift_id UUID,

    allocation_type VARCHAR(30) NOT NULL,

    amount_paise BIGINT NOT NULL
        CHECK (amount_paise > 0),

    created_at TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT ck_contribution_allocation_type
        CHECK (
            allocation_type IN (
                'GIFT',
                'EVENT_WALLET'
            )
        ),

    CONSTRAINT ck_contribution_allocation_target
        CHECK (
            (allocation_type = 'GIFT' AND gift_id IS NOT NULL)
            OR
            (allocation_type = 'EVENT_WALLET' AND gift_id IS NULL)
        ),

    CONSTRAINT fk_allocation_gift_event
        FOREIGN KEY (gift_id, event_id)
        REFERENCES gifts(id, event_id),

    CONSTRAINT uq_contribution_allocation_type
        UNIQUE (
            contribution_id,
            allocation_type
        )
);

CREATE INDEX idx_contribution_allocations_event
    ON contribution_allocations(event_id);

CREATE INDEX idx_contribution_allocations_gift
    ON contribution_allocations(gift_id)
    WHERE gift_id IS NOT NULL;

CREATE INDEX idx_contribution_allocations_contribution
    ON contribution_allocations(contribution_id);
