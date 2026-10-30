# Username recovery and payment clarity

This phase adds features alongside existing sign-in, credential reset and contribution flows.

## Username reminders

Host/platform-admin sign-in and the creator/member sign-in screens link to `/forgot-username`. The page supports Event host, Platform administrator, Group creator and Group member. Host/admin reminders require the matching role, an enabled account and verified email. Older accounts without a separate username receive email sign-in guidance. The page carries the event token and, for members, group ID back to `POST /api/auth/recovery/username/request`. Only an unambiguous account with a previously verified email is eligible. The username is emailed, never returned in the HTTP response. Unknown, unverified, ambiguous and delivery-failed requests receive the same response.

The endpoint retains CSRF protection and caps requests at 12 per IP per hour and three deliveries per account per hour. Limits are reserved atomically in PostgreSQL. Passwords, PINs, verification timestamps and credential/session versions are not changed. The existing email provider configuration is reused with a separate reminder subject; password/PIN recovery retains its existing email subject.

Apply Flyway migrations V77 and V78 by restarting the backend. Actual reminder delivery requires the existing email provider, sender and credentials to be configured. No email-provider setting is enabled automatically.

## Payment clarity

Event transactions display payment-status badges, progress and next steps. Only a captured Razorpay checkout with server verification evidence is labelled verified. Direct transfer reports stay distinct from verified funds. The guest manual-transfer receipt states that receipt of funds still requires independent verification.

The owner-only `GET /api/v1/events/{eventId}/receiving-health` endpoint checks event-linked account availability. It explains unreadable details, disabled accounts, missing confirmation or QR, non-preferred accounts, private invitations and disabled direct transfers. It returns no decrypted payment fields and makes no account, preference, verification or ledger changes.

## Email delivery configuration

Set these in the backend environment, then restart:

```text
GIFT_OTP_EMAIL_PROVIDER=resend
GIFT_OTP_EMAIL_RESEND_API_KEY=<provider API key>
GIFT_OTP_EMAIL_FROM=<provider-verified sender address>
```

With delivery disabled, username recovery returns HTTP 503 consistently for all account types; the page displays an unavailable message instead of a successful reminder request. When configured, unknown/unverified accounts still receive the same generic response as eligible accounts. Provider send failures are logged without recipient addresses, usernames, credentials or provider response bodies.

## Verification signing configuration

Email/contact verification uses `gift.otp.hmac-key-base64`. It now resolves from `GIFT_OTP_HMAC_KEY_BASE64`, falling back to the existing `gift.otp.secret-base64` when no dedicated recovery key is supplied. Explicit recovery-key configuration retains priority. This fixes installations that configured the original OTP secret but could not request email verification codes.

Authenticated email enrollment exposes approved delivery explanations (unverified sender domain, rejected API key, provider throttling or connection failure) without exposing provider bodies, recipients or credentials. Password-reset and username-reminder requests retain generic account-eligibility responses. Receiving a verification code and submitting it successfully is still required to mark an email verified.
