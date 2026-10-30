import test from 'node:test';
import assert from 'node:assert/strict';

import {
  hostApi,
  fetchEventQrPng
} from '../lib/host-api.mjs';

const ID = '32a6d29c-83ea-4804-97c7-279f6f8406f1';

test('QR details use authenticated host endpoint', async () => {
  const calls = [];

  const fetcher = async (url, options) => {
    calls.push({ url, options });

    return {
      ok: true,
      status: 200,
      json: async () => ({
        eventId: ID,
        theme: 'MODERN_MINIMAL',
        url: 'https://example.test/e/token'
      })
    };
  };

  const result = await hostApi.qrDetails(fetcher, ID);

  assert.equal(result.eventId, ID);
  assert.equal(
    calls[0].url,
    `/backend/api/v1/events/${ID}/qr`
  );
  assert.equal(calls[0].options.credentials, 'include');
});

test('QR PNG endpoint returns a blob', async () => {
  const expected = new Blob(['png'], { type: 'image/png' });

  const fetcher = async (url, options) => {
    assert.equal(url, `/backend/api/v1/events/${ID}/qr/png`);
    assert.equal(options.credentials, 'include');

    return {
      ok: true,
      blob: async () => expected
    };
  };

  const result = await fetchEventQrPng(fetcher, ID);

  assert.equal(result, expected);
});

test('invalid QR event ID is rejected', async () => {
  await assert.rejects(
    () => fetchEventQrPng(async () => {}, '../other'),
    /Invalid event identifier/
  );
});

test('QR PNG failures are reported', async () => {
  await assert.rejects(
    () => fetchEventQrPng(
      async () => ({ ok: false, status: 403 }),
      ID
    ),
    /HTTP 403/
  );
});