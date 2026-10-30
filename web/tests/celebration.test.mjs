import test from 'node:test';
import assert from 'node:assert/strict';
import {whatsappUrl,guestPost,csrfCookie} from '../lib/celebration.mjs';
const id='123e4567-e89b-42d3-a456-426614174000';
test('WhatsApp share contains encoded title and exact invitation',()=>{const u=new URL(whatsappUrl('https://example.com/e/'+id,'Birthday & Friends'));assert.equal(u.hostname,'wa.me');assert.match(u.searchParams.get('text'),/Birthday & Friends/);assert.match(u.searchParams.get('text'),new RegExp(id));});
test('WhatsApp rejects unsafe protocols',()=>assert.throws(()=>whatsappUrl('javascript:alert(1)','X')));
test('public RSVP posts only expected route',async()=>{let route,opts;await guestPost(async(u,o)=>{route=u;opts=o;return {ok:true,json:async()=>({})}},id,'rsvp',{guestName:'Sam'});assert.equal(route,`/backend/api/public/celebrations/${id}/rsvp`);assert.equal(opts.method,'POST')});
test('rejects invalid token and kind',async()=>{await assert.rejects(guestPost(()=>{},'../admin','rsvp',{}));await assert.rejects(guestPost(()=>{},id,'payment',{}));});
test('csrf cookie parsed',()=>assert.equal(csrfCookie('foo=x; XSRF-TOKEN=a%3Db'),'a=b'));
