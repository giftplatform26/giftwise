import test from 'node:test';
import assert from 'node:assert/strict';
import {vendorLinks} from '../lib/vendors.mjs';

test('vendor links are built only from safe values', () => {
  const l = vendorLinks({phone: '+919876543210', website: 'https://x.example', instagram: 'lotus.decor'});
  assert.equal(l.whatsapp, 'https://wa.me/919876543210');
  assert.equal(l.call, 'tel:+919876543210');
  assert.equal(l.instagram, 'https://instagram.com/lotus.decor');
  const bad = vendorLinks({phone: 'javascript:1', website: 'javascript:alert(1)', instagram: '../x'});
  assert.deepEqual(bad, {call: null, whatsapp: null, website: null, instagram: null});
});
