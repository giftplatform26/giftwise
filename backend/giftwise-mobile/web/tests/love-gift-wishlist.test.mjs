import test from 'node:test';import assert from 'node:assert/strict';import{readFileSync}from'node:fs';
const page=readFileSync(new URL('../app/e/[token]/page.tsx',import.meta.url),'utf8');
const checkout=readFileSync(new URL('../components/GuestContributionCheckout.tsx',import.meta.url),'utf8');
test('gift cards display contribution progress and counts',()=>{for(const v of ['Gift Value','Contributions Received','Still to Be Gifted','Contributions Made','loveGiftTrack'])assert.ok(page.includes(v))});
test('gift cards open contribution modal',()=>{assert.match(page,/setSelectedGift\(g\)/);assert.match(page,/role="dialog"/)});
test('completed gifts display love message instead of contribution button',()=>assert.match(page,/remaining===0\?<div className="loveGiftComplete"/));
test('popup locks checkout to selected gift',()=>assert.match(checkout,/!selectedGiftId/));
test('event wallet remains separate',()=>assert.match(page,/setWalletOpen\(true\)/));
