AI invitation generation
========================

The configured provider was reproduced under IntelliJ debugger with a minimal, non-personal invitation request. It returned HTTP 429 with error code `credit_balance_exhausted` and type `insufficient_quota`. This is an exhausted API project balance, not a temporary design failure. Live generation requires the administrator to fund the configured OpenAI API project or configure a funded project key in the backend. No key or provider response body is returned to the browser.

The client now distinguishes exhausted credits (402 / AI_CREDITS_EXHAUSTED), provider throttling, configuration/access problems, connection failures and invalid responses. The frontend displays safe, actionable messages and preserves the existing invitation. API errors remain failures; there is no fallback labelled as an AI-generated design. Existing request limits still apply.

The generation request asks for elegant, occasion-specific wording with a welcoming tone. Strict response field length constraints now match the application's validated text limits. Saved event title, host names, date/time and public venue are rendered separately, preserving the supplied facts. The layout keeps a protected 204px real QR slot, adds a refined occasion label and an inviting scan call to action, and uses the established local backgrounds and palettes.

The AI dialog fetches the authenticated event QR PNG and requires it before applying a design. Applying reloads the same event QR in the main editor. Publishing is separate; a draft event's invitation must be published before guests can open the scanned link. Downloads retain the existing real-QR requirement.

Provider implementation reference: https://developers.openai.com/api/docs/guides/structured-outputs

Admin control of Design with AI (Flyway V95)
------------------------------------------
Design with AI now starts disabled. To activate it:
1. Admin → Hosts & privileges → Platform-wide modules → Design with AI.
2. Select the host and grant Design with AI under Host-specific privileges.
3. Admin → Events → enable AI design for the desired event.
The editor link requires all three switches. Disabled events are excluded from the standalone AI designer's event choices. Generation and event-context APIs also enforce the grants, so a direct request cannot bypass the hidden button. Turning access off does not alter previously generated invitation drafts.

Host and event rows provide View AI design details. These read-only panels show identity/context, access switches, provider configuration presence and request totals for the last 24 hours. They never expose API keys, stored prompts or provider response bodies. Event toggle changes are audited as EVENT_AI_DESIGN_CHANGED.

When enabled, the host's existing AI form still collects/loads the event title, occasion, names, date/time, public venue, language, tone, visual style and personal message. Access remains separate from provider availability: administrators can enable the form even while the backend needs provider configuration or API credits. Enabling a grant does not add credits.

API: GET `/api/v1/events/ai-design-options` returns only the authenticated host's eligible events. Admin details use GET `/api/admin/console/hosts/{hostId}/ai-design` and GET `/api/admin/console/events/{eventId}/ai-design-settings`. The corresponding event POST accepts `{enabled}`. Existing platform and host module toggles use the code AI_INVITATION.
