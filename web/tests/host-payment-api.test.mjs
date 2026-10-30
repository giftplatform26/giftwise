import test from 'node:test';import assert from 'node:assert/strict';import {paymentProfiles,validateUpi,validateBank,maskedBank} from '../lib/host-payment-api.mjs';
const id='123e4567-e89b-42d3-a456-426614174000';
test('UPI validation',()=>{assert.equal(validateUpi(' user@bank ').upiId,'user@bank');assert.throws(()=>validateUpi('not-upi'));});
test('bank validation and IFSC normalization',()=>{assert.equal(validateBank({accountNumber:'1234567890',ifsc:'hdfc0001234',accountHolderName:'Host Name'}).ifsc,'HDFC0001234');assert.throws(()=>validateBank({accountNumber:'1',ifsc:'bad',accountHolderName:'A'}));});
test('GET UPI includes session credentials',async()=>{let args;await paymentProfiles.upis(async(...a)=>{args=a;return {ok:true,json:async()=>[]};});assert.equal(args[0],'/backend/api/v1/host/payment-profiles/upi');assert.equal(args[1].credentials,'include');});
test('GET banks includes session credentials',async()=>{let args;await paymentProfiles.banks(async(...a)=>{args=a;return {ok:true,json:async()=>[]};});assert.equal(args[1].cache,'no-store');});
test('write includes CSRF and validated body',async()=>{let args;await paymentProfiles.addUpi(async(...a)=>{args=a;return {ok:true,status:201,json:async()=>({})};},'user@bank','XSRF-TOKEN=abc');assert.equal(args[1].headers['X-XSRF-TOKEN'],'abc');assert.equal(JSON.parse(args[1].body).upiId,'user@bank');});
test('write without CSRF never calls backend',async()=>{let calls=0;await assert.rejects(paymentProfiles.addUpi(async()=>{calls++;},'user@bank',''),/token missing/);assert.equal(calls,0);});
test('invalid destination rejected before network',async()=>{let calls=0;assert.throws(()=>paymentProfiles.preferUpi(async()=>{calls++;},'../admin','XSRF-TOKEN=x'),/Invalid destination/);assert.equal(calls,0);});
test('preferred bank route',async()=>{let path;await paymentProfiles.preferBank(async(u)=>{path=u;return {ok:true,json:async()=>({})};},id,'XSRF-TOKEN=x');assert.equal(path,`/backend/api/v1/host/payment-profiles/banks/${id}/preferred`);});
test('feature gate 404 is explicit',async()=>{await assert.rejects(paymentProfiles.upis(async()=>({ok:false,status:404})),/unavailable/);});
test('never expose raw account number from API',()=>{assert.equal(maskedBank({accountNumber:'123456789012'}),'Account details protected');assert.equal(maskedBank({maskedAccountNumber:'XXXX1234'}),'XXXX1234');});
test('QR upload rejects non PNG',async()=>{assert.throws(()=>paymentProfiles.uploadQr(async()=>{},id,new Blob(['x'],{type:'text/plain'}),'XSRF-TOKEN=x'),/PNG/);});
test('QR generate requires verified response from server',async()=>{await assert.rejects(paymentProfiles.qrImage(async()=>({ok:false,status:409}),id,'generate','XSRF-TOKEN=x'),/UPI account is unavailable for QR generation/);});

