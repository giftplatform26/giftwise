-- Use the product name "GiftWise" instead of the legacy "Gift Platform" in on-screen web text.
-- Message keys are unchanged; only already-bootstrapped text is updated (fresh installs read en.json).
-- Email/SMS templates are intentionally untouched: SMS wording must match DLT-registered templates.
UPDATE localization_sources
   SET source_text = replace(source_text, 'Gift Platform', 'GiftWise')
 WHERE message_key IN ('ui.775be12c6f4bb051','ui.85320364933e965d','ui.fbccad0a96f26b1b','ui.538d6ca0afe64ed0')
   AND source_text LIKE '%Gift Platform%';
UPDATE localization_translations
   SET translated_text = replace(translated_text, 'Gift Platform', 'GiftWise'), version = version + 1, changed_at = now()
 WHERE message_key IN ('ui.775be12c6f4bb051','ui.85320364933e965d','ui.fbccad0a96f26b1b','ui.538d6ca0afe64ed0')
   AND translated_text LIKE '%Gift Platform%';
