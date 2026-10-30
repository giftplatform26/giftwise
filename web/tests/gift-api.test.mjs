import test from 'node:test';
import assert from 'node:assert/strict';
import {eventGiftPath,toGiftRequest,csrfToken,giftApiRequest,cancelGift} from '../lib/gift-api.mjs';
const id='123e4567-e89b-42d3-a456-426614174000';
test('event route uses UUID',()=>assert.equal(eventGiftPath(id),`/backend/api/v1/events/${id}/gifts`));
test('invalid identifiers rejected',()=>assert.throws(()=>eventGiftPath('../admin')));
test('request includes supported retailer product link',()=>assert.deepEqual(toGiftRequest({name:'Coffee',targetAmount:10000,productUrl:'https://example.org',status:'PUBLIC'}),{name:'Coffee',description:'',imageKey:null,targetAmount:'10000.00',fulfillmentOverride:null, priority: 'MEDIUM',productUrl:'https://example.org'}));
test('invalid fulfillment rejected',()=>assert.throws(()=>toGiftRequest({name:'Gift',targetAmount:10,fulfillmentOverride:'UNKNOWN'})));
test('invalid amount rejected',()=>assert.throws(()=>toGiftRequest({name:'Gift',targetAmount:-2})));
test('CSRF cookie decoded',()=>assert.equal(csrfToken('foo=x; XSRF-TOKEN=abc%3D; other=1'),'abc='));
test('authenticated GET sends credentials',async()=>{let options;await giftApiRequest(async(_,o)=>{options=o;return {ok:true,status:200,json:async()=>[]}}, {eventId:id});assert.equal(options.credentials,'include');assert.equal(options.method,'GET')});
test('POST sends CSRF header',async()=>{let options;await giftApiRequest(async(_,o)=>{options=o;return {ok:true,status:201,json:async()=>({})}}, {eventId:id,method:'POST',gift:{name:'Gift',targetAmount:200},cookie:'XSRF-TOKEN=token'});assert.equal(options.headers['X-XSRF-TOKEN'],'token')});
test('403 provides safe error',async()=>assert.rejects(giftApiRequest(async()=>({ok:false,status:403}),{eventId:id}),/Access denied/));
test('cancel uses actual cancel endpoint',async()=>{let url;await cancelGift(async(u)=>{url=u;return {ok:true,json:async()=>({})}},id,id);assert.equal(url,`${eventGiftPath(id,id)}/cancel`)});

test('retailer lookup reference is included on save',()=>assert.equal(toGiftRequest({name:'Gift',targetAmount:19.99,retailerLookupId:'lookup-id'}).retailerLookupId,'lookup-id'));
