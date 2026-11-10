// The GiftWise Android/iOS app is a native shell around this web application. It appends
// NATIVE_APP_TOKEN to the WebView user agent (backend/giftwise-mobile/src/platform/Workspace.tsx).
// Platform administration is intentionally web-only. The checks below are defence in depth:
// the native shell already refuses admin navigation, and admin APIs require the admin role.
export const NATIVE_APP_TOKEN = 'GiftWiseApp';

export function isNativeAppUserAgent(userAgent) {
  return typeof userAgent === 'string' && new RegExp(`(?:^|\\s)${NATIVE_APP_TOKEN}/`).test(userAgent);
}

/** True for admin pages and admin APIs, including encoded and traversal variants. Fails closed. */
export function isAdminPath(pathname) {
  try {
    let path = String(pathname).split(/[?#]/, 1)[0];
    for (let i = 0; i < 4; i++) { const decoded = decodeURIComponent(path); if (decoded === path) break; path = decoded; }
    path = new URL('https://giftwise.invalid/' + path.replace(/\\/g, '/').replace(/^\/+/, '')).pathname;
    return /^\/(?:admin(?:\/|$)|(?:backend\/)?api\/admin(?:\/|$))/i.test(path);
  } catch { return true; }
}

export const NATIVE_ADMIN_MESSAGE = 'This app is for celebration hosts and guests. Use the GiftWise website for platform administration.';
