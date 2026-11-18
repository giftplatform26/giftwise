// Direct UPI intent payments: the guest pays the host's own UPI ID from their UPI app.
// The platform never receives the money. Everything the UPI app or the guest tells us is a report,
// not proof: only the host (checking their own bank/UPI app) can confirm receipt.

const UUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

/** ANDROID, IOS or DESKTOP. iPadOS presents a desktop Mac user agent, so touch points decide. */
export function detectPlatform({userAgent = '', maxTouchPoints = 0} = {}) {
  const ua = String(userAgent);
  if (/Android/i.test(ua)) return 'ANDROID';
  if (/iPhone|iPad|iPod/i.test(ua) || (/Macintosh/i.test(ua) && Number(maxTouchPoints) > 1)) return 'IOS';
  return 'DESKTOP';
}

/** Only server-built upi://pay links are ever used as a link target. */
export function isSafeUpiUri(uri) {
  return typeof uri === 'string' && uri.length <= 1024 && /^upi:\/\/pay\?[A-Za-z0-9%._~@&=+-]+$/.test(uri);
}

/**
 * iPhone: iOS lets several apps claim upi://, and it picks one without a chooser. App-specific schemes
 * let the guest pick. These are best effort; not every app version accepts every parameter.
 */
export const IOS_UPI_APPS = [
  {id: 'gpay', label: 'Google Pay', prefix: 'gpay://upi/pay?'},
  {id: 'phonepe', label: 'PhonePe', prefix: 'phonepe://pay?'},
  {id: 'paytm', label: 'Paytm', prefix: 'paytmmp://pay?'},
];

/** Launch options for the platform. Android shows the system chooser for the generic upi:// link. */
export function upiLaunchOptions(uri, platform) {
  if (!isSafeUpiUri(uri)) return [];
  const query = uri.slice('upi://pay?'.length);
  const generic = {id: 'any', label: platform === 'IOS' ? 'Other UPI app' : 'Pay using any UPI app', href: uri};
  if (platform === 'IOS') return [...IOS_UPI_APPS.map(app => ({id: app.id, label: app.label, href: app.prefix + query})), generic];
  if (platform === 'ANDROID') return [generic];
  return [];
}

/**
 * Preview of a UPI app response string (txnId=..&Status=SUCCESS&ApprovalRefNo=..). Only native shells
 * receive one. The server parses it again and stores it as untrusted evidence.
 */
export function parseUpiResponse(raw) {
  const text = String(raw ?? '').trim();
  if (!text || text.length > 1024 || /[\u0000-\u001f]/.test(text)) return null;
  const fields = {};
  for (const pair of text.split('&')) {
    const eq = pair.indexOf('=');
    if (eq <= 0) return null;
    let key, value;
    try { key = decodeURIComponent(pair.slice(0, eq)).toLowerCase(); value = decodeURIComponent(pair.slice(eq + 1)).trim(); }
    catch { return null; }
    if (key in fields) return null;
    fields[key] = value;
  }
  const raw_status = String(fields.status ?? '').toUpperCase();
  const status = raw_status === 'SUCCESS' ? 'SUCCESS' : ['FAILURE', 'FAILED'].includes(raw_status) ? 'FAILURE'
    : ['SUBMITTED', 'PENDING'].includes(raw_status) ? 'SUBMITTED' : 'UNKNOWN';
  const approval = fields.approvalrefno ?? fields.approvalref ?? '';
  return {status, txnId: fields.txnid || null, txnRef: fields.txnref || null,
    utr: /^\d{12}$/.test(approval) ? approval : null};
}

/** Statuses as the guest sees them. Green is reserved for independent verification. */
export function upiStatusCopy(status) {
  switch (status) {
    case 'INITIATED': return {label: 'Payment app opened', tone: 'neutral', next: 'Finish paying in your UPI app, then come back here.'};
    case 'PENDING': return {label: 'Waiting for payment confirmation', tone: 'pending', next: 'If the money left your account, report it with the 12-digit UPI reference.'};
    case 'FAILED': return {label: 'Payment not completed', tone: 'attention', next: 'No payment was recorded. You can try again.'};
    case 'PAYMENT_REPORTED': return {label: 'Payment reported · awaiting host verification', tone: 'pending', next: 'The host will check their bank or UPI app and confirm.'};
    case 'HOST_CONFIRMED': return {label: 'Host confirmed receipt', tone: 'confirmed', next: 'The host confirmed this payment arrived.'};
    case 'INDEPENDENTLY_VERIFIED': return {label: 'Independently verified', tone: 'success', next: 'This payment was verified independently.'};
    case 'REJECTED': return {label: 'The host could not find this payment', tone: 'attention', next: 'Check the payment in your UPI app and contact the host.'};
    case 'EXPIRED': return {label: 'Payment link expired', tone: 'muted', next: 'Start a new payment if you still want to send a gift.'};
    default: return {label: 'Payment not initiated', tone: 'neutral', next: 'Choose an amount and pay using UPI.'};
  }
}

/** Remembered attempt so a guest returning from the UPI app (or after iOS reloads the tab) can report it. */
export const attemptStorageKey = token => `giftwise.upi-attempt.${token}`;

export function readStoredAttempt(storage, token) {
  try {
    const value = JSON.parse(storage?.getItem(attemptStorageKey(token)) ?? 'null');
    if (!value || !UUID.test(String(value.attemptId)) || !Number.isFinite(value.savedAt)) return null;
    if (Date.now() - value.savedAt > 24 * 60 * 60 * 1000) return null;
    return {attemptId: value.attemptId, savedAt: value.savedAt, launched: value.launched === true};
  } catch { return null; }
}

export function storeAttempt(storage, token, attemptId, launched = false) {
  try { storage?.setItem(attemptStorageKey(token), JSON.stringify({attemptId, savedAt: Date.now(), launched})); } catch { /* storage unavailable */ }
}

export function clearStoredAttempt(storage, token) {
  try { storage?.removeItem(attemptStorageKey(token)); } catch { /* storage unavailable */ }
}

export const MAX_SCREENSHOT_BYTES = 5_000_000;

/** Returns an error message, or '' when the screenshot can be uploaded. Server re-validates and re-encodes. */
export function screenshotProblem(file) {
  if (!file) return '';
  if (!['image/png', 'image/jpeg'].includes(file.type)) return 'Choose a PNG or JPEG screenshot.';
  if (file.size > MAX_SCREENSHOT_BYTES) return 'Choose a screenshot up to 5 MB.';
  return '';
}
