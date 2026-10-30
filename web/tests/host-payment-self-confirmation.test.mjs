import test from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
const dash=readFileSync(new URL('../app/dashboard/page.tsx',import.meta.url),'utf8');
const host=readFileSync(new URL('../app/host/payment-settings/page.tsx',import.meta.url),'utf8');
const guest=readFileSync(new URL('../components/GuestVerifiedDestination.tsx',import.meta.url),'utf8');
test('host payment facilities is in shared workspace navigation',()=>{const nav=readFileSync(new URL('../app/components/HostGlobalNav.tsx',import.meta.url),'utf8');assert.match(nav,/href:'\/host\/payment-settings',label:'Payment Facilities'/);});
test('host confirmation does not claim independent verification',()=>{assert.match(host,/Confirm my details/);assert.match(host,/Independent verification is still required/)});
test('UPI QR popup is gated by an eligible selected destination',()=>{assert.match(guest,/selected\?\.method==='UPI'&&selected\.qrPngBase64/);assert.match(guest,/aria-modal="true"/)});
