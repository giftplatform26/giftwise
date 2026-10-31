# Platform monetization

## Administrator setup

Open **Platform Admin → Monetization**. Both the master switch and individual streams start off. Values are seeded once in the database and edited through the admin UI; deployments do not overwrite edits. Prices are INR paise, percentages are basis points (100 = 1%). Initial gift/wallet rates are 1%; fixed alternatives ₹1. Group coordination starts at 1% with a fixed alternative ₹99. Invitation assets start at ₹99, host plans at ₹299, vendor listings at ₹999, and sponsored pricing at zero. Administrators explicitly enable collection.

A cap of zero means uncapped. For ₹500 → ₹10, ₹1,000 → ₹20, ₹5,000 → ₹75, choose 2% with a ₹75 cap. Fixed and percentage contribution fees support a guest surcharge or a host deduction. Standalone purchases have a fixed price; the group fee supports fixed pricing or a percentage of its goal. Affiliate percentage settings are estimates; actual commissions come from partner statements.

The same screen controls the free active-event limit and host/vendor/sponsor periods. Initial policy is one free published event, a 365-day host plan, and 30-day vendor/sponsor placements. All values are editable. Plan and placement renewals are manual; no automatic recurring charge is scheduled.

Price changes require a reason and an expected revision. Pricing history preserves administrator, timestamp, reason, and full before/after configuration. Commercial policy, affiliate setup, statement transitions and moderation have a separate append-only setup history. Template tier changes use the existing asset audit history.

## Contribution and wallet collection

Classic and focused checkout preview the fee, guest payment and host receipt. The server rejects different accepted amounts before provider creation. Existing orders retain their original requested amount, fee, total, host receipt and pricing revision even after the master switch is turned off. Verified captures credit gift/host accounts with the net amount and PLATFORM_REVENUE with the fee; signed ledger entries balance to zero. Idempotent retries do not post revenue twice.

Direct UPI/bank reports remain reports and never create verified revenue. Provider verification compares the provider's gross payment with the order's gross amount.

## Plans and premium invitations

Hosts open **Plans & partnerships** in their workspace. A paid host plan unlocks unlimited published events, premium gallery assets and event analytics for its configured period. Free hosts can keep drafts and existing published events. Enabling host-plan monetization restricts subsequent publishing to the configured free active-event count; it never deletes or unpublishes existing events. Publishing is serialized per host to enforce this limit under concurrency.

Administrators mark invitation gallery assets Free or Premium. Hosts can purchase a premium asset permanently or access it with an active host plan. Authenticated image endpoints enforce the entitlement; public image URLs continue to reject premium assets. The event invitation editor loads unlocked backgrounds and decorations through protected endpoints. A disabled premium-template charging switch makes these assets available without a charge to authenticated hosts. Previously purchased entitlements retain their original expiry when charging is disabled.

## Group gifting

The group creator has a coordination checkout within the group dashboard, protected by the existing page-bound group session and CSRF rules. It is one purchase per group, calculated from the server-stored goal (or reserved gift target), or the fixed group price. The creator pays this fee separately; member contributions continue to go directly to the selected creator receiving account. No member report is counted as platform fee revenue.

When group fees are enabled, new groups need verified payment or explicit zero-price activation before publishing or exposing the group contribution invitation. Already-published groups are grandfathered. After payment, publishing still requires all existing contact and receiving-account readiness checks.

## Vendors and sponsored recommendations

Hosts or advertisers with a normal platform account submit a vendor listing or sponsored recommendation from Plans & partnerships. The administrator reviews its content and destination URL before a publication period can be purchased. Listings are visible only while approved, paid/activated, unexpired and enabled by the master/stream switches. Renewals extend the original period. Administrator rejection removes a placement from public display. Public event pages show occasion-matched recommendations and vendors, labeled as sponsored or paid partner listings. Click-throughs are tracked; clicks do not create earned revenue.

## Affiliate commissions

Configure approved affiliate accounts in the admin screen with the retailer product domain, attribution parameter name/value, optional click/sub-ID parameter, and optionally a network redirect template containing `{url}` and `{click}`. Domain matches require an exact host or a real subdomain; lookalike domains do not receive attribution. Use the parameters supplied by each partner rather than assuming every retailer uses the same URL format.

Guest retailer links resolve the current published event and active public gift server-side, apply the matching enabled partner attribution, and record a click. Disabled monetization or an unmatched partner uses the original retailer URL. Public links disclose that the platform may earn a commission. Partner membership, credentials and actual commission rates must come from the retailer/network.

Record partner statements with a unique reference, amount, optional matching click ID and state. Confirmed statements can become partner-reported paid or reversed; paid statements can be reversed. Every change requires a reason and revision and preserves before/after history. Clicks are never presented as sales or verified payment receipts. Partner-reported paid totals are separate from independently verified Razorpay revenue.

## Purchase integrity and operational setup

Standalone purchases snapshot price, pricing revision, policy revision and duration. Checkout requires the reviewed amount and a buyer-scoped idempotency key. Pending orders for the same buyer/product are reused with their original price, including when a different browser submits a new key. Captured provider order/payment identity, currency and amount must match before granting access and recording revenue, atomically. Duplicate confirmations and signed webhook retries do not duplicate access or revenue. Webhooks can complete an existing purchase after charging switches are disabled. Renewals serialize the entitlement and extend its prior expiry.

The existing payment provider remains restricted to Razorpay test credentials. This implementation does not change live-mode authorization or provider enrollment. Administrators still supply approved affiliate identifiers and configure Razorpay keys/webhook delivery through the existing environment configuration. No external account is enrolled automatically.

## API overview

- `/api/admin/console/monetization`: current settings, revision-checked updates, and `/history`.
- Admin subroutes `/policy`, `/partners`, `/commissions`, `/placements`, `/setup-history`, `/revenue`: commercial setup, moderation, audited statements and separated revenue totals.
- `/api/v1/commerce`: the authenticated host's access and purchase history; `/offer`, `/checkout`, `/confirm` provide purchase flow.
- `/api/v1/commerce/templates` and `/templates/{id}/image`: catalog accessibility and protected downloads.
- `/api/v1/commerce/placements`: own listing submissions; `/analytics/{eventId}`: owned event analytics, gated by the host plan when enabled.
- `/api/public/groups/{token}/coordination/{groupId}`: creator-scoped offer, `/checkout` and `/confirm`.
- `/api/public/commerce/placements`: active public placements; `/{id}/visit` tracks eligible click-throughs.
- `/api/public/commerce/affiliate/{token}/{giftId}`: server-resolved retailer attribution redirect.

GET client requests use no-store caching. Authenticated host/admin APIs retain existing role and CSRF checks. Group purchase endpoints additionally require the existing creator session and page ID.

## Validation completed

101 distinct focused backend checks pass across calculation/quote tests, event management, gallery service/auditing, webhook verification, group sessions, commercial collection (11 integration cases), contribution monetization, Razorpay capture/order creation and gallery tier security. Commercial collection was rerun successfully after its session-security fixture was granted the pre-existing GROUP_GIFTING module permission; both missing-module and missing-session cases are checked. Fresh PostgreSQL databases successfully migrated through V83.

44 focused web checks pass, including protected premium-decoration URLs, invitation export, checkout, admin role/navigation and gallery behavior. TypeScript validation passes. The wider existing web run recorded 383 passing checks and four failing source-text assertions in batch-7a.test.mjs, guest-manual-contribution.test.mjs, and host-payment-self-confirmation.test.mjs. These assert against login/page.tsx, HostGlobalNav.tsx and GuestDirectTransferService.java, verified unchanged from HEAD at that validation point; the failures predate the monetization implementation. Later payment-control changes extend GuestDirectTransferService without changing the historical assertion target.

Restart the backend through the normal deployment workflow to apply the new Flyway migrations, and rebuild/restart the web application to expose the new screens. No live service was deployed or external account enrolled by this change.
