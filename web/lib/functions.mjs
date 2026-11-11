// Multi-function celebrations: presets, display and validation shared by host and guest views.
export const FUNCTION_PRESETS = ['Haldi', 'Mehendi', 'Sangeet', 'Wedding ceremony', 'Reception', 'Engagement', 'Cocktail night', 'Puja', 'Lunch', 'After-party'];

const time = (t) => {
  if (!t) return '';
  const [h, m] = String(t).split(':').map(Number);
  if (!Number.isFinite(h) || !Number.isFinite(m)) return '';
  const d = new Date(2000, 0, 1, h, m);
  return new Intl.DateTimeFormat(undefined, {hour: 'numeric', minute: '2-digit'}).format(d);
};

/** "Thu, 10 Dec · 4:00 pm – 9:00 pm" (pieces omitted when unknown). */
export function functionWhen(f) {
  const parts = [];
  if (f?.functionDate) {
    const d = new Date(`${f.functionDate}T12:00:00`);
    if (!Number.isNaN(d.getTime())) parts.push(new Intl.DateTimeFormat(undefined, {weekday: 'short', day: 'numeric', month: 'short'}).format(d));
  }
  const start = time(f?.startTime), end = time(f?.endTime);
  if (start) parts.push(end ? `${start} – ${end}` : start);
  return parts.join(' · ');
}

export function functionProblem(d) {
  if (!String(d?.name ?? '').trim()) return 'Name the function, e.g. Mehendi or Reception';
  if (String(d.name).trim().length > 80) return 'Function name must be at most 80 characters';
  if (d.endTime && !d.startTime) return 'Add a start time';
  if (d.startTime && d.endTime && d.functionDate && d.endTime <= d.startTime) return 'End time must be after the start time';
  if (d.mapUrl && !/^https:\/\/[^\s<>"]+$/.test(d.mapUrl.trim())) return 'Map link must start with https://';
  return '';
}

/** Groups functions by date for the guest timeline; undated ones go last. */
export function groupByDay(functions) {
  const groups = new Map();
  for (const f of functions ?? []) {
    const key = f.functionDate ?? '';
    if (!groups.has(key)) groups.set(key, []);
    groups.get(key).push(f);
  }
  return [...groups.entries()].sort(([a], [b]) => (a === '' ? 1 : b === '' ? -1 : 0)).map(([date, items]) => ({date: date || null, items}));
}

/** Builds the payload for the per-function RSVP; skips functions the guest left unanswered. */
export function rsvpPayload({guestName, guestEmail, answers}) {
  const name = String(guestName ?? '').trim(), email = String(guestEmail ?? '').trim();
  if (!name) throw Error('Enter your name');
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) throw Error('Enter a valid email so you can change your answer later');
  const list = Object.entries(answers ?? {}).filter(([, a]) => a?.attendance).map(([functionId, a]) => ({
    functionId, attendance: a.attendance, partySize: a.attendance === 'NO' ? 0 : Math.min(10, Math.max(1, Number(a.partySize) || 1)),
  }));
  if (!list.length) throw Error('Choose an answer for at least one function');
  return {guestName: name, guestEmail: email, answers: list};
}
