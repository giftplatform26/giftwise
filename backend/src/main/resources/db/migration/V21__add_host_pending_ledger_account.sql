-- Batch 4A.13: Pending host wallet accounting
-- Funds must not become available before settlement.

ALTER TABLE wallet_ledger_entries
    DROP CONSTRAINT ck_wallet_account_code;

ALTER TABLE wallet_ledger_entries
    ADD CONSTRAINT ck_wallet_account_code
    CHECK (
        account_code IN (
            'PAYMENT_CLEARING',
            'GIFT_RESERVED',
            'HOST_PENDING',
            'HOST_AVAILABLE',
            'REFUND_PENDING'
        )
    );
