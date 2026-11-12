// Travel & stay: labels, date/time formatting for wall-clock venue times, and share text for a family's private link.

export const MODES = {FLIGHT: 'Flight', TRAIN: 'Train', BUS: 'Bus', CAR: 'Car', OTHER: 'Other'};

/** "2026-12-11T14:30:00" → "2026-12-11T14:30" for <input type="datetime-local">. */
export function toLocalInput(v) {
  const m = /^(\d{4}-\d{2}-\d{2})T(\d{2}:\d{2})/.exec(String(v ?? ''));
  return m ? `${m[1]}T${m[2]}` : '';
}

/** Wall-clock time as written on the ticket, never shifted by the viewer's time zone. */
export function legTime(v, locale) {
  const m = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})/.exec(String(v ?? ''));
  if (!m) return '';
  const d = new Date(Date.UTC(+m[1], +m[2] - 1, +m[3], +m[4], +m[5]));
  return new Intl.DateTimeFormat(locale, {timeZone: 'UTC', day: 'numeric', month: 'short', hour: 'numeric', minute: '2-digit'}).format(d);
}

export function clockOnly(v, locale) {
  const m = /T(\d{2}):(\d{2})/.exec(String(v ?? ''));
  if (!m) return '';
  return new Intl.DateTimeFormat(locale, {timeZone: 'UTC', hour: 'numeric', minute: '2-digit'}).format(new Date(Date.UTC(2000, 0, 1, +m[1], +m[2])));
}

export function dayLabel(d, locale) {
  const m = /^(\d{4})-(\d{2})-(\d{2})/.exec(String(d ?? ''));
  if (!m) return '';
  return new Intl.DateTimeFormat(locale, {timeZone: 'UTC', weekday: 'short', day: 'numeric', month: 'long'}).format(new Date(Date.UTC(+m[1], +m[2] - 1, +m[3])));
}

/** "Flight 6E 512 · BLR T1" */
export function legSummary(leg) {
  if (!leg) return '';
  return [leg.mode ? `${MODES[leg.mode] ?? leg.mode}${leg.ref ? ` ${leg.ref}` : ''}` : leg.ref, leg.point].filter(Boolean).join(' · ');
}

/** WhatsApp link that sends the family their private travel form. */
export function travelShareLink(origin, guest, eventTitle = '') {
  const url = `${origin}/t/${guest.formToken}`;
  const first = String(guest.guestName ?? '').trim().split(/\s+/)[0] || '';
  const text = `Hi ${first}! Please share your travel plans for ${eventTitle || 'the celebration'} so we can arrange your stay and pickup: ${url}`;
  const phone = String(guest.phone ?? '').replace(/\D/g, '');
  return `https://wa.me/${phone}?text=${encodeURIComponent(text)}`;
}
