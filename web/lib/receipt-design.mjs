// Host receipt designer: client-side checks that mirror ReceiptTemplateService, so hosts see problems before saving.
export const RECEIPT_LAYOUTS = ['CLASSIC', 'ELEGANT', 'FESTIVE', 'MINIMAL'];
export const RECEIPT_TEXT_LIMITS = {title: 60, headerText: 120, headerSubtext: 200, thankYouMessage: 400, footerText: 400, signatureName: 80};
const LABELS = {title: 'Receipt title', headerText: 'Header', headerSubtext: 'Header subtitle', thankYouMessage: 'Thank-you message', footerText: 'Footer', signatureName: 'Signature'};
export const LOGO_MAX_BYTES = 5 * 1024 * 1024;
export const LOGO_TYPES = ['image/png', 'image/jpeg', 'image/webp'];

const blank = (v) => typeof v !== 'string' || v.trim() === '';
// eslint-disable-next-line no-control-regex
const control = /[\u0000-\u0009\u000B-\u001F\u007F-\u009F]/;

/** Returns the payload for PUT /receipt-template, or throws an Error with a host-facing message. */
export function receiptPayload(design) {
  const layout = String(design?.layout ?? '').trim().toUpperCase();
  if (!RECEIPT_LAYOUTS.includes(layout)) throw Error('Choose a receipt layout');
  const accentColor = String(design?.accentColor ?? '').trim();
  if (!/^#[0-9A-Fa-f]{6}$/.test(accentColor)) throw Error('Choose a colour like #6336B5');
  const referencePrefix = String(design?.referencePrefix ?? '').trim().toUpperCase();
  if (!/^[A-Z0-9]{2,12}$/.test(referencePrefix)) throw Error('Receipt number prefix must be 2–12 letters or numbers');
  const text = {};
  for (const [key, max] of Object.entries(RECEIPT_TEXT_LIMITS)) {
    const value = design?.[key];
    if (blank(value)) { text[key] = null; continue; }
    const v = value.trim();
    if (v.length > max) throw Error(`${LABELS[key]} must be at most ${max} characters`);
    if (control.test(v)) throw Error(`${LABELS[key]} contains unsupported characters`);
    text[key] = v;
  }
  if (text.title === null) throw Error('Add a receipt title');
  const version = Number(design?.version);
  if (!Number.isInteger(version) || version < 0) throw Error('Reload the page and try again');
  return {
    enabled: design?.enabled === true, layout, accentColor: accentColor.toUpperCase(), ...text, referencePrefix,
    showGuestName: design?.showGuestName !== false, showGiftName: design?.showGiftName !== false,
    showEventDate: design?.showEventDate !== false, showPaymentMethod: design?.showPaymentMethod !== false,
    showPaymentCode: design?.showPaymentCode !== false, version,
  };
}

/** Quick check before uploading a logo; the server re-encodes and enforces its own limits. */
export function logoProblem(file) {
  if (!file) return 'Choose an image';
  if (!LOGO_TYPES.includes(file.type)) return 'Use a PNG, JPEG or WebP image';
  if (file.size > LOGO_MAX_BYTES) return 'This image is larger than 5 MB';
  return '';
}

/** Receipt number the next guest would get if the prefix were saved now (server sends the next number with the saved prefix). */
export function previewReceiptNumber(nextReceiptNumber, prefix) {
  const digits = /-(\d{6,})$/.exec(String(nextReceiptNumber ?? ''))?.[1] ?? '000001';
  const p = String(prefix ?? '').trim().toUpperCase();
  return `${/^[A-Z0-9]{2,12}$/.test(p) ? p : 'GIFT'}-${digits}`;
}
