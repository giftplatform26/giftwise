# Application clicks and outcomes

Admin navigation: **Clicks & outcomes**. Only platform administrators can read the reports and related request timeline.

Deploy both frontend and backend. Migration V99 creates the interaction store. Collection begins after deployment; historical clicks cannot be reconstructed. Report dates use UTC and default to the last 30 days.

## Coverage

- Trusted browser clicks on links, buttons, menu items, tabs, selectors, checkboxes, radios, and disclosure controls. Generated download anchor clicks are excluded to avoid double counting.
- Page opens and successful observed in-app navigation.
- Invitation and member link copying, native sharing, share cancellation and copy failures.
- QR, invitation PNG, calendar and contribution CSV download handoffs and failures.
- Checkout confirmation, provider failure, dismissal and pending confirmation, including group coordination/template purchases.
- Guest/member payment instructions and transfer-report submission. Transfer submissions remain explicitly unverified.
- Server-observed invitation API opens, QR/image/file requests, member/payment requests, and other API mutations. Background catalogue reads and analytics requests are excluded.

## Interpreting the report

Each browser click has a UUID. Explicit handlers update that row as the operation progresses, rather than adding another click. Requests initiated directly by the click can carry its UUID and appear under **What happened?**. Later asynchronous requests may be independent records when no reliable correlation is available.

Click, page-view, operation, and request counts are distinct. An HTTP 2xx is request-level evidence; it is not confirmation of funds received, successful file saving, or completion in another app. Download handoff means the browser received the generated file; the browser does not expose whether it was saved. External links remain handed off unless a separate outcome is observed. Uninstrumented operations remain unconfirmed.

Browser outcomes are client-reported and labelled BROWSER. SERVER records describe actual HTTP responses. Financial reconciliation must continue to use existing verified payment records.

Host/event identity is resolved on the server from the authenticated actor, an owned event, or a published public event token. Public activity is assigned to the event owner. Host-wide actions without an event remain host-wide; unrelated administrator and anonymous actions may remain unattributed. Forged private event IDs cannot assign another host's activity.

## Data and resilience

No form values, clipboard contents, raw public tokens in page paths, full external URLs, request bodies, transaction references, IP addresses, or payment details are stored. Routes keep structural segments and replace dynamic identifiers with `:id`. Known static localization keys can identify controls without storing their displayed/user-authored text.

Anonymous intake requires CSRF. Batches are limited to 50 entries and 600 entries per minute per session. Retried outcomes upsert by ID and server session hash; another session cannot overwrite them, and browser intake cannot overwrite server evidence. Client delivery uses bounded batches, keepalive, and bounded retries. Tracking outages do not block application requests; browser telemetry is best effort and is not an audit ledger.

Reports filter by UTC dates, host, event, action, outcome, and record type. Detailed activity is paginated in groups of 50. Host/event/page summaries show the top 100 groups; action/outcome summaries show the top 200. Overall totals cover every matching record.

## Extending tracking

Add `data-track-action="SOME_ACTION"` to a control for a stable semantic name. Use `beginAction` and `completeAction` from `web/lib/interaction-analytics.mjs` for explicit outcomes. Use `trackedCopy`/`trackedShare` for clipboard and native sharing. Only send fixed action/detail codes, never error messages or entered values. A `data-event-id` ancestor supplies event attribution to standalone tools.
