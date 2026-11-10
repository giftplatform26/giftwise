import test from 'node:test';
import assert from 'node:assert/strict';
import {eventIdForRoute,restoreEventDraft,occasionArt} from '../lib/focused.mjs';
import {validateEvent} from '../lib/host-api.mjs';
const id='123e4567-e89b-12d3-a456-426614174000';
test('event context resolves both event subpages and wishlist links',()=>{
 assert.equal(eventIdForRoute(`/events/${id}/preview`,null),id);assert.equal(eventIdForRoute('/gifts',id),id);assert.equal(eventIdForRoute('/events/new',id),null);assert.equal(eventIdForRoute('/events/not-an-event',null),null);assert.equal(eventIdForRoute('/admin',id),null);
});
test('restored event drafts retain only expected string fields',()=>{
 const defaults={title:'',country:'India'};assert.deepEqual(restoreEventDraft('{broken',defaults),defaults);assert.deepEqual(restoreEventDraft(JSON.stringify({title:'Our wedding',country:null,privateToken:'ignore'}),defaults),{title:'Our wedding',country:'India'});assert.equal(restoreEventDraft(JSON.stringify({title:'a'.repeat(10001)}),defaults).title,'');
});
test('public host name survives creation validation without exposing private contact data',()=>{
 const value=validateEvent({title:'Our celebration',hostName:' Our family ',occasionType:'BIRTHDAY',addressLine1:'1 Main Road',city:'Kochi',state:'Kerala',postalCode:'682001',country:'India',contactName:'Private organiser',contactPhone:'+919876543210'});assert.equal(value.hostName,'Our family');assert.equal(value.contactName,'Private organiser');
});
test('unsupported occasions use an existing neutral invitation artwork',()=>assert.equal(occasionArt('UNRECOGNISED'),'cream-paper'));
