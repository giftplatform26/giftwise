import test from 'node:test';
import assert from 'node:assert/strict';
import {occasionFields,normalizeOccasionDetails,featuredNames,occasionHeadline,invitationTime} from '../lib/occasion-details.mjs';
import {createOccasionDesign} from '../lib/design.mjs';
import {validateSafeLayout} from '../lib/invitation-geometry.mjs';
import {validateEvent,OCCASIONS} from '../lib/host-api.mjs';
import {normalizeFeatures} from '../lib/event-features.mjs';

const valid={title:'Anu weds Rahul',occasionType:'WEDDING',addressLine1:'1 Main St',city:'Kochi',state:'Kerala',postalCode:'682001',country:'India',contactName:'Jiju',contactPhone:'+919876543210',hostName:'Family'};

test('every occasion offers fields, all optional, plus venue and time',()=>{
 for(const occasion of OCCASIONS){const f=occasionFields(occasion);assert.ok(f.who.length>0,occasion);assert.ok(f.keys.includes('venueName'));assert.ok([...f.who,...f.where].every(x=>x.optional))}
});
test('details are trimmed, blank values dropped and other occasions’ keys ignored',()=>{
 assert.deepEqual(normalizeOccasionDetails('WEDDING',{brideName:' Anu ',groomName:'Rahul',celebrantName:'Maya',muhurtham:''}),{brideName:'Anu',groomName:'Rahul'});
 assert.throws(()=>normalizeOccasionDetails('WEDDING',{brideName:'<b>x</b>'}));
 assert.throws(()=>normalizeOccasionDetails('WEDDING',{brideName:'x'.repeat(201)}));
});
test('wedding and birthday wording comes from host details only',()=>{
 assert.equal(featuredNames('WEDDING',{brideName:'Anu',groomName:'Rahul'}),'Anu & Rahul');
 assert.equal(occasionHeadline('WEDDING',{brideName:'Anu',groomName:'Rahul'}),'Anu & Rahul are getting married');
 assert.equal(occasionHeadline('BIRTHDAY',{celebrantName:'Maya',age:'21'}),'Maya’s 21st birthday');
 assert.equal(occasionHeadline('BIRTHDAY',{celebrantName:'Maya',age:'abc'}),'Maya’s birthday');
 assert.equal(occasionHeadline('WEDDING',{}),null);
 assert.equal(invitationTime({muhurtham:'10:30 AM'}),'10:30 AM');
});
test('validateEvent keeps old payloads unchanged and validates the new optional fields',()=>{
 const legacy=validateEvent(valid);assert.ok(!('giftMode' in legacy)&&!('occasionDetails' in legacy));
 const next=validateEvent({...valid,giftMode:'CONTRIBUTION_ONLY',occasionDetails:{brideName:'Anu',age:'5'}});
 assert.equal(next.giftMode,'CONTRIBUTION_ONLY');assert.deepEqual(next.occasionDetails,{brideName:'Anu'});
 assert.throws(()=>validateEvent({...valid,giftMode:'GIFTS_ONLY'}));
});
test('automatic invitation uses wedding details and stays safe',()=>{
 const design=createOccasionDesign('WEDDING',{title:'Our wedding',eventDate:'2026-12-20',hostName:'Families',occasionDetails:{brideName:'Anu',groomName:'Rahul',venueName:'Rosewood Gardens',muhurtham:'10:30 AM',brideFamily:'D/o Thomas',groomFamily:'S/o Menon'}});
 const text=design.elements.map(e=>e.text).join(' ');
 for(const value of ['Anu & Rahul are getting married','Rosewood Gardens','10:30 AM','D/o Thomas','SCAN TO EXPLORE GIFTS'])assert.ok(text.includes(value),value);
 assert.ok(validateSafeLayout(design));
});
test('contribution-only invitations never mention gift items',()=>{
 for(const layout of ['classic','editorial','storybook','celebration']){
  const design=createOccasionDesign('BIRTHDAY',{title:'Maya turns 5',giftMode:'CONTRIBUTION_ONLY',layout,occasionDetails:{celebrantName:'Maya',age:'5'}});
  const text=design.elements.map(e=>e.text).join(' ');
  assert.ok(!/gift/i.test(text),text);assert.ok(text.includes('Maya’s 5th birthday'));assert.ok(validateSafeLayout(design),layout);
 }
});
test('wishlist flag follows gifting and defaults on for older backends',()=>{
 assert.equal(normalizeFeatures({GIFTING_ENABLED:true}).GIFT_WISHLIST,true);
 assert.equal(normalizeFeatures({GIFTING_ENABLED:true,GIFT_WISHLIST:false}).GIFT_WISHLIST,false);
 assert.equal(normalizeFeatures({GIFTING_ENABLED:false}).GIFT_WISHLIST,false);
});
