# Payment controls and Razorpay Route prerequisites

## Implemented in this change

Platform Admin → Payment controls provides separate direct UPI, direct bank transfer, Razorpay checkout and routing settings at platform, host and event scope. Host/event overrides can inherit, enable or disable; an explicit disable at a parent scope cannot be overridden below it. Routing also depends on Razorpay being enabled. Every update requires a reason and revision and stores immutable administrator/time/before/after history.

Direct collection methods initially preserve their previous availability; existing environment/provider/destination checks still apply. Routing starts off. Existing order confirmations and provider webhooks remain available after disabling new collection. Direct transfer reports and payment instructions are blocked server-side when their method is disabled, including group contribution instructions. These are direct-method controls: disabling direct UPI does not independently remove UPI from Razorpay's hosted payment-method selection.

The event transaction page separates the guest order amount, platform fee, host allocation, independently fetched Razorpay fee and its tax component. Provider `fee` already includes GST; `tax` is its component and is not added again. Missing fee fields remain unknown, rather than zero. Host allocation is not proof that funds were credited to the host. Confirmed host credit and Route fees remain unknown until actual transfer/settlement reconciliation exists.

Route/VPA activation is explicitly disabled at platform, host and event scope. The API rejects attempts to enable routing, and the admin UI disables that option. Migration V85 disables any previously selected routing flags with a system history entry while preserving normal Razorpay checkout settings. No transfer API is invoked and no live credential restriction is changed.

Platform Admin → Payment controls now includes **Platform routing commission (%)**, initially **1%** (100 basis points). This is an editable database setup value, with revision-checked updates and immutable before/after history recording the administrator, reason and time. It represents the platform's share of future routed host payments, not Razorpay's processing charge. Updating the rate neither enables routing nor adds a fee to normal checkout. Existing contribution-fee configuration remains independent. When routing is later implemented, select one applicable platform commission rule rather than automatically stacking this percentage with another platform contribution fee.

## Details needed to finish the Route integration

1. **Platform provider account:** Razorpay business/account identifier, confirmation that Route is activated, whether VPA Settlements is separately enabled, and which environment is approved. Existing API keys and webhook secrets belong in protected environment configuration, not chat or browser forms.
2. **Each beneficiary:** host's legal/beneficiary name, contact phone/email, individual/business category and the KYC required by Razorpay for that category. Existing account self-confirmation in this app is not equivalent to approved Route onboarding.
3. **Bank routing:** approved linked-account ID (`acc_...`), beneficiary name, account number and IFSC, and provider activation/verification status. Existing encrypted host destination data can support onboarding; provider-linked identity and verified status must be mapped to the exact host/destination.
4. **UPI routing:** approved VPA linked-account ID, UPI ID, returned holder name and recorded name-matching outcome. Razorpay currently requires live mode for VPA Linked Accounts; bank destinations are used for test-mode validation. Separate linked accounts are required for bank and VPA settlement destinations for the same host. Current documented VPA limits are ₹1,00,000 per transfer and ₹1,00,000 per beneficiary VPA per day.
5. **Host/event mapping:** whether the event inherits the host's approved destination or explicitly selects another approved destination belonging to the same host. Decide the default destination and what happens when a VPA limit is exceeded or a destination is unavailable; do not silently reroute to a different beneficiary.
6. **Charges:** actual contracted gateway pricing, Route transfer pricing, applicable taxes and rounding, and who bears these costs (platform or host). Keep the platform's own fee separate. Do not use illustrative public pricing as the actual contracted rate. Persist estimated charges separately from independently fetched actual charges and reconcile differences.
7. **Release policy:** immediate transfer after captured payment, an event-based hold, or an approval-based release. Gift-reserved funds and host-wallet funds need an explicit release policy. Decide bank settlement timing and how failed/pending payouts are retried.
8. **Exceptions:** refund and chargeback responsibility, transfer reversals, partial refunds, limits, duplicate/replayed events, reconciliation and failed-settlement operations. Subscribe to the relevant account/transfer/settlement events and independently fetch provider evidence before marking a host as credited.

## Amount breakdown

Store guest contribution and total charged, platform fee, provider gateway fee including its tax component, actual Route transfer fee/tax, host transfer amount, transfer/settlement IDs, confirmed host credit, failure/reversal state and the immutable configuration revision used.

If provider costs are borne by the host, host net is reduced by those costs; if the platform bears them, they reduce platform net revenue instead. Provider-cost allocation remains unset while routing is disabled; the configured 1% denotes the platform routing share, not an assumed provider fee. A transfer being created/processed must not be labeled a bank/VPA credit without the corresponding settlement evidence.

## Official provider references (checked 2026-10-31)

- [Linked Accounts](https://razorpay.com/docs/payments/route/linked-account)
- [VPA Settlements: activation, live-only onboarding, name matching and limits](https://razorpay.com/docs/payments/route/linked-account/vpa-settlements)
- [Transfers and fees: illustrative calculations, not contracted pricing](https://razorpay.com/docs/payments/route/transfer-fees-example)
- [Payment entity: fee includes GST, tax is a component](https://razorpay.com/docs/api/payments/entity)
- [Create transfers from orders](https://razorpay.com/docs/api/payments/route/create-transfers-orders)

## Validation

44 backend checks pass: 7 payment-control integration cases, 20 capture regressions, 4 order-creation regressions and 13 signature/confirmation checks. Twelve existing checkout/admin UI checks pass. The integration database migrated through V84 and validated successfully. Cases cover parent-scope disable precedence, inheritance reset, stale revisions, immutable history, access controls, disabled manual reports and checkout, existing-payment reconciliation after disabling collection, and actual provider fee/tax persistence without asserting host credit.

### Routing commission setup validation

The V85 setup passed 27 backend checks (10 payment-control/commission integration cases, 4 normal Razorpay order-creation cases and 13 signature/confirmation checks), 12 checkout/admin UI checks and TypeScript validation. Tests confirm a 100-basis-point initial routing commission, editable rates with immutable history, stale-edit rejection, administrator restrictions, invalid-rate rejection, disabled routing at every scope and no routing commission added to normal checkout. Apply V85 through the normal backend restart/deployment workflow; web changes require the corresponding rebuild/restart.
