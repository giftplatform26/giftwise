import test from 'node:test';import assert from 'node:assert/strict';import{readFileSync}from'node:fs';
const page=readFileSync(new URL('../components/focused/Invitation.tsx',import.meta.url),'utf8');
const checkout=readFileSync(new URL('../components/GuestContributionCheckout.tsx',import.meta.url),'utf8');
test('gift cards display contribution progress and counts',()=>{for(const v of ['gift value',' recorded','still to go','recorded contributions','focusProgressTrack'])assert.ok(page.includes(v),v)});
test('gift cards open contribution modal',()=>{assert.match(page,/onClick=\{\(\)=>open\(g\)\}/);assert.match(page,/<dialog ref=\{ref\} className="focusCheckoutDialog"/)});
test('completed gifts display love message instead of contribution button',()=>assert.match(page,/fullyGifted&&!preview\?<p className="focusGiftComplete"/));
test('popup locks checkout to selected gift',()=>assert.match(checkout,/!selectedGiftId/));
test('event wallet remains separate',()=>{assert.match(page,/setWallet\(!gift\)/);assert.match(page,/focusWalletChoice/)});
