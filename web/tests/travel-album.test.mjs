import test from 'node:test';
import assert from 'node:assert/strict';
import {toLocalInput, legTime, clockOnly, dayLabel, legSummary, travelShareLink} from '../lib/travel.mjs';
import {fitWithin, friendlySize} from '../lib/shared-album.mjs';

test('venue times are shown as written, independent of the viewer time zone', () => {
  assert.equal(toLocalInput('2026-12-11T14:30:00'), '2026-12-11T14:30');
  assert.equal(toLocalInput(null), '');
  assert.match(legTime('2026-12-11T14:30:00', 'en-IN'), /11 Dec.*2:30/i);
  assert.match(clockOnly('2026-12-11T09:05', 'en-GB'), /9:05/);
  assert.match(dayLabel('2026-12-11', 'en-GB'), /Fri.*11 December/);
  assert.equal(legTime('junk'), '');
});

test('leg summary and WhatsApp share link', () => {
  assert.equal(legSummary({mode: 'FLIGHT', ref: '6E 512', point: 'BLR T1'}), 'Flight 6E 512 · BLR T1');
  assert.equal(legSummary({point: 'Pune'}), 'Pune');
  const link = travelShareLink('https://x.test', {formToken: 'abc', guestName: 'Asha Rao', phone: '+91 98765 43210'}, 'Our Wedding');
  assert.ok(link.startsWith('https://wa.me/919876543210?text='));
  assert.ok(decodeURIComponent(link).includes('https://x.test/t/abc'));
  assert.ok(decodeURIComponent(link).includes('Hi Asha!'));
});

test('photos are scaled down but never up', () => {
  assert.deepEqual(fitWithin(4000, 3000, 2000), {width: 2000, height: 1500});
  assert.deepEqual(fitWithin(800, 600, 2000), {width: 800, height: 600});
  assert.deepEqual(fitWithin(0, 10, 100), {width: 0, height: 0});
  assert.equal(friendlySize(2.5 * 1024 * 1024), '2.5 MB');
  assert.equal(friendlySize(10), '1 KB');
});
