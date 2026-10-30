# Batch 5EF — UI API handoff

Enable deliberately with `gift.payment.settlement-ui.enabled=true` in a controlled development environment. Existing session authentication and CSRF apply. Do not enable live payouts.

Base: `/api/v1/host/settlement/events/{eventId}`

- `GET /wallet` — available, held, withdrawable amounts in paise; `livePayoutsEnabled=false`.
- `GET /banks` — verified and enabled host-owned bank destinations, masked only.
- `GET /payouts` — host's requests; `BLOCKED` means no payout initiated.
- `POST /payouts` — `{ "destinationId": "UUID", "amountPaise": 600, "idempotencyKey": "..." }`; creates or replays a blocked request. It does **not** reserve money or initiate payment.
- `POST /payouts/{requestId}/cancel` — idempotently cancels and releases any internal hold.

All requests derive host identity from authenticated session. CSRF token required for POST. Audit records for first creation and first cancellation. Internal reservation and reconciliation endpoints remain unavailable.

## Explicitly pending after handoff

- Full authenticated MockMvc and Testcontainers coverage for new HTTP endpoints and audit rollback.
- Settlement compliance, production wallet-crediting and reconciliation policy.
- Provider-verified payout execution and safe handling of unknown outcomes.
- Production payment profile enablement and secrets provisioning.
- Cross-feature targeted regression and full backend suite at project release.

UI must not label blocked payout requests as successful or show pending gifts as withdrawable.
