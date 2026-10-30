import test from 'node:test';
import assert from 'node:assert/strict';
import {signupEmailState} from '../lib/signup-email.mjs';
test('empty and malformed addresses never show verified',()=>{
 for(const email of ['', 'invalid', 'a@@example.com','a b@example.com','a@example']){
  const state=signupEmailState(email,true);assert.equal(state.validFormat,false);assert.equal(state.verified,false);
 }
});
test('a valid format allows signup and reminds users to verify in Account',()=>{
 const state=signupEmailState('user@example.com');assert.equal(state.validFormat,true);assert.equal(state.verified,false);assert.match(state.message,/verify your email in Account after signup/);
});
test('only explicit verification records confirmed ownership',()=>{
 assert.equal(signupEmailState('user@example.com',true).verified,true);
 for(const value of [false,null,'true',1])assert.equal(signupEmailState('user@example.com',value).verified,false);
});
test('overlong addresses remain invalid even if a prior verification flag is present',()=>{
 assert.equal(signupEmailState('a'.repeat(250)+'@example.com',true).verified,false);
});
