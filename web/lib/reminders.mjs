// Occasion reminders and yearly re-hosting helpers.
export const OCCASION_CHOICES = ['Birthday', 'Wedding anniversary', 'Work anniversary', 'Naming day', 'Remembrance'];

export function whenLabel(daysAway) {
  if (daysAway === 0) return 'Today';
  if (daysAway === 1) return 'Tomorrow';
  if (daysAway < 7) return `In ${daysAway} days`;
  if (daysAway < 14) return 'Next week';
  return `In ${Math.round(daysAway / 7)} weeks`;
}

/** Remember a referral code from ?ref= for 14 days, so it can be claimed after sign-up. */
export function rememberReferral(search, storage, now = Date.now()) {
  const code = new URLSearchParams(search).get('ref');
  if (code && /^[A-Za-z0-9_]{3,40}$/.test(code)) storage.setItem('gift.ref', JSON.stringify({code: code.toUpperCase(), at: now}));
}
export function pendingReferral(storage, now = Date.now()) {
  try {
    const v = JSON.parse(storage.getItem('gift.ref') || 'null');
    return v && typeof v.code === 'string' && now - v.at < 14 * 86400000 ? v.code : null;
  } catch { return null; }
}
