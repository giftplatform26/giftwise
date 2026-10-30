# Signup and Account email verification

Host, group creator and member signup requires a correctly formatted email address. Email code verification is optional and does not block signup or depend on email delivery being available.

The signup field shows a green tick for valid format and a red cross for invalid format. A green format indicator does not confirm ownership. The field and signup confirmation remind users to verify their email later in Account to enable recovery.

Accounts created without verification retain a null email_verified_at. Authenticated Account verification confirms ownership with an email code:

- Host: Account → Email verification.
- Platform administrator: My account → Email verification.
- Group creator: Account · email verification, including before group setup.
- Member: Account · contact verification.

The shared sign-in page has no email-verification link. Saved standalone verification URLs remain available. Existing login and recovery security remain unchanged; email recovery requires verified ownership.

The optional CSRF-protected signup-email start/verify APIs remain supported for compatible clients. Supplied proofs must match the email, account kind and scope, expire after ten minutes and can only be consumed once. Invalid supplied proofs roll back account creation. Signup clients may omit emailVerificationToken.

Restart the backend and refresh the frontend after updating. Tests use mocked delivery and do not send emails.
