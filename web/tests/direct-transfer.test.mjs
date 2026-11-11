import test from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
import {newPaymentCode,PAYMENT_CODE_PATTERN,paymentNote,normaliseReference,referenceProblem,receiptPath,receiptStage} from '../lib/direct-transfer.mjs';
const read=path=>readFileSync(new URL(`../${path}`,import.meta.url),'utf8');

test('payment codes are readable, well-formed and match the backend pattern',()=>{
  for(let i=0;i<200;i++){const code=newPaymentCode();assert.match(code,PAYMENT_CODE_PATTERN);assert.doesNotMatch(code.slice(3),/[01IO]/)}
  assert.equal(newPaymentCode(n=>new Uint8Array(n)),'GW-AAAAAA');
  const backend=read('../backend/src/main/java/com/giftplatform/payment/direct/GuestDirectTransferService.java');
  assert.match(backend,/PAYMENT_CODE="GW-\[A-HJ-NP-Z2-9\]\{6\}"/);
  assert.equal(paymentNote('GW-7K3P9Q'),'GiftWise GW-7K3P9Q');
});
test('UPI references are 12 digits and pasted text is cleaned up',()=>{
  assert.equal(normaliseReference('upi','UPI Ref No: 4123 4567 8901 paid'),'412345678901');
  assert.equal(normaliseReference('bank',' hdfc n520 '),'HDFCN520');
  assert.equal(referenceProblem('upi','412345678901'),'');
  assert.match(referenceProblem('upi','ABC123456789'),/12 digits/);
  assert.match(referenceProblem('upi',''),/12-digit/);
  assert.equal(referenceProblem('bank','HDFCN52026111012345'),'');
  assert.match(referenceProblem('bank','abc'),/8–40/);
});
test('receipt links and stages',()=>{
  assert.equal(receiptPath('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'),'/r/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa');
  assert.equal(receiptPath('../admin'),null);
  assert.deepEqual(receiptStage('HOST_CONFIRMED'),{step:2,tone:'success',label:'Received by the host'});
  assert.equal(receiptStage('AWAITING_VERIFICATION').tone,'pending');
  assert.equal(receiptStage('REJECTED').tone,'warning');
});
test('checkout sends the code, shows it, and offers the receipt',()=>{
  const checkout=read('components/GuestContributionCheckout.tsx'),view=read('components/focused/Checkout.tsx'),dest=read('components/GuestVerifiedDestination.tsx');
  assert.match(checkout,/idempotencyKey:requestKey\.current,paymentCode\}/);
  assert.match(checkout,/const \[paymentCode\]=useState\(\(\)=>newPaymentCode\(\)\)/);
  assert.match(view,/className="directCode"/);assert.match(view,/View your receipt/);assert.match(view,/navigator\.clipboard\.readText/);
  assert.match(dest,/payment-qr\?\$\{query\}/);assert.match(dest,/&tn=\$\{encodeURIComponent\(note\)\}/);
});
test('host can confirm transfers and admins have a Razorpay switch',()=>{
  const tx=read('app/events/[id]/transactions/page.tsx');
  assert.match(tx,/pending=true/);assert.match(tx,/\/claims\/\$\{row\.id\}\/decision/);assert.match(tx,/'RECEIVED'\|'NOT_RECEIVED'\|'UNDECIDED'/);
  const admin=read('app/admin/page.tsx'),sw=read('components/RazorpaySwitch.tsx');
  assert.match(admin,/<RazorpaySwitch /);
  assert.match(sw,/controls\/RAZORPAY\?scope=PLATFORM`,\{revision:view\.revision,enabled:!on,reason:reason\.trim\(\)\}/);
  assert.match(sw,/razorpay-status/);
});
