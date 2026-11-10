import test from 'node:test';
import assert from 'node:assert/strict';
import {FLASH_KEY,validateFlash,saveFlash,consumeFlash} from '../lib/flash.mjs';
function store(){const data=new Map();return {getItem:k=>data.get(k)??null,setItem:(k,v)=>data.set(k,v),removeItem:k=>data.delete(k)};}
test('all notification variants validate',()=>{for(const type of ['success','error','warning','info'])assert.equal(validateFlash(type,' Hello ').message,'Hello');});
test('unknown variants and empty/oversized messages rejected',()=>{assert.throws(()=>validateFlash('html','Hi'));assert.throws(()=>validateFlash('success',' '));assert.throws(()=>validateFlash('success','x'.repeat(241)));});
test('notification survives redirect and is consumed exactly once',()=>{const s=store();assert.equal(saveFlash(s,'success','Account created'),true);assert.deepEqual(consumeFlash(s),{type:'success',message:'Account created'});assert.equal(consumeFlash(s),null);});
test('later flash replaces earlier one',()=>{const s=store();saveFlash(s,'info','First');saveFlash(s,'error','Second');assert.equal(consumeFlash(s).message,'Second');});
test('invalid stored JSON safely discarded',()=>{const s=store();s.setItem(FLASH_KEY,'not-json');assert.equal(consumeFlash(s),null);assert.equal(s.getItem(FLASH_KEY),null);});
test('malicious variant in storage rejected',()=>{const s=store();s.setItem(FLASH_KEY,JSON.stringify({type:'unknown',message:'Oops'}));assert.equal(consumeFlash(s),null);});
test('storage unavailable does not block login/registration',()=>{const s={getItem(){throw Error('denied')},setItem(){throw Error('denied')},removeItem(){throw Error('denied')}};assert.equal(saveFlash(s,'success','OK'),false);assert.equal(consumeFlash(s),null);});
