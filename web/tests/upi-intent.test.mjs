import test from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
import {detectPlatform,isSafeUpiUri,upiLaunchOptions,parseUpiResponse,upiStatusCopy,readStoredAttempt,storeAttempt,clearStoredAttempt,attemptStorageKey,screenshotProblem} from '../lib/upi-intent.mjs';

const uri='upi://pay?pa=asha@okhdfc&pn=Asha%20Rao&am=2500.00&cu=INR&tr=GWTABCDEFGHJK2345&tn=GiftWise%20GW-7K3P9Q';
const memory=()=>{const m=new Map();return {getItem:k=>m.has(k)?m.get(k):null,setItem:(k,v)=>m.set(k,String(v)),removeItem:k=>m.delete(k)}};

test('platform detection: Android, iPhone, iPadOS desktop UA, desktop',()=>{
  assert.equal(detectPlatform({userAgent:'Mozilla/5.0 (Linux; Android 14; Pixel 8) Chrome/126 Mobile'}),'ANDROID');
  assert.equal(detectPlatform({userAgent:'Mozilla/5.0 (iPhone; CPU iPhone OS 18_0 like Mac OS X) Safari'}),'IOS');
  assert.equal(detectPlatform({userAgent:'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) Safari',maxTouchPoints:5}),'IOS');
  assert.equal(detectPlatform({userAgent:'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) Safari',maxTouchPoints:0}),'DESKTOP');
  assert.equal(detectPlatform({userAgent:'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/126'}),'DESKTOP');
  assert.equal(detectPlatform(),'DESKTOP');
});

test('only server-built upi://pay links are used as link targets',()=>{
  assert.equal(isSafeUpiUri(uri),true);
  for(const bad of ['javascript:alert(1)','https://evil.example/pay?pa=x','upi://pay?pa=x y','upi://pay?pa=x"onclick=1','upi://mandate?pa=x',null,'upi://pay?'+'a'.repeat(1100)])
    assert.equal(isSafeUpiUri(bad),false,String(bad));
});

test('Android hands one generic upi:// link to the OS chooser',()=>{
  const options=upiLaunchOptions(uri,'ANDROID');
  assert.equal(options.length,1);assert.equal(options[0].href,uri);
});

test('iPhone offers app-specific links plus a generic one, all carrying the same server parameters',()=>{
  const options=upiLaunchOptions(uri,'IOS');
  assert.deepEqual(options.map(o=>o.id),['gpay','phonepe','paytm','any']);
  const query=uri.slice('upi://pay?'.length);
  for(const o of options)assert.ok(o.href.endsWith(query),o.id);
  assert.equal(options.at(-1).href,uri);
});

test('desktop and unsafe links get no app launch (QR is shown instead)',()=>{
  assert.deepEqual(upiLaunchOptions(uri,'DESKTOP'),[]);
  assert.deepEqual(upiLaunchOptions('javascript:alert(1)','ANDROID'),[]);
});

test('UPI app responses are parsed for pre-fill only',()=>{
  const ok=parseUpiResponse('txnId=AXI123&responseCode=00&Status=SUCCESS&txnRef=GWTABCDEFGHJK2345&ApprovalRefNo=412345678901');
  assert.deepEqual(ok,{status:'SUCCESS',txnId:'AXI123',txnRef:'GWTABCDEFGHJK2345',utr:'412345678901'});
  assert.equal(parseUpiResponse('Status=FAILURE&responseCode=ZD').status,'FAILURE');
  assert.equal(parseUpiResponse('status=submitted').status,'SUBMITTED');
  assert.equal(parseUpiResponse('Status=SUCCESS&ApprovalRefNo=ABC').utr,null,'non 12-digit approval refs are not UTRs');
});

test('missing or invalid callback responses are ignored',()=>{
  for(const bad of ['',null,'garbage','Status=SUCCESS&Status=FAILURE','a=%E0%A4%A','x\u0000=1','=1'])assert.equal(parseUpiResponse(bad),null,String(bad));
});

test('status copy never shows unverified payments as green success',()=>{
  for(const s of ['INITIATED','PENDING','FAILED','PAYMENT_REPORTED','HOST_CONFIRMED','REJECTED','EXPIRED',undefined])
    assert.notEqual(upiStatusCopy(s).tone,'success',String(s));
  assert.equal(upiStatusCopy('INDEPENDENTLY_VERIFIED').tone,'success');
  assert.equal(upiStatusCopy(undefined).label,'Payment not initiated');
  assert.equal(upiStatusCopy('INITIATED').label,'Payment app opened');
  assert.equal(upiStatusCopy('PENDING').label,'Waiting for payment confirmation');
  assert.match(upiStatusCopy('PAYMENT_REPORTED').label,/awaiting host verification/);
  assert.equal(upiStatusCopy('HOST_CONFIRMED').label,'Host confirmed receipt');
});

test('attempt is remembered so a guest returning from the UPI app can report it',()=>{
  const s=memory(),token='0f8fad5b-d9cb-469f-a165-70867728950e',id='7c9e6679-7425-40de-944b-e07fc1f90ae7';
  assert.equal(readStoredAttempt(s,token),null);
  storeAttempt(s,token,id,true);
  const back=readStoredAttempt(s,token);assert.equal(back.attemptId,id);assert.equal(back.launched,true);
  s.setItem(attemptStorageKey(token),JSON.stringify({attemptId:'not-a-uuid',savedAt:Date.now()}));assert.equal(readStoredAttempt(s,token),null);
  s.setItem(attemptStorageKey(token),JSON.stringify({attemptId:id,savedAt:Date.now()-25*3600*1000}));assert.equal(readStoredAttempt(s,token),null,'stale');
  s.setItem(attemptStorageKey(token),'{broken');assert.equal(readStoredAttempt(s,token),null);
  clearStoredAttempt(s,token);assert.equal(s.getItem(attemptStorageKey(token)),null);
  assert.equal(readStoredAttempt({getItem(){throw Error('blocked')}},token),null,'private mode');
  assert.doesNotThrow(()=>storeAttempt({setItem(){throw Error('full')}},token,id));
});

test('screenshot upload accepts PNG/JPEG up to 5 MB',()=>{
  assert.equal(screenshotProblem(null),'');
  assert.equal(screenshotProblem({type:'image/png',size:1000}),'');
  assert.equal(screenshotProblem({type:'image/jpeg',size:5_000_000}),'');
  assert.match(screenshotProblem({type:'image/gif',size:10}),/PNG or JPEG/);
  assert.match(screenshotProblem({type:'image/svg+xml',size:10}),/PNG or JPEG/);
  assert.match(screenshotProblem({type:'image/png',size:5_000_001}),/5 MB/);
});

const ui=readFileSync(new URL('../components/UpiIntentPayment.tsx',import.meta.url),'utf8');
const checkout=readFileSync(new URL('../components/GuestContributionCheckout.tsx',import.meta.url),'utf8');
const focused=readFileSync(new URL('../components/focused/Checkout.tsx',import.meta.url),'utf8');
const css=readFileSync(new URL('../components/upi-intent.css',import.meta.url),'utf8');

test('guest flow: report question, manual UTR, review before submit, honest final message',()=>{
  for(const text of ['Did you complete your payment?','Yes, payment completed','Payment pending','Payment failed','Review details','Edit details',
    'Your payment has been reported. The host will verify receipt.','UPI app didn’t open?','Copy UPI ID','switch back to this browser'])
    assert.ok(ui.includes(text),text);
  assert.doesNotMatch(ui,/payment (successful|received)|gift is confirmed/i);
});

test('frontend never sends the amount or payee with the report; the server attempt fixes them',()=>{
  const report=ui.slice(ui.indexOf('async function submitReport'),ui.indexOf('async function refresh'));
  assert.doesNotMatch(report,/amountPaise|destinationId|upiId|paymentCode/);
  assert.match(ui,/X-XSRF-TOKEN/);
});

test('both checkouts offer the UPI app flow and keep manual reporting available',()=>{
  for(const source of [checkout,focused]){
    assert.match(source,/<UpiIntentPayment /);
    assert.match(source,/Already paid another way\? Report it with your UPI reference/);
    assert.match(source,/onUnavailable=\{\(\)=>setManualUpi\(true\)\}/);
  }
});

test('layout is mobile-first with large tap targets and no green for pending states',()=>{
  assert.match(css,/\.upiIntentPrimary,\.upiIntentSecondary\{display:block;width:100%/);
  assert.match(css,/min-height:48px/);
  assert.match(css,/tone-pending span\{background:#fff1d6/);
});
