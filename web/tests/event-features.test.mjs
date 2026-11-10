import test from 'node:test';
import assert from 'node:assert/strict';
import {normalizeFeatures,fetchEventFeatures} from '../lib/event-features.mjs';
const token='123e4567-e89b-12d3-a456-426614174000';
test('missing and malformed feature flags never enable guest features',()=>{
 for(const input of [null,undefined,{}, {GROUP_GIFTING:'true',WHATSAPP_SHARING:1}])assert.ok(Object.values(normalizeFeatures(input)).every(x=>x===false));
});
test('guest features preserve explicit server grants and denials',async()=>{
 let request;const features=await fetchEventFeatures(async(url,options)=>{request={url,options};return {ok:true,json:async()=>({GROUP_GIFTING:false,WHATSAPP_SHARING:false,RSVP:true})}},token);
 assert.equal(features.GROUP_GIFTING,false);assert.equal(features.WHATSAPP_SHARING,false);assert.equal(features.RSVP,true);assert.equal(request.options.cache,'no-store');assert.equal(request.url,`/backend/api/public/events/${token}/features`);
});
test('feature request failures do not fall back to enabled modules',async()=>{
 await assert.rejects(fetchEventFeatures(async()=>({ok:false}),token));
 await assert.rejects(fetchEventFeatures(()=>{throw Error('Must not fetch')},'invalid'));
});

test('new celebration features require explicit grants and gifting respects server denial',()=>{
 const flags=normalizeFeatures({GIFTING_ENABLED:false,CELEBRATION_PLANNING:true,EVENT_ESSENTIALS:'true',EVENT_REUSE:true});
 assert.equal(flags.GIFTING_ENABLED,false);assert.equal(flags.CELEBRATION_PLANNING,true);assert.equal(flags.EVENT_ESSENTIALS,false);assert.equal(flags.EVENT_REUSE,true);
 assert.equal(normalizeFeatures({GIFTING_ENABLED:true}).GIFTING_ENABLED,true);
});
