import test from 'node:test';
import assert from 'node:assert/strict';
import {brandAsset,contrastText} from '../lib/branding.mjs';
import {readConsent,saveConsent,CONSENT_KEY} from '../lib/cookie-consent.mjs';
test('branding only loads controlled public image paths',()=>{for(const value of ['https://evil.example/logo','javascript:alert(1)','//evil.example/a','/api/public/branding/assets/../secret'])assert.equal(brandAsset(value),'/giftwise-icon.svg');assert.equal(brandAsset('/api/public/branding/assets/00000000-0000-0000-0000-000000000001'),'/backend/api/public/branding/assets/00000000-0000-0000-0000-000000000001')});
test('brand buttons choose readable text for dark and light colors',()=>{assert.equal(contrastText('#000000'),'#ffffff');assert.equal(contrastText('#ffffff'),'#111111')});
test('optional analytics requires explicit versioned choice and supports withdrawal',()=>{const values=new Map();const storage={getItem:k=>values.get(k)||null,setItem:(k,v)=>values.set(k,v)};assert.equal(readConsent(storage),null);values.set(CONSENT_KEY,'{"analytics":true}');assert.equal(readConsent(storage),null);saveConsent(true,storage);assert.equal(readConsent(storage).analytics,true);saveConsent(false,storage);assert.equal(readConsent(storage).analytics,false)});
test('unavailable browser storage never implies analytics consent',()=>assert.equal(readConsent({getItem(){throw Error('blocked')}}),null));
