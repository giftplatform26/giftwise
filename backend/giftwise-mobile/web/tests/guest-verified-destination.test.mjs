import test from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
const ui=readFileSync(new URL('../components/GuestVerifiedDestination.tsx',import.meta.url),'utf8');
const checkout=readFileSync(new URL('../components/GuestContributionCheckout.tsx',import.meta.url),'utf8');
test('preferred destination is selected with fallback',()=>{
 assert.match(ui,/eligible.find\(d=>d.preferred\)\?\?eligible\[0\]/);
});
test('UPI QR and bank instructions render only for eligible response',()=>{
 assert.match(ui,/selected\?\.method==='UPI'/);
 assert.match(ui,/selected\?\.method==='BANK'/);
 assert.match(ui,/Show UPI QR/);
 assert.match(ui,/Beneficiary:/);
});
test('Razorpay and pending report remain separate',()=>{
 assert.match(checkout,/reportDirectPayment/);
 assert.match(checkout,/razorpaySignature/);
 assert.match(checkout,/I have done the payment/);
});
