import test from 'node:test';
import assert from 'node:assert/strict';
import {receiptPayload, logoProblem, previewReceiptNumber} from '../lib/receipt-design.mjs';

const base = {enabled: true, layout: 'elegant', accentColor: '#9a6b2f', title: ' Gift receipt ', headerText: 'Asha & Ravi', headerSubtext: '',
  thankYouMessage: 'Thank you!', footerText: null, signatureName: '  ', referencePrefix: 'asha30', showGuestName: true, showGiftName: false,
  showEventDate: true, showPaymentMethod: true, showPaymentCode: true, version: 3};

test('normalises a valid design into the API payload', () => {
  const p = receiptPayload(base);
  assert.equal(p.layout, 'ELEGANT');
  assert.equal(p.accentColor, '#9A6B2F');
  assert.equal(p.referencePrefix, 'ASHA30');
  assert.equal(p.title, 'Gift receipt');
  assert.equal(p.headerSubtext, null);
  assert.equal(p.signatureName, null);
  assert.equal(p.showGiftName, false);
  assert.equal(p.version, 3);
});

test('rejects what the server would reject', () => {
  assert.throws(() => receiptPayload({...base, layout: 'NEON'}), /layout/);
  assert.throws(() => receiptPayload({...base, accentColor: 'red'}), /colour/);
  assert.throws(() => receiptPayload({...base, referencePrefix: 'A'}), /prefix/);
  assert.throws(() => receiptPayload({...base, referencePrefix: 'GIFT-1'}), /prefix/);
  assert.throws(() => receiptPayload({...base, title: '   '}), /title/);
  assert.throws(() => receiptPayload({...base, headerText: 'x'.repeat(121)}), /Header must be at most 120/);
  assert.throws(() => receiptPayload({...base, footerText: 'bad\u0007bell'}), /unsupported/);
  assert.throws(() => receiptPayload({...base, version: -1}), /Reload/);
});

test('allows line breaks in longer messages', () => {
  assert.equal(receiptPayload({...base, thankYouMessage: 'Line one\nLine two'}).thankYouMessage, 'Line one\nLine two');
});

test('logo pre-checks', () => {
  assert.equal(logoProblem(null), 'Choose an image');
  assert.match(logoProblem({type: 'image/gif', size: 10}), /PNG/);
  assert.match(logoProblem({type: 'image/png', size: 6 * 1024 * 1024}), /5 MB/);
  assert.equal(logoProblem({type: 'image/jpeg', size: 1000}), '');
});

test('previews the next receipt number with an unsaved prefix', () => {
  assert.equal(previewReceiptNumber('GIFT-000017', 'asha30'), 'ASHA30-000017');
  assert.equal(previewReceiptNumber('GIFT-000017', 'x'), 'GIFT-000017');
  assert.equal(previewReceiptNumber(null, 'ABC'), 'ABC-000001');
});
