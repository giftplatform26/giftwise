// Experience and charity wishlist items.
export function safeCharityUrl(url) {
  return typeof url === 'string' && /^https:\/\/[^\s<>"]+$/.test(url) && url.length <= 500 ? url : null;
}
export function charityProblem(name, url) {
  if (!String(name ?? '').trim()) return "Add the charity's name";
  if (String(name).trim().length > 120) return 'Charity name must be at most 120 characters';
  if (!safeCharityUrl(String(url ?? '').trim())) return "Add the charity's donation page link (starting with https://)";
  return '';
}
