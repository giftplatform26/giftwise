# GiftWise email templates

Seven HTML/text template pairs are provided as assets. They are connected through `BrandedEmailService` and `TransactionalEmailOutbox`; configured security delivery also uses a branded HTML/text layout. See `docs/BRANDING_AND_READINESS_IMPLEMENTATION.md` for provider/origin setup, delivery triggers, retry behavior and remaining translation work.

Integration contract:

- Send multipart/alternative HTML and plain text; use the matching heading as subject. Localize subject/body through the existing localization service before rendering.
- HTML-escape all text substitutions and attribute values. Plain-text substitutions must not be HTML-escaped. Reject missing variables. Do not use direct string replacement on untrusted data.
- Resolve every URL from configured trusted HTTPS public origins. Validate URL scheme/host before HTML escaping. Brand image must be a stable public HTTPS asset. Never accept arbitrary guest-supplied action/support URLs.
- Supply languageCode from enabled locales; dates and formattedAmount from event/transaction locale and authoritative currency/amount.
- verificationCode and expiresInMinutes come only from the existing expiring challenge service. Never store rendered security messages or codes in logs/analytics.
- Deduplicate registration/event/payment delivery using authoritative source IDs. Contribution received and payment success templates require verified receipt/capture; manual transfers need separate pending wording.
- Respect anonymity and recipient authorization; do not include guest names or lists without applicable permission. Use existing notification preferences for optional messages; security notices remain essential.
- Configure real support/legal URLs and finalized legal pages before production delivery. Preview long names and translated content in desktop/mobile email clients.
