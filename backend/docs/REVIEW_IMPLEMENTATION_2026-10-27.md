# Application review implementation — 27 October 2026

Scope: flow correctness, data accuracy and performance. A broader browser/mobile redesign and customer acquisition features remain the next stages.

## Compatibility

- Member logout passes platform authentication routing but still requires CSRF. It clears only that group's member identity and credential epoch.
- Group sessions continue to be validated in their controllers. Host/admin authentication and immediate credential revocation remain intact.
- PIN policy controls registration, sign-in and setup. Disabling PIN keeps password registration and password sign-in available. Existing PIN-only API clients remain supported when PIN is enabled; current forms accept an optional account name so named PIN sign-in avoids scanning every account hash. PIN-only browser sign-in remains available.
- Creator registration signs in through the existing login endpoint (including session rotation/page identity). Approved auto-join members similarly sign in through the existing member login. Pending members still require approval. No new authentication mechanism was introduced.
- Manual reports remain unverified. Dashboard summaries label them accordingly; they do not credit financial balances or the ledger.
- Report SQL keeps the existing provider verification, allocation posting and split-payment rules. Selected gift progress is independent of report pagination, date and status filters. Page rows are bounded in SQL, with separate aggregates. CSV retains its record cap.

## Operational measurement

Spring HTTP request metrics are available to authenticated platform administrators only:

- `/actuator/metrics/http.server.requests`
- Filter by the templated `uri` and HTTP status tags when comparing screens. No token or account identifiers should be used as metric tags.
- Latency histogram buckets: 100 ms, 300 ms, 1 s, 3 s. Capture a baseline in the deployed environment; source review cannot establish actual speed gains.

Opt-in browser diagnostics: build the web application with `NEXT_PUBLIC_DIAGNOSTICS_ENABLED=true`. Inspect `window.giftDiagnostics()` in the browser console. Samples stay in memory (maximum 200), with UUIDs/numeric identifiers redacted and query strings removed. No data is sent to a third party or persisted. Flow counters distinguish started, reported/unverified, confirmed, failed, cancelled and confirmation pending. Browser interactions are raw event durations, not a full INP calculation. Screen shell timing does not mean data has finished loading; invitation/dashboard data-ready measurements are separate.

This diagnostic baseline is for development and acceptance checks; cross-session business conversion reporting will require an explicitly chosen analytics destination later.

## Verification

- Full backend default test suite: 569 tests passed (`./mvnw -q test`).
- Focused PostgreSQL regression suite: 6 tests passed, covering database pagination/totals, UTC filters, pending provider orders, verified split allocations, CSV exports, and group dashboard summaries (`./mvnw -q test-compile failsafe:integration-test failsafe:verify -Dit.test=HostReportPaginationIntegrationTest`).
- Frontend: 333 tests passed (`npm test`).
- Production web build passed (`npm run build`); existing CSS autoprefixer warnings remain.
- Browser/device visual testing and real provider payment testing have not been performed in this change. Actual latency improvements require measurements in the deployed environment.
- Updated pre-existing test fixtures to supply current policy services, registration contacts, event contact/address details and mandatory host display names. Production requirements were preserved.
- No database migrations, provider capture logic or financial ledger writes were changed.

Source snapshots for the initial edits are retained outside the application under `/tmp/gift-review-originals` for this session.
