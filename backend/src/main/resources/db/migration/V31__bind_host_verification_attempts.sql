-- Request binding is immutable after creation. Never store bank/UPI plaintext here.
ALTER TABLE host_destination_verification_attempts
 ADD COLUMN request_provider VARCHAR(40),
 ADD COLUMN request_reference VARCHAR(120),
 ADD COLUMN request_fund_account_id VARCHAR(120);
ALTER TABLE host_destination_verification_attempts
 ADD CONSTRAINT chk_verification_request_binding CHECK (
   (request_provider IS NULL AND request_reference IS NULL AND request_fund_account_id IS NULL)
   OR (request_provider IS NOT NULL AND length(trim(request_provider)) > 0
       AND request_reference IS NOT NULL AND length(trim(request_reference)) > 0
       AND request_fund_account_id IS NOT NULL AND length(trim(request_fund_account_id)) > 0)
 );
CREATE UNIQUE INDEX ux_host_verification_request_reference
 ON host_destination_verification_attempts(request_provider,request_reference)
 WHERE request_reference IS NOT NULL;
