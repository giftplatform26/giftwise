# GiftWise branding and readiness implementation

## Using the new features

1. Sign in as a platform administrator at `/login` and open `/admin`.
2. Choose **Branding & appearance**. Set application name, color, footer, support email and social links, then save.
3. Upload the platform logo and optional favicon (PNG or JPEG, up to 2 MB). Save text edits before uploading an image. Reset restores the shared fallback image.
4. The current page updates immediately. Other tabs receive a refresh message; other active browser/native sessions refresh within 15 seconds and when brought to the foreground. Native installed launcher icons and OS application names are build-time assets and require a new signed build.
5. Account > Profile picture & preferences manages picture, time zone and optional email preferences. Existing name, language, mobile and email verification controls are retained.
6. Events > Notifications includes **Email an invitation** for a published event. This is an explicit send action, limited to 100 queued invitations per event per hour. Repeating a failed request with its request ID does not create another email.
7. Admin > Activity & audit includes account, event, gift, contribution and payment activity, plus the existing administrative audit.

## Credential behavior

Host and group credential creation use the same rules: usernames are 4–30 ASCII letters, numbers and underscores, with an alphanumeric first character; passwords contain at least 8 Unicode code points, an ASCII uppercase/lowercase letter, number and punctuation, within 72 UTF-8 bytes for BCrypt. Group usernames remain stored lowercase as required by their existing schema. Existing sign-in flows do not apply the new creation rules. Existing stored names, usernames and password hashes are untouched.

Host, group creator and group member registration show green checks / red crosses, text for each rule and a screen-reader status summary. Password reset uses the same component. Terms/privacy acceptance is mandatory on all three registration flows and recorded with a timestamp and document version. Existing accounts are not assigned fabricated acceptance. Offline administrator provisioning/recovery retains its stronger 16-character minimum and now also validates composition and the BCrypt byte limit.

## Database rollout

Run normal Flyway migrations before starting the updated application:

- V103: platform branding/assets, profile preferences and group legal consent.
- V104: transactional email outbox and immutable rendered retry content.
- V105: disabled data foundations for tenants/branding/memberships, currency metadata, coupons/redemptions, referrals, subscription plans/billing snapshots and gift recommendations.
- V106: host legal consent and broadened compatible username constraint. Version 102 belongs to the existing premium-stationery Java migration; it is preserved.

Logo images are bounded, decoded and re-encoded to PNG; SVG/active content is rejected. Dimensions are checked before allocation, metadata is discarded, and output is capped at 1024 pixels per edge. Image blobs are stored in PostgreSQL. Public immutable UUID URLs support login screens and email; profile pictures require the signed-in owner's session and are not public. Branding settings are public, while all branding writes require a platform-admin role and CSRF. Settings updates use revision checks; administrative changes are audited. Old public branding images remain available for previously delivered emails; define an asset retention policy before automatic cleanup.

## Email setup and operation

Use the existing Resend configuration:

- `GIFT_OTP_EMAIL_PROVIDER=resend`
- `GIFT_OTP_EMAIL_RESEND_API_KEY` from the secret manager
- `GIFT_OTP_EMAIL_FROM` with a verified sender
- `GIFT_PUBLIC_BASE_URL=https://your-web-origin` (or the existing production `GIFT_PRODUCTION_PUBLIC_ORIGIN`)

Do not include an origin path, query or credentials. Set the real support email in Branding & appearance. The admin screen reports whether branded delivery is configured; it does not expose secrets. Existing security delivery retains its prior text behavior if the new canonical origin has not been configured. Once configured, verification/reset/reminder messages use the current branded HTML layout and plain-text alternative.

Registration, explicit event invitations, verified payment receipts, host contribution receipts and host event-completion messages use a durable outbox. No external email is sent in the registration/payment database transaction. The worker starts after 60 seconds and polls every 30 seconds, processing up to 10 messages per pass. Optional host event/contribution emails respect profile preferences; essential payment receipts and security messages remain enabled. Marketing preference is persisted for future campaigns; no marketing sender was added.

Provider calls use stable idempotency keys and persisted rendered content for retries. Retry attempts stop after six claims or 20 hours from queue creation, within the provider's documented [24-hour idempotency window](https://resend.com/docs/dashboard/emails/idempotency-keys). Abandoned leases recover after five minutes. Failure records expose sanitized error codes, not provider bodies or secrets. `SENT` means provider acceptance, not confirmed inbox delivery; bounce/delivery webhook integration is separate. Configure retention for recipient addresses and rendered transactional messages. Security codes are never written to this outbox.

All seven HTML/text template pairs are under `src/main/resources/email-templates`. The default bodies are English; existing security messages retain localization. Full translations of the new transactional bodies still need to be published through the localization process. Current branding applies when a message is first rendered; retries and already delivered messages preserve their original content.

## Inactivity enforcement

Signed-in host/admin sessions track actual user interactions separately from HTTP polling. Notification refreshes do not extend the configured server idle deadline (30 minutes in the production profile). Group creator and member sessions use the existing 10-minute group idle duration, scoped to each group account. Expired sessions cannot be revived by a late activity signal. Existing credentials remain usable after signing in again.

## Consent, privacy and payment UI

Optional browser analytics stays disabled until an explicit versioned opt-in. Reject and withdrawal are available in the cookie banner and footer. Withdrawal drops unsent events. Analytics collection/filter routes require the versioned consent header; absence does not create an analytics session. Necessary application authentication, financial records and business audit are independent of optional analytics consent. An already transmitted request cannot be recalled.

Payment checkout now links to payment/refund terms and privacy disclosures in both application modes. Manual transfer reports keep pending wording; payment success remains based on verified provider state. Guest-name visibility continues to follow existing event/group controls. A separate public contributor-name display and per-contribution anonymity feature were not added; current group membership visibility rules remain authoritative.

Private application routes use noindex metadata and robots exclusions. Event descriptions have counters against their actual server limits, and calendar validation rejects impossible dates such as 30 February.

## Remaining operator inputs and release verification

Privacy/terms are still clearly marked drafts. Final business identity, canonical origin, support/privacy contact, retention and refund/cancellation rules must come from the operator; no business policies were invented. These inputs were requested during implementation. Draft legal acceptance must not be represented as a production legal review.

Source/unit checks and web/mobile compilation do not replace PostgreSQL migration and end-to-end tests. Docker was unavailable in the initial implementation pass. In the follow-up, Docker Desktop was started and all 106 migrations plus 63 targeted PostgreSQL tests passed; see `docs/TESTING.md`. Before production, verify migration execution, concurrent admin updates, actual logo persistence/propagation, email-provider sandbox delivery, consent withdrawal and host/guest payment journeys. No production migration, live email, payment or deployment was performed.

V105 supplies the requested future-ready database structures for multi-currency metadata, white-label tenants, promotions, referrals, subscription snapshots and gift recommendation records. These features start disabled and have no active commercial APIs. Multi-currency payment ledgers/providers, redemption locking/limits, referral qualification, subscription webhooks, recommendation generation and tenant authorization still require implementation before activation. Existing INR ledger constraints were preserved; changing global branding does not claim tenant isolation or activate billing.

## Verification in this session

- 90 targeted backend regression tests passed.
- 423 frontend tests passed; the final focused validation/branding/consent set also passed (27 tests).
- Next.js production build and web TypeScript checks passed.
- Native mobile TypeScript checks and 14 native project tests passed.
- `git diff --check` passed.
- Follow-up: all 106 migrations and 63 targeted PostgreSQL tests passed after the Docker fix. Actual provider delivery and the complete integration suite remain separate checks.
