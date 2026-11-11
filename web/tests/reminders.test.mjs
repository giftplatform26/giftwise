import test from 'node:test';
import assert from 'node:assert/strict';
import {whenLabel, rememberReferral, pendingReferral} from '../lib/reminders.mjs';

test('friendly countdown', () => {
  assert.equal(whenLabel(0), 'Today');
  assert.equal(whenLabel(1), 'Tomorrow');
  assert.equal(whenLabel(5), 'In 5 days');
  assert.equal(whenLabel(10), 'Next week');
  assert.equal(whenLabel(28), 'In 4 weeks');
});

test('referral codes are kept for 14 days and validated', () => {
  const store = new Map(); const s = {getItem: k => store.get(k) ?? null, setItem: (k, v) => store.set(k, v)};
  rememberReferral('?ref=<script>', s, 0);
  assert.equal(pendingReferral(s, 0), null);
  rememberReferral('?ref=gwab12cd', s, 0);
  assert.equal(pendingReferral(s, 1000), 'GWAB12CD');
  assert.equal(pendingReferral(s, 15 * 86400000), null);
});
