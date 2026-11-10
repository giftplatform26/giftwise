import test from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
const ui=readFileSync(new URL('../components/GuestContributionCheckout.tsx',import.meta.url),'utf8');
test('guest report is distinct from provider confirmation',()=>{assert.match(ui,/reportDirectPayment/);assert.doesNotMatch(ui,/AWAITING_VERIFICATION|awaiting verification/i);assert.match(ui,/\/direct\/.*\/claims/);});
test('Razorpay confirmation is retained',()=>assert.match(ui,/razorpaySignature:result.razorpay_signature/));
test('guest submission displays success without claiming provider confirmation',()=>{
  assert.match(ui,/reportDirectPayment/);
  assert.match(ui,/\/direct\/.*\/claims/);
  assert.match(ui,/Your gift details have been submitted successfully/);
  assert.match(ui,/Thank you for sharing your love!/);
  assert.doesNotMatch(ui,/does not verify receipt of funds/i);
});
