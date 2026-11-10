export function isAdminPath(pathname) {
  try {
    let path = pathname.split(/[?#]/, 1)[0];
    for (let i = 0; i < 4; i++) { const decoded = decodeURIComponent(path); if (decoded === path) break; path = decoded; }
    path = new URL('https://giftwise.invalid' + '/' + path.replace(/\\/g, '/').replace(/^\/+/, '')).pathname;
    return /^\/(?:admin(?:\/|$)|(?:backend\/)?api\/admin(?:\/|$))/i.test(path);
  } catch { return true; }
}
