import test from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
const payment=readFileSync(new URL('../app/host/payment-settings/page.tsx',import.meta.url),'utf8');
const login=readFileSync(new URL('../app/login/page.tsx',import.meta.url),'utf8');
test('payment settings redirects only on HTTP 401',()=>{assert.match(payment,/response.status===401/);assert.match(payment,/reason=session-expired/);assert.match(payment,/paymentProfiles.upis\(authFetch\)/);assert.match(payment,/paymentProfiles.banks\(authFetch\)/);});
test('login explains expiry and validates local return path',()=>{assert.match(login,/Your session has expired/);assert.match(login,/next.startsWith\('\/'\)/);assert.match(login,/next.startsWith\('\/\/'\)/);});
