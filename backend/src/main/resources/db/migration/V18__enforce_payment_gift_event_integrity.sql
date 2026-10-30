-- Batch 4A.6
-- Prevent cross-event gift references in financial records.

-- Required for composite foreign-key references.
ALTER TABLE gifts
    ADD CONSTRAINT uq_gifts_id_event
    UNIQUE (id, event_id);

-- A payment's gift must belong to its event.
ALTER TABLE payment_orders
    ADD CONSTRAINT fk_payment_order_gift_event
    FOREIGN KEY (gift_id, event_id)
    REFERENCES gifts(id, event_id);

-- A contribution's gift must belong to its event.
ALTER TABLE contributions
    ADD CONSTRAINT fk_contribution_gift_event
    FOREIGN KEY (gift_id, event_id)
    REFERENCES gifts(id, event_id);

CREATE INDEX idx_payment_orders_gift_event
    ON payment_orders(gift_id, event_id);

CREATE INDEX idx_contributions_gift_event
    ON contributions(gift_id, event_id);
