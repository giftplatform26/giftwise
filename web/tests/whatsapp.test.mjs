import test from 'node:test';
import assert from 'node:assert/strict';
import {audienceCount} from '../lib/whatsapp.mjs';

test('audience counts skip opted-out guests', () => {
  const c = [{replyAttendance: 'YES'}, {replyAttendance: null}, {replyAttendance: 'NO'}, {replyAttendance: null, optedOut: true}];
  assert.equal(audienceCount(c, 'ALL'), 3);
  assert.equal(audienceCount(c, 'NO_REPLY'), 1);
  assert.equal(audienceCount(c, 'ATTENDING'), 1);
  assert.equal(audienceCount(null, 'ALL'), 0);
});
