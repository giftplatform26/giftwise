// Shared helpers for the shagun register, personal gift ledger and live wall.
export const GIFT_KINDS = [
  ['CASH', 'Cash'], ['ENVELOPE', 'Envelope'], ['UPI', 'UPI / bank'], ['GIFT_ITEM', 'Gift item'], ['OTHER', 'Other'],
];
export const KIND_LABEL = Object.fromEntries([...GIFT_KINDS, ['ONLINE', 'Online gift'], ['BANK', 'Bank transfer']]);
// Customary amounts end in 1 (₹501, ₹1,101…) — one tap at a busy venue.
export const QUICK_AMOUNTS = [501, 1100, 2100, 5100, 11000];
export const WALL_THEMES = [['NIGHT', 'Night', 'Deep background, glowing text — best in dim halls'], ['IVORY', 'Ivory', 'Soft and elegant for daylight'], ['FESTIVE', 'Festive', 'Warm colours for a party mood']];

/** "2,100", "₹ 2100.50", "2100/-" → paise. Returns null when empty, throws on nonsense. */
export function parseRupees(value) {
  const raw = String(value ?? '').replace(/[₹,\s]/g, '').replace(/\/-$/, '').replace(/^rs\.?/i, '');
  if (raw === '') return null;
  if (!/^\d{1,10}(\.\d{1,2})?$/.test(raw)) throw Error('Enter an amount like 2100 or 2,100.50');
  const paise = Math.round(Number(raw) * 100);
  if (paise < 100) throw Error('Amount must be at least ₹1');
  if (paise > 100000000000) throw Error('That amount looks too large');
  return paise;
}

export function rupeesInput(paise) {
  if (paise == null) return '';
  return paise % 100 === 0 ? String(paise / 100) : (paise / 100).toFixed(2);
}

/** Validates a register / ledger entry before sending. Returns an error message or ''. */
export function entryProblem({name, amountPaise, item, phone}) {
  if (!String(name ?? '').trim()) return 'Add the name';
  if (String(name).trim().length > 120) return 'Name must be at most 120 characters';
  if (amountPaise == null && !String(item ?? '').trim()) return 'Add an amount or describe the gift';
  if (phone && !/^\+?[0-9]{7,15}$/.test(String(phone).replace(/[\s()\-.]/g, ''))) return 'Enter a valid phone number, or leave it empty';
  return '';
}

export function newClientKey() {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) return crypto.randomUUID();
  return `${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 12)}`;
}

/** Same matching rule as the server: case, accents, punctuation and extra spaces ignored. */
export function personKey(name) {
  return String(name ?? '').normalize('NFKD').replace(/\p{M}/gu, '').toLowerCase().replace(/[^\p{L}\p{N} ]/gu, ' ').replace(/\s+/g, ' ').trim();
}

/** Case-insensitive filter over register rows by giver, side, item or note. */
export function filterEntries(entries, query) {
  const q = personKey(query);
  if (!q) return entries;
  return entries.filter(e => [e.giverName, e.side, e.itemDescription, e.note].some(v => personKey(v).includes(q)));
}

/** "You gave ₹3,100 · they gave ₹5,100" balance hint for the ledger; positive means they gave more. */
export function balance(person) {
  return (person?.receivedPaise ?? 0) - (person?.givenPaise ?? 0);
}

/** Order slides so new wishes appear next, then the rest loop. */
export function nextSlides(current, incoming, shownIds) {
  const known = new Set(current.map(s => s.id));
  const fresh = incoming.filter(s => !known.has(s.id) && !shownIds.has(s.id));
  const kept = incoming.filter(s => !fresh.includes(s));
  return {fresh, all: [...fresh, ...kept]};
}
