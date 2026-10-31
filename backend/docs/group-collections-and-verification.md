# Group collections and email verification

Global switches added in V81 override the contact-verification requirements below. Both channels default to disabled; see `platform-verification-controls.md`. Receiving-account requirements remain in force.

Migration: `V80__group_surprises_and_member_verification.sql` (run with normal Flyway startup).

- Group member email and its verification are optional by default. Platform admins can require verification for a particular host under **Hosts & privileges**; hosts can also require it in **Account**. Verification is required when either switch is enabled. Disabling a host switch cannot remove an administrator requirement. New registrations enforce this on the server.
- Host publishing requires verified account email plus the existing event-linked, enabled, preferred receiving-account checks. Event responses include `publishDisabledReasons` for tooltips.
- Group invitations require the creator's verified email and at least one enabled, non-revoked receiving account. This is independent of collection publication. Existing published groups also need to meet these requirements.
- A creator can reserve one public, active gift from their own event after email or mobile ownership is verified. The reservation immediately blocks new individual contribution orders/reports and shows guests **A surprise is waiting**. A unique database index prevents competing reservations. Existing payments already in flight are not cancelled.
- Only the creator can publish a collection. Publication reveals its report to the event owner and creates one in-app group notification. Invitations and reservations do not publish the collection. Existing groups need an explicit collection publication; the migration does not reveal them automatically.
- Host group summaries/details are separate from individual contributions. Manual transfer reports remain unverified and never credit the financial ledger. Notifications are in-app records, not email/SMS delivery.

## API additions

- GET/PUT `/api/admin/hosts/{hostId}/group-member-email-policy`
- GET/PUT `/api/host/group-member-email-policy`
- POST `/api/public/groups/{token}/gift-reservation/{groupId}` with `giftId`
- POST `/api/public/groups/{token}/publish/{groupId}` publishes to the host
- GET `/api/v1/events/{eventId}/contributions/groups` and `/{groupId}`
- GET `/api/v1/events/{eventId}/group-notifications`
- POST `/api/v1/events/{eventId}/group-notifications/{id}/read`

Mutations retain CSRF protection. Creator routes retain the page-bound creator session; host routes enforce event ownership.

## Existing delivery limits

The current mobile OTP implementation is a local simulator and deliberately does not mark mobile ownership verified. Use email verification until a real SMS provider is implemented and configured.

The current **Open UPI app** link uses `upi://pay`. It does not implement a return callback or automatically record a transfer. Users return with a transaction reference, and their report remains unverified. Reliable automatic confirmation requires a payment-provider integration with server-side payment verification/webhooks; returning to the page alone is not evidence of payment.
