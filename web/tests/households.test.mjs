import test from 'node:test';
import assert from 'node:assert/strict';
import {partyOf, filterHouseholds, sideLabel, IMPORT_TEMPLATE, blankMember} from '../lib/households.mjs';

test('party counts attending members, or the expected size when nobody is listed', () => {
  assert.equal(partyOf({expectedSize: 4, members: []}), 4);
  assert.equal(partyOf({expectedSize: 4, members: [{name: 'A'}, {name: 'B', attending: false}, {name: ' '}]}), 1);
  assert.equal(partyOf({expectedSize: 0}), 1);
  assert.equal(partyOf({expectedSize: 99}), 30);
});

test('search matches word starts across names, members, city and phone', () => {
  const list = [
    {name: 'Sharma family', side: 'BRIDE', rsvp: 'YES', city: 'Pune', phone: '+919876543210', members: [{name: 'Priya Sharma'}]},
    {name: 'Ravi Kumar', side: 'GROOM', rsvp: 'PENDING', members: []},
    {name: 'José Dias', rsvp: 'NO', members: []},
  ];
  assert.deepEqual(filterHouseholds(list, {q: 'pri'}).map((h) => h.name), ['Sharma family']);
  assert.deepEqual(filterHouseholds(list, {q: 'pun'}).map((h) => h.name), ['Sharma family']);
  assert.deepEqual(filterHouseholds(list, {q: '98765'}).map((h) => h.name), ['Sharma family']);
  assert.deepEqual(filterHouseholds(list, {q: 'jose'}).map((h) => h.name), ['José Dias']);
  assert.deepEqual(filterHouseholds(list, {side: 'GROOM'}).map((h) => h.name), ['Ravi Kumar']);
  assert.deepEqual(filterHouseholds(list, {side: 'UNASSIGNED'}).map((h) => h.name), ['José Dias']);
  assert.deepEqual(filterHouseholds(list, {rsvp: 'YES'}).map((h) => h.name), ['Sharma family']);
  assert.equal(filterHouseholds(list, {}).length, 3);
});

test('labels and template are readable', () => {
  assert.equal(sideLabel('BRIDE'), "Bride's side");
  assert.equal(sideLabel('UNASSIGNED'), 'No side yet');
  assert.match(IMPORT_TEMPLATE.split('\n')[0], /^Name, People, Phone/);
  assert.equal(blankMember().attending, true);
});
