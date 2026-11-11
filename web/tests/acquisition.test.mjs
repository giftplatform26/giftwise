import test from 'node:test';
import assert from 'node:assert/strict';
import {tryDraft, readTryDraft, TRY_DRAFT_KEY, wrapLines, daysUntil, cardCopy, rememberOffer, pendingOffer, slugify} from '../lib/acquisition.mjs';

const store = () => { const m = new Map(); return {getItem: k => m.get(k) ?? null, setItem: (k, v) => m.set(k, v)}; };

test('try drafts keep only creator fields and expire after a week', () => {
  const d = tryDraft({title: 'Asha turns 5', occasionType: 'BIRTHDAY', eventDate: 'not-a-date', evil: '<script>'}, 1000);
  assert.equal(d.eventDate, '');
  assert.equal(d.evil, undefined);
  const s = store(); s.setItem(TRY_DRAFT_KEY, JSON.stringify(d));
  assert.equal(readTryDraft(s, 2000).title, 'Asha turns 5');
  assert.equal(readTryDraft(s, 1000 + 8 * 86400000), null);
});

test('text wraps to the width and ellipsises overflow', () => {
  const measure = t => t.length * 10;
  assert.deepEqual(wrapLines('Meera and Arjun', 100, measure), ['Meera and', 'Arjun']);
  const long = wrapLines('one two three four five six seven eight nine ten', 60, measure, 2);
  assert.equal(long.length, 2);
  assert.ok(long[1].endsWith('…'));
});

test('countdown and copy', () => {
  assert.equal(daysUntil('2026-11-20', new Date(2026, 10, 11)), 9);
  assert.equal(daysUntil('2026-11-01', new Date(2026, 10, 11)), 0);
  assert.equal(cardCopy('COUNTDOWN', {title: 'X', days: 1}).kicker, '1 day to go');
  assert.match(cardCopy('THANK_YOU', {title: 'our wedding', hostName: 'Meera'}).footer, /Meera/);
});

test('offers are validated and expire', () => {
  const s = store();
  rememberOffer('bad', s, 0); assert.equal(pendingOffer(s, 0), null);
  rememberOffer('9ab7765e-4efe-4cee-88c5-ad6e4c2040c4', s, 0);
  assert.equal(pendingOffer(s, 1000), '9ab7765e-4efe-4cee-88c5-ad6e4c2040c4');
  assert.equal(pendingOffer(s, 15 * 86400000), null);
});

test('slugs for partner pages', () => {
  assert.equal(slugify('Lotus Weddings & Events!'), 'lotus-weddings-events');
  assert.equal(slugify('Café Décor'), 'cafe-decor');
});
