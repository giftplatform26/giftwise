import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import { navigationTarget, isUpiAppUrl } from '../src/platform/policy.mjs';
const origin = 'https://celebrate.example';
const query = 'pa=asha@okhdfc&pn=Asha%20Rao&am=2500.00&cu=INR&tr=GWTABCDEFGHJK2345&tn=GiftWise%20GW-7K3P9Q';

test('UPI payment links from the web page open the installed UPI apps', () => {
  for (const url of [`upi://pay?${query}`, `gpay://upi/pay?${query}`, `phonepe://pay?${query}`, `paytmmp://pay?${query}`]) {
    assert.equal(isUpiAppUrl(url), true, url);
    assert.equal(navigationTarget(url, origin), 'external', url);
  }
});

test('other UPI or look-alike schemes stay blocked', () => {
  for (const url of ['upi://mandate?pa=x@y', 'gpay://settings', 'phonepe://transfer', 'paytmmp://cash_wallet', 'upi://pay?pa=x y', 'upi://pay?pa="x"', 'intent://pay#Intent;scheme=upi;end', `upi://pay?${'a'.repeat(1100)}`]) {
    assert.equal(isUpiAppUrl(url), false, url);
    assert.equal(navigationTarget(url, origin), 'blocked', url);
  }
});

test('the shell tells the page when the guest returns from the UPI app or no app opened', () => {
  const workspace = fs.readFileSync(new URL('../src/platform/Workspace.tsx', import.meta.url), 'utf8');
  assert.match(workspace, /AppState\.addEventListener\('change'/);
  assert.match(workspace, /giftwise:app-resumed/);
  assert.match(workspace, /giftwise:upi-launch-failed/);
  const web = fs.readFileSync(new URL('../../../web/components/UpiIntentPayment.tsx', import.meta.url), 'utf8');
  assert.match(web, /addEventListener\('giftwise:app-resumed'/);
  assert.match(web, /addEventListener\('giftwise:upi-launch-failed'/);
});
