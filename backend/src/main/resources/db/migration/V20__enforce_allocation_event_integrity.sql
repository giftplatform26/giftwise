-- Ensure allocation event matches its contribution event.

ALTER TABLE contributions
    ADD CONSTRAINT uq_contributions_id_event
    UNIQUE (id, event_id);

ALTER TABLE contribution_allocations
    ADD CONSTRAINT fk_allocation_contribution_event
    FOREIGN KEY (contribution_id, event_id)
    REFERENCES contributions(id, event_id);

-- A gift allocation must also reference the same gift
-- as its original contribution.
-- EVENT_WALLET allocations intentionally have no gift_id.

CREATE OR REPLACE FUNCTION check_contribution_allocation_gift()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    original_gift UUID;
BEGIN
    SELECT gift_id INTO original_gift
    FROM contributions
    WHERE id = NEW.contribution_id;

    IF NEW.allocation_type = 'GIFT'
       AND NEW.gift_id IS DISTINCT FROM original_gift THEN
        RAISE EXCEPTION
            'Gift allocation must match contribution gift';
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_check_contribution_allocation_gift
BEFORE INSERT OR UPDATE
ON contribution_allocations
FOR EACH ROW
EXECUTE FUNCTION check_contribution_allocation_gift();
