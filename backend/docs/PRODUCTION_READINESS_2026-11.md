# Production readiness — November 2026

Status of the checks to complete before the November 2026 release goes live. Each line says what was done, the result, and what remains.

## 1. Load test (done 12 November 2026)

Tool: `backend/ops/loadtest/loadtest.mjs` (Node 22, no dependencies). Run against a staging copy, never production:

```sh
node backend/ops/loadtest/loadtest.mjs --base https://staging.example --token <invitation-token> \
  --event <event-id> --user <host-username> --password '<host-password>' --guests 200 --seconds 60
```

Setup: one backend on a MacBook (also running the load generator), PostgreSQL 17 in Docker, a wedding with 400 RSVPs, 300 WhatsApp guests, 250 register entries, 120 wishes, 10 functions with 1,500 answers. 200 guests and 10 hosts made requests continuously with no pause, which is far more demanding than real guests.

| Run | Throughput | Guest p50 / p95 | Host feature check p50 | Errors |
| --- | --- | --- | --- | --- |
| Before | 95 req/s | 1.6–2.6 s / 3.3–4.7 s | 8.3 s | 0% |
| After the fixes below | 299 req/s | 0.61–0.69 s / 0.95–1.22 s | 0.64 s | 0% |

Fixes made:
- `EffectiveModulePermissionService.eventModules` loads all feature switches in 2 queries instead of about 40. It runs on every invitation view.
- The connection pool is configurable: `DB_POOL_SIZE` (default 20) and `DB_POOL_MIN_IDLE` (default 5). Keep the total across all app instances below PostgreSQL `max_connections`.
- Measured with full JIT (`-Dspring-boot.run.optimizedLaunch=false`); `spring-boot:run` otherwise disables part of the JIT and under-reports production speed.

Remaining: repeat on the real staging server (separate load-generator machine, production JVM flags) and record the numbers here. Consider a short cache (5–10 s) for public invitation data if traffic peaks exceed about 300 req/s per instance.

## 2. Security pass (done 12 November 2026)

| Area | Check | Result |
| --- | --- | --- |
| Dependencies | `npm audit --omit=dev` | Web 0, website 0. Mobile: 5 moderate in Expo build tooling (`glob`/`minimatch`), not shipped in the app; the remaining fix needs a breaking Expo change — revisit with the next Expo upgrade. |
| Access control | Every new host API goes through `EventAccess` (owner, or co-host role with capability) and the module switches | Covered by integration tests for owners, co-hosts, gift desk, partners and outsiders (404/403). |
| Money | Payments, receiving accounts, budget and ownership transfer stay owner-only; handover refused once money is involved; charity wishes blocked by a database trigger | Covered by tests. |
| Public endpoints | Read-only public routes use unguessable tokens (invitation, wall, run sheet, pass, receipt); writes are rate-limited per device (`functions/rsvp`, `find-table`, `acquisition-events`) | Reviewed. `find-table` needs 3+ letters, returns at most 5 names, 60 lookups per device per day, and only when the host turns it on. |
| Webhooks | WhatsApp webhook verifies `X-Hub-Signature-256` (HMAC) and the verify token; disabled until both secrets are set | Covered by tests. |
| Injection | SQL uses bound parameters; CSV export neutralises formula characters; React escapes text; JSON-LD escapes `<` | Reviewed. |
| Redirects | Sign-in `next` is validated (relative paths only, no `//`, no admin); sign-up forwards it to sign-in | Reviewed. |
| Secrets | Payment keys, WhatsApp token and app secret come from the backend environment only, never the web environment | Documented in the upgrade steps. |

Remaining: an independent penetration test before enabling live payment collection (Razorpay live keys), as already required by the production runbook.

## 3. Monitoring and alerts (to set up on the server)

- Uptime: point an external monitor at `/actuator/health/readiness` (backend) and `/` (web), alerting after 2 failed checks.
- Errors: the backend logs unhandled exceptions with request ids. Forward logs to the existing log destination and alert on 5xx rate above 1% for 5 minutes. An error-tracking service (for example Sentry) can be added later; it needs an account and a data-processing decision.
- Queues: alert if `transactional_email_outbox` or `whatsapp_messages` has rows `QUEUED` older than 30 minutes.

## 4. Backups (to verify on the server)

`backend/ops/backup.sh`, `verify-backup.py` and `restore-drill.sh` already exist. Before go-live: run a backup, restore it to a scratch database with `restore-drill.sh`, confirm the schema head is 119 and the app starts against it.

## 5. Release checks

- Backend: `./mvnw test` (unit) and `./mvnw test-compile failsafe:integration-test failsafe:verify` (all integration classes).
- Web: `npm test`, `npx tsc --noEmit`, `npx next build`.
- Website: `npm run typecheck`, `npm run build`, `npm test`.
- Mobile: `npm test`.
- The CI workflow in `.github/workflows/ci.yml` runs all of the above on every push.
