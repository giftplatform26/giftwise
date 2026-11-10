# GiftWise Product Readiness Checklist

Reviewed 2026-11-05 against the working tree, including existing uncommitted work. This is a source review and implementation plan, not a claim that every screen or integration has been tested. Existing unrelated changes were retained.

## Changes implemented

Follow-up implementation details and current rollout requirements: [Branding & readiness implementation](BRANDING_AND_READINESS_IMPLEMENTATION.md).

### Initial pass

- Host registration and password reset use one password policy: 8 characters, uppercase, lowercase, number, ASCII punctuation, at most 72 UTF-8 bytes for BCrypt.
- Host username creation permits 4–30 ASCII letters, numbers and underscores, starting with a letter or number. Existing usernames/hashes are untouched; case-insensitive uniqueness remains.
- Live host signup username/password checklists and recovery password checklist show a checkmark for each satisfied requirement and textual unmet guidance.
- Host signup requires explicit legal acceptance on client and server. V106 adds timestamped, versioned consent records. Existing accounts are not assigned fabricated consent.
- Registration errors include field details and a specific duplicate-username response.
- Shared web footer, disclaimer, inactivity notice, first-visit cookie notice, GiftWise SVG favicon and linked draft privacy/terms/contact pages were added.

- Seven branded HTML/text email template pairs were added with a documented safe integration contract; delivery integration was completed in the follow-up; see the implementation guide.

## Release gates and boundaries

Apply V106 through the normal migration process before running the updated application. Do not deploy the draft legal pages as final policies: operator identity, support contact, retention, jurisdiction and route-specific refund decisions are unresolved. Current legal version identifiers refer to these draft source documents; implement immutable published-document records before production acceptance.

Follow-up: optional analytics now requires explicit opt-in; group credential creation, consent and native header branding are implemented. Offline admin provisioning retains a stronger minimum and enforces composition. Existing logins continue to use their existing credentials. A full end-to-end, accessibility, legal and security audit remains required. See [implementation guide](BRANDING_AND_READINESS_IMPLEMENTATION.md) for rollout and remaining inputs.

## Checklist

### 1. Authentication & account management

- **Priority:** Mandatory
- **Observed status:** Partial; registration, session login, email recovery and verification exist. Host signup and reset tuned in this change. Group creation policies still differ.
- **Purpose / why needed:** Let hosts, administrators and guests access the correct account safely.
- **Recommended implementation:** Reuse AuthController and recovery services. Expose forgot-password, reset and resend verification in each supported account flow; keep response wording non-enumerating for recovery. Enforce account-kind authorization and invalidate sessions on reset. Preserve all existing login credentials.
- **Sample wording:** If an eligible verified account exists, a security code will be sent.
- **UI placement:** Login, registration, verification and Account.
- **Database considerations:** Keep normalized unique identities and hashed credentials; recovery challenge expiry, attempt limits and credential epochs already have foundations. Never store plaintext passwords.
- **Admin configuration:** Configure email transport and account-kind recovery policy; provide delivery health without revealing codes.

### 2. Security requirements

- **Priority:** Mandatory
- **Observed status:** Partial; Spring Security, CSRF, recovery throttling and authorization controls exist; no penetration-test certification.
- **Purpose / why needed:** Protect accounts, financial operations and private event data.
- **Recommended implementation:** Verify authorization on every resource and mutation, CSRF for cookie sessions, secure cookies, HTTPS, CSP, rate limits, upload type/size controls and dependency scanning. Audit privileged changes. Test cross-host and guest access.
- **Sample wording:** Your session has expired. Please sign in again.
- **UI placement:** All authenticated and payment endpoints; security status in admin.
- **Database considerations:** Separate secrets from application tables; redact tokens and credentials from logs; use least-privilege database access.
- **Admin configuration:** Production configuration health checks; rotate provider keys outside UI; restrict privileged roles.

### 3. Password policies

- **Priority:** Mandatory
- **Observed status:** Host/group signup and reset aligned; offline administrator provisioning/recovery retains a stronger 16-character minimum with composition validation.
- **Purpose / why needed:** Make new credentials meet one clear policy without locking out existing users.
- **Recommended implementation:** For host signup/reset require 8 characters, ASCII uppercase, lowercase, digit and punctuation. Enforce the BCrypt 72 UTF-8 byte limit. Show each rule below the field with ✓ when met and Still needed text otherwise. Never apply creation rules to sign-in. Retain stronger administrator provisioning rules.
- **Sample wording:** Password must contain an uppercase letter.
- **UI placement:** Immediately below each new-password field; username checklist below username creation fields.
- **Database considerations:** Only persist password hashes; do not bulk migrate passwords or usernames. Broadened username CHECK retains normalized uniqueness.
- **Admin configuration:** Publish effective rules consistently; no toggle that silently weakens hashing or allows plaintext.

### 4. Field validations

- **Priority:** Mandatory
- **Observed status:** Partial; Bean Validation, service validation and database constraints exist; global input coverage needs further testing.
- **Purpose / why needed:** Prevent invalid records and provide predictable correction paths.
- **Recommended implementation:** Validate mandatory fields on client and server; validate real calendar dates and time zones, amount bounds and currency precision. Keep database unique email/username indexes. Username creation: 4–30 ASCII letters/digits/underscore, first character alphanumeric. Trim contact fields but never passwords.
- **Sample wording:** Please enter a valid email address.
- **UI placement:** Inline below the offending input plus a submission error summary.
- **Database considerations:** Use NOT NULL, uniqueness and nonnegative amount constraints; reject invalid enum values; keep normalized indexes.
- **Admin configuration:** Configure only legitimate domain limits; server remains authoritative.

### 5. Error messages

- **Priority:** Mandatory
- **Observed status:** Registration now returns actionable field and duplicate-username errors; other modules need a message audit.
- **Purpose / why needed:** Tell users how to recover while preserving private information.
- **Recommended implementation:** Return stable error codes and field paths; map provider failures to user wording; show a trace reference for support. Keep generic credential mismatch for sign-in so account existence is not exposed. Do not render raw SQL or stack traces.
- **Sample wording:** Unable to save your changes. Please try again.
- **UI placement:** Inline fields; persistent alert above submit; payment failures beside payment action.
- **Database considerations:** Record sanitized error code, correlation ID and retry classification; no payment secrets.
- **Admin configuration:** Manage localized message catalogues and inspect aggregate error rates.

### 6. Success messages

- **Priority:** Mandatory
- **Observed status:** Existing host toast and payment notification foundations; exact wording not globally standardized.
- **Purpose / why needed:** Confirm completed actions and prevent duplicate submissions.
- **Recommended implementation:** Use completed wording only after server confirmation. Preserve separate pending/manual-transfer states. Show event and gift success with meaningful next actions.
- **Sample wording:** Your event has been created successfully. / Gift added successfully. / Thank you for your contribution. / Payment completed successfully.
- **UI placement:** Toast for reversible actions; persistent confirmation page for payments.
- **Database considerations:** Derive financial success from verified provider state; use idempotency and transaction reference.
- **Admin configuration:** Localize copy; prevent admins from changing financial status through message settings.

### 7. Warning messages

- **Priority:** Mandatory
- **Observed status:** Partial; destructive-action wording needs screen-by-screen review.
- **Purpose / why needed:** Explain consequences before users commit destructive actions.
- **Recommended implementation:** Name the resource and affected data; distinguish removing display content from cancelling transactions. Disable repeated submissions.
- **Sample wording:** Are you sure you want to delete this gift? This action cannot be undone.
- **UI placement:** Before gift deletion, event deletion, refunds and account termination.
- **Database considerations:** Maintain audit trail and transaction retention; define soft-delete and cascade behavior.
- **Admin configuration:** Control retention rules and role permissions; warnings cannot bypass authorization.

### 8. Confirmation dialogs

- **Priority:** Mandatory
- **Observed status:** Coverage not verified across all destructive routes.
- **Purpose / why needed:** Give a deliberate final decision for consequential actions.
- **Recommended implementation:** Use accessible modal with heading, description, Cancel and explicit action. Focus the safe action, trap focus, support Escape and restore focus. Server must recheck state before mutation.
- **Sample wording:** Deleting this event will remove all associated gifting information. Continue?
- **UI placement:** Delete event/gift, irreversible changes and financially material actions.
- **Database considerations:** Persist the actual mutation audit, not just dialog display; use optimistic locking where supported.
- **Admin configuration:** No override allowing confirmation to replace required security checks.

### 9. Footer requirements

- **Priority:** Mandatory
- **Observed status:** Global web footer added here; mobile and email remain separate surfaces.
- **Purpose / why needed:** Make legal and support information discoverable everywhere.
- **Recommended implementation:** Render the shared footer in root layout for login, signup, admin, guest and payment pages. Verify narrow screens and embedded flows. Keep contact information configurable once branding management exists.
- **Sample wording:** © 2026 GiftWise. All rights reserved. | Privacy Policy | Terms & Conditions | Contact Us
- **UI placement:** Bottom of every web page, without covering checkout controls.
- **Database considerations:** Future branding configuration stores versioned footer and support metadata; no sensitive values.
- **Admin configuration:** Platform-level footer text, verified support email and safe social URLs.

### 10. Header requirements

- **Priority:** Mandatory
- **Observed status:** HostGlobalNav exists; branding and cross-surface consistency need review.
- **Purpose / why needed:** Provide consistent orientation and account actions.
- **Recommended implementation:** Keep logo and accessible home link, active navigation, profile and sign-out. Guest header should show event identity without exposing host-only links. Preserve keyboard access and mobile menu focus.
- **Sample wording:** GiftWise / My account / Sign out
- **UI placement:** Top of dashboard, event, guest and admin screens.
- **Database considerations:** Resolve role and brand from trusted server context.
- **Admin configuration:** Configure brand assets and allowed navigation features by role.

### 11. Branding standards

- **Priority:** Recommended
- **Observed status:** Platform-admin branding configuration, safe uploads, revision checks, public resolution and cross-session updates implemented.
- **Purpose / why needed:** Support consistent identity and later white-label operation.
- **Recommended implementation:** Create platform branding settings with app name, logo, favicon, color, footer, support email and social links. Use public read-only resolved brand API and admin-only versioned updates. Validate contrast and asset safety.
- **Sample wording:** GiftWise — Celebrate meaningful moments.
- **UI placement:** Shared shell, authentication, checkout, mobile and transactional email.
- **Database considerations:** platform_brands with tenant scope, asset IDs, color tokens, active version and updated_by; cache by tenant/version.
- **Admin configuration:** Preview, upload, save, rollback; audit every change; enforce safe URL schemes.

### 12. Logo placement

- **Priority:** Mandatory
- **Observed status:** Shared uploaded logo used by web root, authentication, admin, host workspace, guest view, native header and configured branded emails.
- **Purpose / why needed:** Make users recognize the same platform across touchpoints.
- **Recommended implementation:** Use one BrandLogo component and shared email asset. Include meaningful alt text and fixed dimensions. Show logo on login, signup, dashboard, guest, checkout, landing and mobile screens.
- **Sample wording:** GiftWise
- **UI placement:** Header and authentication card; email header and app launch assets.
- **Database considerations:** Store approved asset reference and dimensions; serve stable public HTTPS email asset.
- **Admin configuration:** Upload with MIME, dimensions, size and malware checks; preview desktop/mobile and email.

### 13. Favicon requirements

- **Priority:** Mandatory
- **Observed status:** GiftWise SVG icon and root metadata added here; installed-app icon set still pending.
- **Purpose / why needed:** Identify the app in tabs and bookmarks.
- **Recommended implementation:** Serve GiftWise icon; provide PNG/ICO fallback, Apple touch icon and manifest variants in the production asset pipeline. Verify browser cache refresh.
- **Sample wording:** GiftWise
- **UI placement:** Browser tab, pinned icon and app manifest.
- **Database considerations:** Store favicon asset ID in future brand settings; version URLs after updates.
- **Admin configuration:** Favicon upload with square-image validation and preview.

### 14. Privacy policy requirements

- **Priority:** Mandatory
- **Observed status:** Draft /privacy page added; operator-specific details still require completion.
- **Purpose / why needed:** Explain what happens to personal data and how to request help.
- **Recommended implementation:** List data categories, purposes, recipients, payment handling, cookies, analytics, guest visibility, retention, rights and contact. Match actual processing and geography. Publish a reviewed immutable version before launch.
- **Sample wording:** You can request help with access, correction or deletion through our published privacy contact.
- **UI placement:** Footer, signup consent and collection points for optional sensitive data.
- **Database considerations:** Consent table added for new host signup; add published legal documents with locale, version, content hash and effective date.
- **Admin configuration:** Authorized legal editor, review/publish workflow, operator identity and privacy contact.

### 15. Terms & conditions requirements

- **Priority:** Mandatory
- **Observed status:** Draft /terms page added; refund and jurisdiction decisions remain unresolved.
- **Purpose / why needed:** Define responsibilities and commercial expectations.
- **Recommended implementation:** Define platform role, user responsibilities, payment routing, refunds/cancellation, fees, third-party limitations, suspension, termination and dispute contact. Do not promise refunds the integrations cannot execute.
- **Sample wording:** Cancelling or deleting an event does not automatically reverse a payment.
- **UI placement:** Signup checkbox, footer and payment disclosure link.
- **Database considerations:** Record exact accepted version and timestamp; retain consent with account lifecycle rules.
- **Admin configuration:** Review and publish legal versions; require new acceptance for applicable material updates.

### 16. Cookie consent requirements

- **Priority:** Mandatory
- **Observed status:** Versioned opt-in/reject/withdrawal implemented; browser tracking and backend analytics recording require consent.
- **Purpose / why needed:** Explain storage and control optional tracking where applicable.
- **Recommended implementation:** Inventory cookies and browser storage. Keep essential sessions operational. Add Accept optional, Reject optional and preferences controls before enabling optional analytics when consent is required. Ensure telemetry does not send before that decision; support withdrawal.
- **Sample wording:** We use cookies to improve your experience. [Accept] [Learn More]
- **UI placement:** First visit; persistent preferences link in footer.
- **Database considerations:** Version purpose-specific preferences and expiry; do not equate a UI acknowledgement with legal consent.
- **Admin configuration:** List vendors, purposes and lifetimes; enable categories only after implementation and review.

### 17. Accessibility requirements

- **Priority:** Mandatory
- **Observed status:** New credential guidance uses described-by associations and text symbols; no full accessibility audit completed.
- **Purpose / why needed:** Allow keyboard, screen-reader and low-vision users to complete tasks.
- **Recommended implementation:** Target WCAG 2.2 AA. Check semantic labels, errors, visible focus, contrast, dialogs, status announcements, reduced motion, touch target sizes and non-color cues. Test keyboard and a screen reader on auth and payment flows.
- **Sample wording:** Requirement met / Still needed
- **UI placement:** Every interactive component; validation feedback adjacent to inputs.
- **Database considerations:** Store text alternatives and localized labels for uploaded content where appropriate.
- **Admin configuration:** Validate brand contrast; require meaningful media descriptions.

### 18. Multi-language readiness

- **Priority:** Recommended
- **Observed status:** LocalizationService, platform_languages, provider and localization admin already exist.
- **Purpose / why needed:** Let users understand invitations and critical account/payment text.
- **Recommended implementation:** Reuse existing catalogues and fallback rules. Extract new strings, translate legal pages and transactional messages, support RTL and locale-sensitive numbers/dates. Do not translate identifiers or payment references.
- **Sample wording:** Preferred language
- **UI placement:** Account settings, guest language switcher and localization admin.
- **Database considerations:** Persist account/event preference and notification language; version legal acceptance per locale.
- **Admin configuration:** Translation review, missing-key report, supported-language enablement and fallback configuration.

### 19. Mobile responsiveness

- **Priority:** Mandatory
- **Observed status:** Next.js frontend and mobile project present; no complete device review performed.
- **Purpose / why needed:** Make gifting and payments usable on phones.
- **Recommended implementation:** Verify 320px through desktop, long translated text, safe areas, virtual keyboards, upload controls, dialogs and payment redirect return. Avoid fixed footers covering primary actions.
- **Sample wording:** Processing Payment…
- **UI placement:** All screens, especially guest checkout and authentication.
- **Database considerations:** Keep APIs device-independent with secure client session/token contracts.
- **Admin configuration:** Brand previews and device smoke-test report; no client-only permission enforcement.

### 20. Admin configuration screens

- **Priority:** Recommended
- **Observed status:** Existing controls retained; Branding & appearance and email delivery status added.
- **Purpose / why needed:** Let authorized operators manage the platform without code edits.
- **Recommended implementation:** Extend existing admin console with branding and policy publication. Keep secrets in secret management. Add change history and scoped permissions. Show configuration readiness rather than silently accepting incomplete settings.
- **Sample wording:** Support email must be configured before publishing.
- **UI placement:** Platform Admin > Branding / Legal / Notifications / Policies.
- **Database considerations:** Tenant-aware versioned settings; optimistic locking, updated_by and audit snapshots.
- **Admin configuration:** Platform-admin authorization, preview, validation, save, rollback and publish history.

### 21. Notification templates

- **Priority:** Recommended
- **Observed status:** Host inbox, thank-you service and outbox foundations already exist.
- **Purpose / why needed:** Provide timely, correct event and payment updates.
- **Recommended implementation:** Reuse outbox with idempotent delivery, retry/backoff and dead-letter visibility. Separate payment pending, captured and refunded messages; respect preferences for optional notifications.
- **Sample wording:** Contribution received. View your event contributions.
- **UI placement:** Notification bell, event history and user preferences.
- **Database considerations:** Persist template/version/locale, recipient, channel, source ID and delivery status; deduplicate source events.
- **Admin configuration:** Template review, safe variables, preview, test delivery, retry and failure dashboard.

### 22. Email templates

- **Priority:** Mandatory
- **Observed status:** Seven branded HTML/text pairs, safe renderer, configured branded security transport and durable transactional delivery implemented. Provider/origin configuration and real delivery verification remain required.
- **Purpose / why needed:** Build trust and provide useful next steps in email.
- **Recommended implementation:** Use a shared escaped HTML layout plus text alternative. Include logo, support, privacy, terms and copyright. Create registration, verification, password reset, invitation, contribution received, payment success and event completed templates. Link only to configured trusted public origins.
- **Sample wording:** Verify your GiftWise email / Reset your GiftWise password / You are invited / Payment completed successfully.
- **UI placement:** Email header, main action, support/legal footer; readable on narrow clients.
- **Database considerations:** Template key/version/locale and outbox linkage; never log tokens or include passwords.
- **Admin configuration:** Approved sender/domain, verified support email, branded preview, test recipient and delivery monitoring.

### 23. Empty state messages

- **Priority:** Recommended
- **Observed status:** Many feature components already render states; exact coverage remains unverified.
- **Purpose / why needed:** Explain why a section is empty and provide an appropriate next step.
- **Recommended implementation:** Differentiate no data, no permission, filtered results and load failure. Show host add/invite actions only when authorized.
- **Sample wording:** No gifts added yet. Start by adding your first gift. / No guests invited yet.
- **UI placement:** Gift lists, guest lists, notification inbox, analytics and admin tables.
- **Database considerations:** No placeholder database records required; return explicit empty collections and pagination totals.
- **Admin configuration:** Localized copy; permission-aware primary action.

### 24. Loading states

- **Priority:** Mandatory
- **Observed status:** Existing loading.tsx and busy buttons found; broad coverage needs review.
- **Purpose / why needed:** Prevent blank screens and accidental duplicate actions.
- **Recommended implementation:** Use skeletons or status text, aria-busy and disabled submit buttons. Preserve form content on failure. Provide bounded retries; never automatically repeat non-idempotent payment or creation requests.
- **Sample wording:** Creating Event… / Loading Gifts… / Processing Payment…
- **UI placement:** Within the region being loaded and next to submitted action.
- **Database considerations:** Idempotency keys and operation status for long jobs; backend timeouts.
- **Admin configuration:** Observe slow operations and provider timeouts; configure safe worker retries.

### 25. Audit logging

- **Priority:** Mandatory
- **Observed status:** Business audit feed added; user create/update and verified payment/contribution records supplement existing event/gift audit. Migration/integration verification remains required.
- **Purpose / why needed:** Make important changes traceable for support and investigation.
- **Recommended implementation:** Record user created/updated, gift added/edited, contribution received and payment processed after authoritative transaction completion. Cover admin branding/legal edits. Restrict access and protect append-only records.
- **Sample wording:** Gift edited by host at [time].
- **UI placement:** Admin > Audit with actor, event, action, date and correlation filters.
- **Database considerations:** Use existing audit tables; add stable domain event type, actor, target, redacted change details and source ID; never store credential hashes in snapshots.
- **Admin configuration:** Retention, export authorization, pagination and privacy redaction.

### 26. Analytics

- **Priority:** Recommended
- **Observed status:** Interaction tracking and AdminInteractions already exist.
- **Purpose / why needed:** Understand conversion and failures without collecting private form content.
- **Recommended implementation:** Reuse sanitized events; distinguish funnel attempts from authoritative payment captures. Add event creation, invitation open, gift selection and checkout funnel. Apply optional consent policy and avoid invitation token leakage.
- **Sample wording:** Checkout completion rate
- **UI placement:** Admin analytics; scoped host summary.
- **Database considerations:** Existing interaction analytics schema; retention and tenant-scoped aggregates.
- **Admin configuration:** Role-based dashboards, date filters, retention and analytics enablement.

### 27. SEO requirements

- **Priority:** Recommended
- **Observed status:** Dynamic branded title, private-route noindex metadata and robots exclusions added. Marketing sitemap remains outside this private application.
- **Purpose / why needed:** Make public marketing pages discoverable while protecting private events.
- **Recommended implementation:** Use route-specific metadata, canonical public URLs, OG assets and sitemap only for public marketing pages. Set noindex for auth, account, admin, tokenized invitations and checkout. Do not expose guest lists in structured data.
- **Sample wording:** GiftWise | Celebrate meaningful moments
- **UI placement:** Page metadata, robots rules and share previews.
- **Database considerations:** Store public event metadata separately from private guest and payment details.
- **Admin configuration:** Approve public titles, descriptions, social images and indexing policy.

### 28. Performance requirements

- **Priority:** Recommended
- **Observed status:** Performance diagnostics and production-readiness components exist; no load benchmark run.
- **Purpose / why needed:** Keep invitation and checkout flows responsive under realistic load.
- **Recommended implementation:** Measure p75 Core Web Vitals, API latency, error rates and checkout time. Size images, lazy load editors, paginate lists, index lookup paths and cache public configuration safely. Exercise representative traffic and webhook bursts.
- **Sample wording:** Loading Gifts…
- **UI placement:** Loading regions and admin operational dashboards.
- **Database considerations:** Review query plans, pool bounds, indexes, outbox backlog and retention.
- **Admin configuration:** Define SLOs and alerts; set asset size budgets and provider timeout limits.

### 29. Future scalability considerations

- **Priority:** Nice to Have
- **Observed status:** Localization/mobile/monetization foundations retained; V105 adds disabled schema foundations for tenants, currencies, coupons, referrals, subscriptions and gift recommendations. Current payment ledgers remain INR-only; commercial activation and tenant isolation are not claimed.
- **Purpose / why needed:** Avoid architectural dead ends while implementing current features.
- **Recommended implementation:** Keep API contracts versioned. Add tenant scoping before white-label isolation. For currencies use currency-specific minor units and immutable transaction currency; do not merely remove INR checks. Plan coupons, referrals, subscriptions and recommendations around explicit bounded domains.
- **Sample wording:** Currency: INR
- **UI placement:** Admin feature configuration, checkout currency and future subscription screens.
- **Database considerations:** Design tenant/brand IDs, ISO currency+minor-unit amounts, coupon redemption uniqueness, referral attribution, subscription entitlement periods and consented recommendation inputs. Do not add unused placeholder columns to every table.
- **Admin configuration:** Feature gates, provider capability matrix, migration rehearsals and tenant isolation tests.

### 30. Legal & compliance considerations

- **Priority:** Mandatory
- **Observed status:** Draft disclosures and consent persistence added; no compliance certification implied.
- **Purpose / why needed:** Match the product promises to actual payment and data operations.
- **Recommended implementation:** Confirm operator identity, service regions, age rules, privacy contact, retention, payment obligations and refund process. Review applicable effective dates and jurisdiction-specific requirements. Verify the payment provider agreement and consumer-facing fee disclosure. Complete draft policies before production.
- **Sample wording:** GiftWise facilitates gifting between hosts and guests.
- **UI placement:** Privacy/terms/contact, signup, guest contribution and checkout review.
- **Database considerations:** Record legal versions and auditable consent; implement rights requests and retention exceptions with accountable ownership.
- **Admin configuration:** Named policy owner, review dates, publication history and operational support process.

## GiftWise-specific acceptance criteria

### Event-specific forms

Reuse occasion types and celebration planning settings. Render only applicable sections while retaining safe server validation. Require a title, valid real date when supplied, address/contact fields appropriate to the event and permission-aware gift settings. Count characters against the actual configured server limit: current host API description limit is 10,000, so a UI must not claim 500 unless both contracts change. Validate start/end ordering and explicit event timezone. Empty optional fields must remain optional. Test each occasion, save/reopen and publish-readiness checks.

### Guest privacy and contribution anonymity

Reuse group privacy settings (`joinPolicy`, `memberVisibility`, `amountVisibility`) and confirm enforcement in every API response, export and notification. Provide separate controls for public display name, public amount and host/admin access. Sample: “Hide my name from other guests. Your host and payment provider may still see transaction details.” Do not promise absolute anonymity. Invitation tokens must not appear in logs or analytics payloads. Explain link sharing before publication.

### Payment disclosure and cancellation

Before payment show recipient identity, selected route, amount, currency, platform/provider charges if applicable, total, status expectations and refund/cancellation link. A manually declared transfer must read “Transfer details submitted; awaiting verification.” Display “Payment completed successfully” only after authoritative confirmation. Event deletion must not silently delete ledger records or imply a refund. Reuse existing destination verification and reconciliation services.

### Host branding controls

Keep host event branding separate from platform identity and legal ownership. Allow approved event logo/image/color choices within contrast and content constraints. Do not allow host text to replace payment-provider disclosures or platform support/legal links. Persist event brand overrides with asset references and audit updates; resolve platform defaults first. Platform admin must be able to revoke unsafe assets.

### Email specification

All templates need escaped variables, accessible branded header, meaningful action, text alternative, support link, privacy/terms links and © 2026 GiftWise. Use trusted HTTPS origins for actions; no user-provided arbitrary action URLs.

| Trigger | Subject | Body / primary action | Delivery rule |
|---|---|---|---|
| Registration committed | Welcome to GiftWise | Your account has been created. Sign in to start your first event. | One per account; no plaintext credentials. |
| Verification requested | Verify your GiftWise email | Use this security code to verify your email. If you did not request it, ignore this email. | Include actual configured expiry; rate limited; never log code. |
| Password reset requested | Reset your GiftWise password | Use this security code to reset your password. | Same non-enumerating request response for eligible/ineligible accounts. |
| Event invitation | You are invited to {{eventName}} | {{hostName}} invited you. View invitation. | Authorized invitation only; avoid unnecessary guest disclosure. |
| Verified contribution | Contribution received for {{eventName}} | Thank you for your contribution. View details. | Authoritative receipt only; preserve anonymity settings. |
| Verified captured payment | Your GiftWise payment confirmation | Payment completed successfully. Reference: {{reference}}. | Idempotent source event; amount/currency and recipient included. |
| Event completed | {{eventName}} has completed | Thank you for celebrating with us. View your event. | Trigger once from explicit completion state; no guessed date-only completion. |

### Profile implementation and verification

Implemented safe profile picture upload, IANA time zone and optional email notification preferences alongside the existing name/language/mobile/email controls. Reuse these preferences for any later delivery channels. Do not allow disabling essential security notices. Validate image MIME/size, timezone against supported identifiers and locale against enabled platform languages. Persist preferences by user ID; account changes require authorization and sensitive-contact verification. Test read/save/reload and mobile parity.

## Suggested implementation sequence

1. Complete migration and focused regression validation for this change.
2. Complete operator branding/support and publish reviewed legal documents; add cookie preference enforcement before optional tracking.
3. Align all group credential creation screens and backend rules together, preserving existing login compatibility. Add mandatory group consent records and mobile equivalents.
4. Complete profile preferences, centralized branding and seven wired email templates using existing notification delivery infrastructure.
5. Audit event/gift/guest/payment UI wording, real-date and amount validation, confirmations, counters, empty/loading states and privacy enforcement.
6. Run end-to-end host/guest/admin/mobile journeys, database integration tests, accessibility checks and payment sandbox reconciliation tests.
7. Implement future multi-currency/tenant features only with complete ledger/provider/authorization contracts and migration tests.

## Source evidence

- Authentication/credentials: `src/main/java/com/giftplatform/security/{AuthController,CredentialPolicy}.java`, `security/recovery/EmailRecoveryController.java`, `user/{dto/RegisterRequest,service/RegistrationServiceImpl,entity/User}.java`.
- Groups: `src/main/java/com/giftplatform/group/GroupContributionController.java` (existing credential and privacy settings).
- Localization: `src/main/java/com/giftplatform/localization/`, migration V97, `../web/components/LocalizationProvider.tsx`.
- Notifications: `notification/HostThankYouService.java`, `ThankYouMessageRenderer.java`, `security/delivery/EmailDeliveryConfiguration.java`, notification outbox migrations.
- Audit: `audit/`, `admin/PlatformAdminAuditController.java`, `../web/app/admin/AdminAudit.tsx`.
- Analytics: `analytics/`, V99, `../web/lib/interaction-analytics.mjs`, `../web/app/admin/AdminInteractions.tsx`.
- Currency limitations: V17 `ck_payment_order_currency`, V33 payout currency CHECK, V83 commercial currency CHECK.
- UI shell: `../web/app/layout.tsx`, `components/{CredentialRequirements,LegalFooter,CookieNotice}.tsx`.
- New database change: `src/main/resources/db/migration/V106__credential_policy_and_legal_consent.sql`.

## External reference points

Accessible field feedback should associate errors/instructions with inputs and provide understandable notifications: [W3C WAI form notifications](https://www.w3.org/WAI/tutorials/forms/notifications/).

For an India-facing deployment, confirm applicable obligations and commencement dates against the official [MeitY Digital Personal Data Protection Rules, 2025](https://www.meity.gov.in/static/uploads/2025/11/53450e6e5dc0bfa85ebd78686cadad39.pdf). This checklist does not determine jurisdiction or certify legal compliance.

## Verification results for this pass

- Frontend full suite: 423 passed; additional focused rerun after final consent changes: 27 passed.
- Frontend and native mobile TypeScript: `tsc --noEmit` passed. Next.js production build passed.
- Backend targeted regression suite: 90 tests passed, including branding permissions, image decoding, all email templates, retry idempotency, consent, group registration/CSRF and account recovery. Tests used headless AWT and an explicit Mockito Java agent.
- Follow-up: Docker Desktop started; all 106 migrations applied and 63 targeted PostgreSQL tests passed, including the 18 registration integration tests. See [testing guide](TESTING.md).
- Native mobile suite: 14 tests passed.
- No live browser, mobile-device, email delivery or payment sandbox journey was executed.
