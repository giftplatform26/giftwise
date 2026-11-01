Gift contribution options (V90)
==============================

Backend changes build on Event's existing host delivery address and invitation-scoped creator/member sessions. Apply Flyway V90 before using these APIs. Amounts are integer paise. Existing CSRF protection, invitation feature permissions, and group-specific session checks apply.

Individual guests
-----------------

POST `/api/public/groups/{token}/guest-gifts/individual` with:

```json
{"name":"Coffee maker","description":"For your new kitchen","productUrl":"https://example.com/coffee","guestName":"Alex","email":"alex@example.com","mobile":"+919876543210","contributionPaise":500000}
```

A guest logs in using the existing invitation account flow; creating a group is unnecessary. Contribution amount is optional for direct purchases. The gift is immediately selected as INDIVIDUAL and shown as a planned gift. Public gift responses omit contact details, amounts and tracking. The owner-only host report includes them before publication. Existing add/select endpoints remain supported.

POST `.../guest-gifts/{id}/delivery` with `{ "status":"GIFT_PURCHASED" }`, then `{ "status":"TRACKING_SHARED", "trackingDetails":"Carrier and reference" }`. Supported states are PLANNED, GIFT_PURCHASED, TRACKING_SHARED, COMPLETED. Only the selecting guest may change delivery details. Read `.../guest-gifts/{id}/delivery-address` for the host address. Private `.../guest-gifts/mine` includes contact/amount/tracking fields only for the selecting guest, including when another guest originally added the gift.

Group gifts
-----------

Existing group setup, payment instructions, guest contributions without registration, and member login remain available. Existing `/publish/{groupId}` opens the group invitation; it does not publish a completed gift.

* GET `/api/public/groups/{token}/gift-board`: group gifts show Something is Cooking 🍳 until gift publication; gift name/details/link stay hidden until then. Public responses never include funding totals or contributor details.
* GET `.../gift-progress/{groupId}`: approved group member session required; returns target, collected, reported amounts and percentage. Creators also retain dashboard summary access.
* POST `.../gift-receipts/{groupId}/{reportId}`: creator acknowledges receipt of a reported manual transfer. It counts toward gift progress, but the report remains REPORTED_UNVERIFIED and creates no verified ledger posting. Repeating acknowledgement does not count the amount twice. Receipts marked VERIFIED by trusted reconciliation also count. Unacknowledged self-reports do not count toward collection.
* POST `.../gift-publish/{groupId}`: creator explicitly publishes the gift after its target is reached and a gift name is set. Public and host display status becomes 100% Contributed. Published wishlist plans cannot be selected/released again.
* GET `.../gift-delivery-address/{groupId}`: creator-only host delivery address.
* POST `.../gift-fulfilment/{groupId}`: `{ "status":"GIFT_PURCHASED" }` or `{ "status":"AMOUNT_TRANSFERRED_TO_HOST" }` after full funding. A purchase may advance to TRACKING_SHARED with `trackingDetails`. Either path may advance to COMPLETED. Completed gifts cannot switch paths.

Host `/api/v1/events/{eventId}/contributions/groups` now includes draft groups, target, collected amount, percentage, reported totals, creator contact, funding status, publication status, fulfilment and tracking. Its transaction and activity endpoints are available for draft groups too. These changes supersede the earlier documentation saying host reports contain only published gifts/groups.

This implements backend APIs. The web application still needs forms and controls wired to the new individual, receipt, publication and fulfilment endpoints. Status updates record actions reported by the guest/creator; they do not initiate bank transfers, purchases or shipping.

Individual-first popup (V91)
---------------------------
The invitation action now reads “Add a Special Gift” and first offers Individual Gift or Group Gift. Only the group route opens group registration/sign-in. Individuals complete name, email, mobile, gift details and giving method directly, without registering. The two methods are LINK_AND_CONTRIBUTION (positive contribution amount required) and DIRECT_DELIVERY (product link required; amount omitted).

The new POST `/api/public/groups/{token}/guest-gifts/individual-entry` accepts those details plus optional `sourceGiftId` for selecting an available guest gift. A private browser session grants access to that guest's saved entries using GET on the same URL, and to `/{id}/delivery-address` and POST `/{id}/delivery`. Keep the browser session to return and share tracking. Group login and sessions remain separate. Internal attribution records cannot be signed into because their credentials are random and undisclosed.

V91 persists the chosen giving method. The web popup and host guest-gift report are wired to these endpoints; this supersedes the earlier note that frontend wiring is pending for individual gifts. Group receipt/publication/fulfilment controls from V90 still need separate frontend wiring.

Host contributions for individual gifts (V92)
--------------------------------------------
The link-and-contribution flow now asks for ₹1–₹10,00,000 and continues to the host's existing eligible UPI/QR or bank destination. Guests transfer the saved amount to that account and submit a transaction reference. GET/POST `.../individual-entry/{id}/host-contribution` are restricted to the owning browser session. POST accepts only `destinationId`, `method` (UPI/BANK), and `transactionReference`; amount and contact details come from the saved gift. The existing direct-transfer service enforces invitation, destination, channel, duplicate-reference and idempotency rules.

V92 links this unverified host transfer claim to the special gift. Repeated submissions use one server-generated key per gift and private session owner. Host gift reports include the claim ID, method and verification status; the regular host payment report also contains the manual claim. This is a direct transfer to the host, with no wishlist allocation or verified financial posting. Host payment channels and an eligible preferred event-linked account must already be enabled.

Guest cards display “Presented with love ♡” for individual gifts, and “A gift of love from a group ♡” for group selections, without revealing contact details, payment references or amounts.

Independent group collections (V93)
-----------------------------------
The Group option under Add a Special Gift creates an independent group. Its collection plan offers only General collection (choose our gift later) or Our own gift (product link). Host wishlist gifts stay in their existing per-gift group contribution flow. V93 marks independent groups, migrates existing general/custom groups, and prevents an independent group from reserving a wishlist gift.

The creator's session can POST `/api/public/groups/{token}/gift-metadata` with `{productUrl}` to fill name, description and INR goal using the existing bounded retailer client and metadata parser. Invalid/unreadable links can be entered manually. Metadata fetches are limited to ten per minute per creator session. The group stores its own editable gift details, independently of host wishlist records. POST `.../gift-plan/{groupId}` edits the plan or returns to a general collection while preserving contributions, until gift publication.

Independent groups appear on the guest gift board immediately after creation. Draft cards show group name and Something is Cooking, with no gift details, contributor identities or progress amounts. Once receiving accounts and invitation prerequisites are ready, the card links to the existing group page, where guests can contribute without registration or join/sign in as members. Group setup proceeds directly to receiving accounts, then contribution settings (default access BOTH), and invitation sharing. Existing channel, verification and coordination-fee policies continue to apply.
