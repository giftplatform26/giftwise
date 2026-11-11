// Customer-acquisition helpers: try-without-account drafts, story cards, offers and partner page addresses.

export const TRY_DRAFT_KEY = 'gift.try.draft';
const WEEK = 7 * 24 * 60 * 60 * 1000;

/** Fields a visitor can fill before signing up, in the same shape the signed-in creator restores. */
export function tryDraft(form, now = Date.now()) {
  const pick = (v, max) => String(v ?? '').slice(0, max);
  return {
    title: pick(form.title, 200), description: pick(form.description, 2000), occasionType: pick(form.occasionType, 30),
    eventDate: /^\d{4}-\d{2}-\d{2}$/.test(form.eventDate ?? '') ? form.eventDate : '', hostName: pick(form.hostName, 200),
    city: pick(form.city, 120), theme: pick(form.theme, 40) || 'MODERN_MINIMAL', savedAt: now,
  };
}

/** A draft saved within the last week, or null. */
export function readTryDraft(storage, now = Date.now()) {
  try {
    const v = JSON.parse(storage.getItem(TRY_DRAFT_KEY) || 'null');
    return v && typeof v === 'object' && now - Number(v.savedAt) < WEEK ? v : null;
  } catch { return null; }
}

/** Splits text into lines that fit a width, using a measure function (canvas measureText in the browser). */
export function wrapLines(text, maxWidth, measure, maxLines = 3) {
  const words = String(text ?? '').split(/\s+/).filter(Boolean);
  const lines = [];
  let line = '';
  for (const w of words) {
    const next = line ? `${line} ${w}` : w;
    if (measure(next) <= maxWidth || !line) line = next;
    else { lines.push(line); line = w; }
    if (lines.length === maxLines) break;
  }
  if (line && lines.length < maxLines) lines.push(line);
  if (lines.length === maxLines && words.join(' ').length > lines.join(' ').length) lines[maxLines - 1] = lines[maxLines - 1].replace(/\s*\S*$/, '') + '…';
  return lines;
}

/** Days until a date (local), never negative. */
export function daysUntil(isoDate, today = new Date()) {
  if (!isoDate) return null;
  const d = new Date(`${isoDate}T12:00:00`);
  const t = new Date(today.getFullYear(), today.getMonth(), today.getDate(), 12);
  return Math.max(0, Math.round((d - t) / 86400000));
}

/** Story card headline per kind. */
export function cardCopy(kind, {title, hostName, days}) {
  switch (kind) {
    case 'COUNTDOWN': return {kicker: days === 0 ? 'Today is the day' : days === 1 ? '1 day to go' : `${days ?? ''} days to go`, headline: title, footer: 'See the invitation'};
    case 'THANK_YOU': return {kicker: 'Thank you', headline: `for celebrating ${title} with us`, footer: hostName ? `With love, ${hostName}` : 'With love'};
    case 'LIVE': return {kicker: 'Happening now', headline: title, footer: 'Send your wishes'};
    default: return {kicker: "You're invited", headline: title, footer: 'Tap the link to RSVP'};
  }
}

/** Remembers the free-first-celebration offer from a guest page for 14 days, so it can be claimed after sign-up. */
export function rememberOffer(token, storage, now = Date.now()) {
  if (/^[0-9a-f-]{36}$/i.test(String(token ?? ''))) storage.setItem('gift.offer', JSON.stringify({token, at: now}));
}
export function pendingOffer(storage, now = Date.now()) {
  try {
    const v = JSON.parse(storage.getItem('gift.offer') || 'null');
    return v && /^[0-9a-f-]{36}$/i.test(v.token) && now - v.at < 14 * 86400000 ? v.token : null;
  } catch { return null; }
}

/** "Lotus Weddings & Events" → "lotus-weddings-events". */
export function slugify(name) {
  return String(name ?? '').normalize('NFKD').replace(/\p{M}/gu, '').toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-+|-+$/g, '').slice(0, 40);
}
