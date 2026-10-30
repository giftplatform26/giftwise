-- Provider-derived details only; never store card data or complete provider payloads.
ALTER TABLE payment_transactions
 ADD COLUMN payment_method TEXT,
 ADD COLUMN bank_code TEXT,
 ADD COLUMN bank_transaction_id TEXT,
 ADD COLUMN rrn TEXT,
 ADD COLUMN upi_transaction_id TEXT;
