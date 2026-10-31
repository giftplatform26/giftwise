# Platform-wide verification controls

Apply Flyway migration V81 with the normal backend deployment and deploy the web changes together.

Both mobile OTP and email verification default to **disabled**. Platform Admin → Authentication & policies contains **Enable mobile OTP across the application** and **Bypass email verification across the application**. Only platform administrators can save these controls; changes are audited. Saved settings override the property defaults across instances and restarts. An enable operation requires local provider/key configuration. Disabling remains available during delivery outages.

When a channel is disabled, its forms, codes, status panels and signup reminders are hidden; its API endpoints are disabled as well. Email bypass overrides per-host member-email requirements and host/creator publishing/invitation email checks. Mobile OTP is offered in Account for platform admins, hosts, group creators and group members. Enabled mobile checks apply to host publishing, creator invitations/publication and signed-in member contribution reports; gift reservation requires ownership proof from an enabled channel. Existing username/password and PIN logins remain available, including administrator access to the switches.

Receiving-account requirements, account ownership, CSRF protection and password/PIN authentication are unchanged. Bypass does not set verification timestamps, grant account access, or permit credential recovery without proof. Code-based email recovery is hidden/disabled with the email channel; users keep their existing sign-in credentials and can contact support for recovery.

## Future MSG91 configuration

The required and optional fields are documented in `src/main/resources/application.properties.example` (and the local application.properties):

- `GIFT_OTP_SMS_PROVIDER=msg91`
- `GIFT_MSG91_AUTH_KEY`: server-side MSG91 Auth Key
- `GIFT_MSG91_TEMPLATE_ID`: MSG91 **OTP** template ID
- `GIFT_OTP_SECRET_BASE64`: stable random signing secret, Base64 encoded, at least 32 bytes
- Optional `GIFT_OTP_HMAC_KEY_BASE64`: dedicated contact-verification signing key; defaults to the above
- Optional `GIFT_OTP_SMS_EXPIRY_MINUTES=5` (1–10)

Map the approved Sender ID/Header, PE/Entity ID and DLT content template in MSG91 for Indian delivery. The approved OTP template must contain `##OTP##`. No credentials are sent to the browser. The SendOTP adapter sends the application's generated six-digit code and checks both HTTP status and MSG91's JSON success result. No live SMS is sent by automated tests.

Challenges are account/type/destination-bound; only HMAC evidence is stored. A provider acceptance is not ownership proof: verification requires the matching code. Limits: 3 sends/account/hour, 5 sends/destination/hour, 60-second resend cooldown, five guesses, single use and expiry. Failed sends remain rate-limited and cannot produce a verified contact. Legacy simulator challenges cannot be consumed by production mobile verification.

## Future email configuration

Set `GIFT_OTP_EMAIL_PROVIDER=resend`, `GIFT_OTP_EMAIL_RESEND_API_KEY`, `GIFT_OTP_EMAIL_FROM` (a verified sender/domain), and the signing key. Then turn off **Bypass email verification**. Missing provider credentials do not prevent application startup in bypass mode. No provider outage automatically changes the policy: an authenticated platform administrator explicitly bypasses the affected channel.

Provider reference: https://docs.msg91.com/otp/sendotp and https://msg91.com/help/sendotp/where-to-find-the-sendotp-api-how-to-get-template-id
