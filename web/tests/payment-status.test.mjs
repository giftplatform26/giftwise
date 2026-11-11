import test from 'node:test';
import assert from 'node:assert/strict';
import {paymentStatus} from '../lib/payment-status.mjs';
test('verified label requires captured checkout and server verification time',()=>{
 assert.equal(paymentStatus({source:'CHECKOUT',status:'CAPTURED',verifiedAt:'2026-10-30'}).label,'Payment verified');
 assert.notEqual(paymentStatus({source:'CHECKOUT',status:'CAPTURED'}).label,'Payment verified');
 assert.notEqual(paymentStatus({source:'CHECKOUT',status:'PENDING',verifiedAt:'2026-10-30'}).label,'Payment verified');
});
test('manual transfer reports cannot be mistaken for verified funds',()=>{
 for(const status of ['REPORTED_UNVERIFIED','PENDING','CAPTURED','VERIFIED']){
 const state=paymentStatus({source:'DIRECT_TRANSFER',status,verifiedAt:'2026-10-30'});
 assert.equal(state.step,1);assert.match(state.label,/Waiting for you to confirm/);assert.notEqual(state.tone,'success');
 }
});
test('host-confirmed transfers say the host confirmed them, never provider verification',()=>{
 const state=paymentStatus({source:'DIRECT_TRANSFER',status:'HOST_CONFIRMED'});
 assert.equal(state.tone,'success');assert.match(state.label,/confirmed by you/);assert.doesNotMatch(state.label,/verified/i);
 assert.equal(paymentStatus({source:'DIRECT_TRANSFER',status:'REJECTED'}).label,'Marked not received');
});
test('refunds and failures do not show payment verified',()=>{
 for(const status of ['REFUNDED','PARTIALLY_REFUNDED','FAILED','CANCELLED','REJECTED'])assert.notEqual(paymentStatus({source:'CHECKOUT',status,verifiedAt:'2026-10-30'}).label,'Payment verified');
});
test('existing orders are not payment confirmations',()=>{
 assert.equal(paymentStatus({source:'CHECKOUT',status:'CREATED'}).label,'Awaiting payment');
 assert.equal(paymentStatus({source:'CHECKOUT',status:'AUTHORIZED'}).label,'Awaiting provider confirmation');
 assert.equal(paymentStatus({source:'CHECKOUT',status:'PENDING',providerPaymentId:'pay_example'}).step,2);
});
