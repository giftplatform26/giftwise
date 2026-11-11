// Direct UPI / bank transfers straight to the host (no gateway, no fees).
// The payment code travels in the payment note so the host can match their statement.

// No 0/O/1/I so codes are easy to read aloud and type. Must match the backend PAYMENT_CODE pattern.
const ALPHABET = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
export const PAYMENT_CODE_PATTERN = /^GW-[A-HJ-NP-Z2-9]{6}$/;

export function newPaymentCode(random = n => crypto.getRandomValues(new Uint8Array(n))) {
  const bytes = random(6);
  let code = 'GW-';
  for (const byte of bytes) code += ALPHABET[byte % ALPHABET.length];
  return code;
}

export const paymentNote = code => `GiftWise ${code}`;

/** Normalise what the guest typed or pasted. UPI: keep the 12-digit UTR found anywhere in the text. */
export function normaliseReference(method, value) {
  const text = String(value ?? '').trim();
  if (method === 'upi') {
    const match = text.replace(/\s+/g, '').match(/\d{12}/);
    return match ? match[0] : text.replace(/\s+/g, '');
  }
  return text.replace(/\s+/g, '').toUpperCase();
}

/** Returns an error message, or '' when valid. Mirrors backend validation. */
export function referenceProblem(method, value) {
  const text = String(value ?? '').trim();
  if (!text) return method === 'upi'
    ? 'After paying, enter the 12-digit UPI reference (UTR) from your UPI app.'
    : 'After transferring, enter the bank reference (UTR) from your banking app.';
  if (method === 'upi') return /^\d{12}$/.test(text) ? '' : 'A UPI reference (UTR) is exactly 12 digits. You can find it in your UPI app under the payment details.';
  if (!/^[A-Za-z0-9-]{8,40}$/.test(text)) return 'A bank reference is 8–40 letters or numbers, without spaces.';
  return '';
}

export function receiptPath(receiptToken) {
  return /^[0-9a-f-]{36}$/i.test(String(receiptToken ?? '')) ? `/r/${receiptToken}` : null;
}

export const RECEIPT_STEPS = [
  {key: 'SENT', label: 'Sent by you'},
  {key: 'WAITING', label: 'Waiting for the host to confirm'},
  {key: 'DONE', label: 'Received by the host'},
];

/** Which receipt step is current, from the claim status. */
export function receiptStage(status) {
  if (status === 'HOST_CONFIRMED') return {step: 2, tone: 'success', label: 'Received by the host'};
  if (status === 'REJECTED') return {step: 1, tone: 'warning', label: 'The host could not find this transfer yet'};
  if (status === 'EXPIRED') return {step: 1, tone: 'muted', label: 'This report has expired'};
  return {step: 1, tone: 'pending', label: 'Waiting for the host to confirm'};
}
