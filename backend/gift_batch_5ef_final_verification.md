# Batch 5EF final verification

Run `bash ~/Downloads/gift_5ef_final_verify.sh` from your Mac (after downloading).

- HTTP: real Spring Security filter chain, authentication, CSRF, blocked state, no-store, internal route denial.
- PostgreSQL: real V33-V35 schema, host isolation, verified destinations, audit idempotency, audit rollback, no reservations or provider calls.
- Existing Batch 5CD concurrency tests included as targeted regression.

These tests do not enable live payouts. `gift.payment.settlement-ui.enabled` is set only in the HTTP test context.

For frontend: `gift_batch_5ef_api_handoff.md` describes API contracts. No claim of live transfer success is permitted.
