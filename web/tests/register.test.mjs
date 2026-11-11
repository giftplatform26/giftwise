import test from 'node:test';
import assert from 'node:assert/strict';
import {parseRupees, rupeesInput, entryProblem, personKey, filterEntries, balance, nextSlides} from '../lib/register.mjs';

test('parses the ways people type rupees', () => {
  assert.equal(parseRupees('2100'), 210000);
  assert.equal(parseRupees('₹ 2,100.50'), 210050);
  assert.equal(parseRupees('5100/-'), 510000);
  assert.equal(parseRupees('Rs.501'), 50100);
  assert.equal(parseRupees(''), null);
  assert.throws(() => parseRupees('abc'), /amount like/);
  assert.throws(() => parseRupees('0.5'), /at least/);
  assert.throws(() => parseRupees('2100.555'), /amount like/);
});

test('formats paise back for editing', () => {
  assert.equal(rupeesInput(210000), '2100');
  assert.equal(rupeesInput(210050), '2100.50');
  assert.equal(rupeesInput(null), '');
});

test('checks entries like the server', () => {
  assert.equal(entryProblem({name: ' ', amountPaise: 100}), 'Add the name');
  assert.equal(entryProblem({name: 'Ravi', amountPaise: null, item: ''}), 'Add an amount or describe the gift');
  assert.equal(entryProblem({name: 'Ravi', amountPaise: null, item: 'Lamp'}), '');
  assert.match(entryProblem({name: 'Ravi', amountPaise: 100, phone: '12'}), /phone/);
  assert.equal(entryProblem({name: 'Ravi', amountPaise: 100, phone: '+91 98765-43210'}), '');
});

test('matches people the same way as the server', () => {
  assert.equal(personKey('  Ravi  KUMAR. '), 'ravi kumar');
  assert.equal(personKey('José'), 'jose');
  assert.equal(personKey('राम कुमार'), personKey('राम  कुमार'));
});

test('filters register rows', () => {
  const rows = [{giverName: 'Ravi', side: 'Bride side'}, {giverName: 'Lata', itemDescription: 'Silver lamp'}];
  assert.equal(filterEntries(rows, 'bride').length, 1);
  assert.equal(filterEntries(rows, 'LAMP')[0].giverName, 'Lata');
  assert.equal(filterEntries(rows, '').length, 2);
});

test('balance is positive when they gave more', () => {
  assert.equal(balance({givenPaise: 310000, receivedPaise: 510000}), 200000);
});

test('new wall wishes jump the queue', () => {
  const current = [{id: 'a'}, {id: 'b'}];
  const {fresh, all} = nextSlides(current, [{id: 'c'}, {id: 'a'}, {id: 'b'}], new Set());
  assert.deepEqual(fresh.map(s => s.id), ['c']);
  assert.deepEqual(all.map(s => s.id), ['c', 'a', 'b']);
});
