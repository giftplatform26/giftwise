import test from 'node:test';
import assert from 'node:assert/strict';
import { validateInvitation, createInvitation, listInvitations, fetchInvitationPng } from '../lib/invitation-api.mjs';
const ID = '32a6d29c-83ea-4804-97c7-279f6f8406f1';
const DID = '25f583d1-93b7-4efd-944a-3c3aa99021fd';
const valid = { name: ' Family ', style: 'FLORAL', greeting: 'Celebrate!' };
test('trims design name', () => assert.equal(validateInvitation(valid).name, 'Family'));
test('rejects blank name', () => assert.throws(() => validateInvitation({ ...valid, name: ' ' }), /name/i));
test('rejects oversized name', () => assert.throws(() => validateInvitation({ ...valid, name: 'x'.repeat(101) }), /name/i));
test('accepts 100-character name', () => assert.equal(validateInvitation({ ...valid, name: 'x'.repeat(100) }).name.length, 100));
test('rejects unsupported style', () => assert.throws(() => validateInvitation({ ...valid, style: 'EVIL' }), /style/i));
test('accepts 180-character greeting', () => assert.equal(validateInvitation({ ...valid, greeting: 'a'.repeat(180) }).greeting.length, 180));
test('rejects 181-character greeting', () => assert.throws(() => validateInvitation({ ...valid, greeting: 'a'.repeat(181) }), /Greeting/));
test('rejects SVG image', () => assert.throws(() => validateInvitation({ ...valid, image: { type: 'image/svg+xml', size: 12 } }), /PNG/));
test('rejects oversized image', () => assert.throws(() => validateInvitation({ ...valid, image: { type: 'image/png', size: 6*1024*1024 } }), /5 MB/));
test('rejects empty image', () => assert.throws(() => validateInvitation({ ...valid, image: { type: 'image/png', size: 0 } }), /5 MB/));
test('list uses authenticated endpoint', async () => {
 const data = [{ id: DID }];
 const result = await listInvitations(async (url, opts) => {
  assert.equal(url, `/backend/api/v1/events/${ID}/qr/invitations`);
  assert.equal(opts.credentials, 'include'); return { ok: true, json: async () => data };
 }, ID);
 assert.deepEqual(result, data);
});
test('rejects invalid event ID', async () => assert.rejects(() => listInvitations(async () => {}, '../admin'), /Invalid event/));
test('rejects invalid design ID', async () => assert.rejects(() => fetchInvitationPng(async () => {}, ID, '../admin'), /Invalid invitation/));
test('PNG response remains binary', async () => {
 const blob = new Blob(['png'], { type: 'image/png' });
 assert.equal(await fetchInvitationPng(async (_, opts) => { assert.equal(opts.credentials, 'include'); return { ok: true, blob: async () => blob }; }, ID, DID), blob);
});
test('rejects non-PNG preview', async () => assert.rejects(() => fetchInvitationPng(async () => ({ ok: true, blob: async () => new Blob(['html'], { type: 'text/html' }) }), ID, DID), /Unexpected/));
test('maps access errors', async () => assert.rejects(() => listInvitations(async () => ({ ok: false, status: 403 }), ID), /Access denied/));
test('multipart POST includes CSRF and never sets Content-Type', async () => {
 let count = 0;
 const result = await createInvitation(async (url, opts) => {
  count++;
  if (count === 1) { assert.match(url, /\/auth\/csrf$/); return { ok: true }; }
  assert.equal(opts.method, 'POST'); assert.equal(opts.credentials, 'include');
  assert.equal(opts.headers['X-XSRF-TOKEN'], 'abc123');
  assert.equal(opts.headers['Content-Type'], undefined);
  assert.equal(opts.body.get('name'), 'Family');
  assert.equal(opts.body.get('style'), 'FLORAL');
  return { ok: true, json: async () => ({ id: DID }) };
 }, ID, valid, { cookie: 'XSRF-TOKEN=abc123' });
 assert.equal(result.id, DID); assert.equal(count, 2);
});
test('missing CSRF token fails closed', async () => {
 let count = 0;
 await assert.rejects(() => createInvitation(async () => { count++; return { ok: true }; }, ID, valid, { cookie: '' }), /CSRF cookie missing/);
 assert.equal(count, 1);
});
test('server conflict is surfaced', async () => {
 let count = 0;
 await assert.rejects(() => createInvitation(async () => { count++; return count === 1 ? { ok: true } : { ok: false, status: 409 }; }, ID, valid, { cookie: 'XSRF-TOKEN=abc123' }), /no longer be edited/);
});
