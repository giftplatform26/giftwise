import test from 'node:test';
import assert from 'node:assert/strict';
import {normalizeMobile,validateRegistration} from '../lib/host-api.mjs';

test('Indian mobiles typed without a country code get +91',()=>{
 for(const input of ['9994030203','09994030203','919994030203','+919994030203','99940 30203','+91 99940-30203'])assert.equal(normalizeMobile(input),'+919994030203',input);
});
test('other international numbers are kept as typed',()=>assert.equal(normalizeMobile('+447911123456'),'+447911123456'));
test('invalid numbers are rejected',()=>{for(const input of ['','12345','5994030203','99940302031','abc'])assert.equal(normalizeMobile(input),'',input);});
test('registration sends the international form',()=>{
 const out=validateRegistration({fullName:'Anjali Nair',username:'validuser',email:'a@b.co',mobile:'9994030203',password:'Abcdef12345!',termsAccepted:true});
 assert.equal(out.mobile,'+919994030203');
});
