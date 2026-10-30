import test from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
const checkout=readFileSync(new URL('../components/GroupGuestCheckout.tsx',import.meta.url),'utf8');
const page=readFileSync(new URL('../app/groups/[token]/[groupId]/page.tsx',import.meta.url),'utf8');
test('selecting payment method directly loads destination and opens modal',()=>{
 assert.match(checkout,/onClick=\{\(\)=>void choosePayment\(d.id\)\}/);
 assert.match(checkout,/setModal\(true\)/);
 assert.match(checkout,/setStage\('reference'\)/);
 assert.ok(!checkout.includes('Show payment details →'));
});
test('UPI ID is not displayed in guest UI and bank fields remain modal-only',()=>{
 assert.ok(!checkout.includes('details.upiId'));
 assert.match(checkout,/role=\{focused\?undefined:'dialog'\}/);
 assert.match(checkout,/details.accountNumber/);
 assert.match(checkout,/transactionReference:reference.trim\(\)/);
});
test('guest and member panels are mutually exclusive by section',()=>{
 assert.match(page,/section==='contribute'&&<GroupGuestCheckout/);
 assert.match(page,/section==='membership'/);
 assert.match(page,/accessMode==='BOTH'/);
 assert.match(page,/accessMode!=='GUESTS_ONLY'&&section==='membership'/);
 assert.match(page,/setSection\(a==='MEMBERS_ONLY'\?'membership':'contribute'\)/);
});
