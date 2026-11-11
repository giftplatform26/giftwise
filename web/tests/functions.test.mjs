import test from 'node:test';
import assert from 'node:assert/strict';
import {functionWhen, functionProblem, groupByDay, rsvpPayload} from '../lib/functions.mjs';

test('describes when a function happens', () => {
  assert.equal(functionWhen({}), '');
  assert.match(functionWhen({functionDate: '2026-12-10', startTime: '16:00', endTime: '21:00:00'}), /–/);
  assert.ok(functionWhen({startTime: '19:00'}).length > 0);
});

test('validates like the server', () => {
  assert.match(functionProblem({name: ''}), /Name the function/);
  assert.equal(functionProblem({name: 'Mehendi', endTime: '20:00'}), 'Add a start time');
  assert.equal(functionProblem({name: 'X', functionDate: '2026-12-10', startTime: '19:00', endTime: '18:00'}), 'End time must be after the start time');
  assert.match(functionProblem({name: 'X', mapUrl: 'javascript:alert(1)'}), /https/);
  assert.equal(functionProblem({name: 'X', mapUrl: 'https://maps.example/x'}), '');
});

test('groups by day with undated last', () => {
  const g = groupByDay([{id: 1}, {id: 2, functionDate: '2026-12-10'}, {id: 3, functionDate: '2026-12-10'}]);
  assert.equal(g[0].date, '2026-12-10');
  assert.equal(g[0].items.length, 2);
  assert.equal(g[1].date, null);
});

test('builds RSVP payload and skips unanswered', () => {
  const p = rsvpPayload({guestName: ' Asha ', guestEmail: 'a@b.co', answers: {f1: {attendance: 'YES', partySize: 14}, f2: {attendance: 'NO', partySize: 3}, f3: {}}});
  assert.deepEqual(p.answers, [{functionId: 'f1', attendance: 'YES', partySize: 10}, {functionId: 'f2', attendance: 'NO', partySize: 0}]);
  assert.throws(() => rsvpPayload({guestName: 'A', guestEmail: 'bad', answers: {}}), /email/);
  assert.throws(() => rsvpPayload({guestName: 'A', guestEmail: 'a@b.co', answers: {f3: {}}}), /at least one/);
});
