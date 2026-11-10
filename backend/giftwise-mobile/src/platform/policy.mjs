export function isAdminPath(pathname) {
  try {
    let path = pathname.split(/[?#]/, 1)[0];
    for (let i = 0; i < 4; i++) { const decoded = decodeURIComponent(path); if (decoded === path) break; path = decoded; }
    path = new URL('https://giftwise.invalid' + '/' + path.replace(/\\/g, '/').replace(/^\/+/, '')).pathname;
    return /^\/(?:admin(?:\/|$)|(?:backend\/)?api\/admin(?:\/|$))/i.test(path);
  } catch { return true; }
}
export function validateOrigin(value, development = false) {
  if (!value || /[\s\\]/.test(value)) throw new Error('Set the GiftWise web URL before opening your workspace.');
  const url = new URL(value);
  if (url.username || url.password || url.search || url.hash || url.pathname !== '/') throw new Error('The web URL must be an origin without a path, query, or credentials.');
  if (url.protocol !== 'https:' && !(development && url.protocol === 'http:')) throw new Error('Release builds require an HTTPS GiftWise web URL.');
  return url.origin;
}
export function navigationTarget(value, origin, checkoutOrigins = []) {
  try {
    const url = new URL(value);
    if (url.origin === origin && isAdminPath(url.pathname)) return 'blocked';
    if (url.origin === origin && ['https:', 'http:'].includes(url.protocol)) return 'internal';
    if (url.protocol === 'https:' && checkoutOrigins.includes(url.origin)) return 'checkout';
    if (['https:', 'http:', 'mailto:', 'tel:', 'sms:', 'whatsapp:', 'upi:'].includes(url.protocol)) return 'external';
  } catch {}
  return 'blocked';
}
export function deepLinkPath(value, origin) {
  try {
    const url = new URL(value);
    if (url.origin === origin) return isAdminPath(url.pathname) ? null : url.pathname + url.search + url.hash;
    if (url.protocol === 'giftwise:' && url.hostname === 'open') {
      const path = url.pathname;
      if (!isAdminPath(path) && path.startsWith('/') && !path.startsWith('//') && !/[\\\r\n]/.test(path)) return path + url.search + url.hash;
    }
  } catch {}
  return null;
}
export function parseNativeMessage(raw, messageURL, origin) {
  if (isAdminPath(new URL(messageURL).pathname)) throw new Error('Administration is available only in the web application.');
  if (new URL(messageURL).origin !== origin || raw.length > 18_000_000) throw new Error('Untrusted or oversized native request.');
  const data = JSON.parse(raw);
  if (data.type === 'share' && typeof data.text === 'string' && data.text.length <= 8000) {
    if (data.url && (typeof data.url !== 'string' || (new URL(data.url).origin !== origin || isAdminPath(new URL(data.url).pathname)))) throw new Error('Only GiftWise links can be shared through this bridge.');
    return { type: 'share', text: data.text, url: data.url || '' };
  }
  if (data.type === 'clipboard' && typeof data.text === 'string' && data.text.length <= 8000) return { type: 'clipboard', text: data.text };
  if (data.type === 'export' && typeof data.base64 === 'string' && data.base64.length <= 16_000_000 && /^[A-Za-z0-9+/]*={0,2}$/.test(data.base64)) {
    const extensions = { 'image/png': '.png', 'image/jpeg': '.jpg', 'image/svg+xml': '.svg', 'application/pdf': '.pdf', 'application/json': '.json', 'text/csv': '.csv', 'text/plain': '.txt', 'text/calendar': '.ics' };
    const extension = extensions[data.mime];
    if (!extension) throw new Error('This file format is not supported by mobile export.');
    const stem = String(data.name || 'GiftWise-export').replace(/\.[^.]*$/, '').replace(/[^a-zA-Z0-9_-]/g, '-').slice(0,80) || 'GiftWise-export';
    return { type: 'export', name: stem + extension, mime: data.mime, base64: data.base64 };
  }
  throw new Error('Unsupported native request.');
}
