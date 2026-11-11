import test from 'node:test';
import assert from 'node:assert/strict';
import {safeCharityUrl, charityProblem} from '../lib/gift-kinds.mjs';

test('only https charity links are used', () => {
  assert.equal(safeCharityUrl('https://give.example/donate'), 'https://give.example/donate');
  assert.equal(safeCharityUrl('javascript:alert(1)'), null);
  assert.equal(safeCharityUrl('http://insecure.example'), null);
  assert.equal(safeCharityUrl(null), null);
});
test('charity wishes need a name and link', () => {
  assert.match(charityProblem('', 'https://x.example'), /name/);
  assert.match(charityProblem('Green Earth', 'x'), /https/);
  assert.equal(charityProblem('Green Earth', 'https://x.example'), '');
});
