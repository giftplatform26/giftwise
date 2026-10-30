# Design with AI in Gift Invitation Studio

The studio's **Design with AI** button loads the current event title, occasion and date, and prefills host names, an appropriate visual style, the event description (up to 400 characters) and a suggested venue. These are shown for review before generation. It opens a short preference form: visual style, tone, language, optional names, public venue, time and a personal message. An event must be selected. Generation produces an editable invitation preview with AI wording, palette, typography and a layout using the studio's existing backgrounds. It does not generate a new raster illustration.

**Use this design** applies the preview as one undoable edit. Closing the dialog preserves the current design. Existing local draft saving and PNG/JSON exports continue to work. Generation and applying a draft do not publish the event or modify its saved details. The QR slot remains reserved for the application's real event QR; the model cannot choose its target.

## Backend setup

In IntelliJ **Run → Edit Configurations → backend application → Environment variables**, add:

```text
OPENAI_API_KEY=your_api_key
```

Optional settings:

```text
GIFT_INVITATION_AI_ENABLED=true
OPENAI_INVITATION_MODEL=gpt-4.1-mini
```

These map to `gift.invitation.ai.enabled`, `gift.invitation.ai.api-key`, and `gift.invitation.ai.model` in application.properties. Keep the actual key in the backend environment, never frontend code, source control, or a NEXT_PUBLIC variable. Restart the backend to load configuration and apply migration V74. Without a key the form reports that AI generation is unavailable; it does not pretend to generate a design.

The integration uses the [OpenAI Responses API with Structured Outputs](https://developers.openai.com/api/docs/guides/structured-outputs?api-mode=responses), `store:false`, a bounded output budget and schema validation. The default [GPT-4.1 mini model](https://developers.openai.com/api/docs/models/gpt-4.1-mini) can be changed through the backend setting.

## Data and limits

Only the saved event title, occasion, host display name, date and explicitly entered public invitation preferences are sent to AI. The event address is used to prefill the editable public venue field. The host can edit or remove it before pressing Generate. Only that reviewed field is sent to AI, rather than additional saved address fields. Contact name/email/phone, owner ID, public token, payment details and QR URL are excluded.

Authenticated event ownership and CSRF are required. Each host may attempt 10 generations per hour, enforced in PostgreSQL across application instances. Failed provider calls count toward that limit; unconfigured requests do not. Request metadata records no prompt, response text or API secret. Provider failures, refusals, incomplete output and unsupported design choices leave the current invitation unchanged. Both backend and frontend validate the generated result, and all produced layouts pass the existing QR protection checks.

Automated tests mock the AI provider. A real generation requires a configured API key and provider access; it was not executed with a paid key during development.
