import test from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
import {paymentProfiles} from '../lib/host-payment-api.mjs';
const id='123e4567-e89b-42d3-a456-426614174000';
for(const [kind,route] of [['Upi','upi'],['Bank','banks']]){
 for(const action of ['enable','disable','confirm']){
  test(`${kind} ${action} uses authenticated POST and CSRF`,async()=>{
   let url,options;
   const name=action==='confirm'?`confirm${kind}`:`${action}${kind}`;
   await paymentProfiles[name](async(u,o)=>{url=u;options=o;return {ok:true,status:200,json:async()=>({})};},id,'XSRF-TOKEN=abc');
   assert.equal(url,`/backend/api/v1/host/payment-profiles/${route}/${id}/${action==='confirm'?'host-confirm':action}`);
   assert.equal(options.method,'POST');assert.equal(options.credentials,'include');assert.equal(options.headers['X-XSRF-TOKEN'],'abc');
  });
 }
}
const page=readFileSync(new URL('../app/host/payment-settings/page.tsx',import.meta.url),'utf8');
test('disabled accounts offer Enable and enabled accounts offer Disable',()=>{assert.match(page,/u\.enabled===false\?<button[^]*?>\{localize\([^]*?defaultValue:"Enable"[^]*?\)\}<\/button>/);assert.match(page,/b\.enabled===false\?<button[^]*?>\{localize\([^]*?defaultValue:"Enable"[^]*?\)\}<\/button>/);});
test('new payment actions retain session-aware fetch',()=>{assert.match(page,/paymentProfiles\.enableUpi\(authFetch/);assert.match(page,/paymentProfiles\.enableBank\(authFetch/);assert.match(page,/paymentProfiles\.confirmUpi\(authFetch/);assert.match(page,/paymentProfiles\.confirmBank\(authFetch/);});
test('preferred requires verified status and is visibly selected',()=>{assert.match(page,/verificationStatus!=='VERIFIED'/);assert.match(page,/paySelected/);assert.match(page,/aria-pressed/);});
