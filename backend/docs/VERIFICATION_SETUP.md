# Account, payment and transaction verification

Receiving accounts (host-wide and event-specific) now offer **Verify** and **Verification details**. The event sidebar has **Payment verification**, with checkout/direct-transfer status, provider references and verification timestamps.

## Backend configuration

Configure these secrets in the backend's environment or IDE run configuration, never in source control:

- `CASHFREE_SECUREID_CLIENT_ID`
- `CASHFREE_SECUREID_CLIENT_SECRET`
- `RAZORPAY_KEY_ID` (existing client accepts test keys only)
- `RAZORPAY_KEY_SECRET`
- `RAZORPAY_WEBHOOK_SECRET`

`GIFT_ACCOUNT_VERIFICATION_ENABLED` defaults to `true`. Missing Cashfree credentials make the account-verification settings endpoint report unavailable; no provider request is sent.

`GIFT_ACCOUNT_VERIFICATION_ENVIRONMENT` defaults to `SANDBOX`. Set `PRODUCTION` only with an activated Cashfree Secure ID account and its production credentials. Bank verification uses `/verification/bank-account/sync`; UPI uses `/verification/upi`. The endpoints and fields follow [Cashfree's OpenAPI schema](https://www.cashfree.com/docs/openapi/vrs/vrsv2.yaml).

Sandbox responses are stored as `SANDBOX_VALID` and never change the destination's verification status. Production promotion requires VALID, a provider reference and an exact normalized account-holder name match; bank verification additionally requires DIRECT_MATCH. UPI verification also checks the returned verification ID and VPA. The caller explicitly consents before sending account details. Only active, owned accounts can be checked. Changes to the account during a check invalidate the result.

Restart the backend to apply Flyway V72 and load environment configuration. No real provider calls are performed by the automated tests.

## Checkout verification

The existing Razorpay flow verifies checkout/webhook signatures and independently fetches the provider payment, checking captured status, order, amount and INR before posting contributions. Register `/api/webhooks/razorpay` in the provider dashboard with the same webhook secret. The new host action rechecks only provider IDs already stored against an order belonging to the host's event; it accepts no client-supplied provider payment ID. Existing idempotency prevents duplicate posting.

## Direct transfers

Guest-reported transfers remain AWAITING_VERIFICATION. A personal UPI/bank reference, screenshot or host confirmation does not provide an authenticated bank receipt. These appear with an explicit bank-feed-not-connected explanation. Automatic verification of such receipts requires a collection-provider or bank feed that authenticates incoming credits and binds each credit to the destination, amount, currency, event and unique reference. Do not enable the old reconciliation flag expecting it to create that integration.

This change does not activate withdrawals or live Razorpay checkout.
