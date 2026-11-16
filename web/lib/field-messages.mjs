// Plain-language requirements for inputs that use a `pattern`, so people see what to type instead of the
// browser's generic "Please match the requested format". A field's own `title` always wins.
export const MOBILE_HINT = 'Enter a 10-digit mobile number (for example 9876543210), or include the country code (for example +919876543210).';

const BY_PATTERN = {
  '[0-9]{6}': 'Enter the 6-digit code.',
  '[A-Za-z0-9][A-Za-z0-9_]{3,29}': 'Use 4–30 letters, numbers or underscores, starting with a letter or number. No spaces.',
  'https://[^\\s]+': 'Enter a full link starting with https:// and without spaces.',
  '\\+[1-9][0-9]{7,14}': 'Enter the mobile number with the country code, for example +919876543210.',
  '[0-9]{12}': 'Enter the 12-digit UPI reference (UTR) number.',
  '[A-Za-z0-9\\-]{8,40}': 'Enter the bank reference: 8–40 letters, numbers or hyphens.',
  '[A-Za-z0-9]{2,12}': 'Use 2–12 letters or numbers, without spaces.',
  '\\+?[0-9][0-9 \\-]{7,23}': 'Enter a phone number using digits, spaces or dashes (a leading + is allowed).',
  '[A-Z0-9_]*': 'Use capital letters, numbers or underscores only.',
  '[A-Za-z0-9_-]+': 'Use letters, numbers, underscores or hyphens only, without spaces.'
};

/** The message to show for a field whose value does not match its pattern; null when there is nothing better to say. */
export function formatRequirement(field) {
  const title = typeof field?.title === 'string' ? field.title.trim() : '';
  if (title) return title;
  const pattern = typeof field?.getAttribute === 'function' ? field.getAttribute('pattern') : field?.pattern;
  return (pattern && BY_PATTERN[pattern]) || null;
}

export const knownPatterns = Object.keys(BY_PATTERN);
