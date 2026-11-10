import test from 'node:test';
import assert from 'node:assert/strict';
import {occasionTemplates,createOccasionDesign} from '../lib/design.mjs';
import {validateSafeLayout} from '../lib/invitation-geometry.mjs';
import {filterBackgrounds} from '../lib/background-gallery.mjs';

test('every occasion has automatic artwork, event details and a protected QR',()=>{
 for(const occasion of Object.keys(occasionTemplates)){
  const design=createOccasionDesign(occasion,{title:'Maya’s celebration',eventDate:'2026-12-20',hostName:'Maya & family',eventAddress:{addressLine1:'Celebration Hall',city:'Chennai'}});
  assert.ok(design.backgroundImage);assert.ok(validateSafeLayout(design),occasion);
  const text=design.elements.map(e=>e.text).join(' ');
  for(const value of ['Maya’s celebration','2026-12-20','Maya & family','Celebration Hall','Chennai'])assert.ok(text.includes(value));
 }
});
test('every gallery background creates a complete safe invitation',()=>{
 for(const background of filterBackgrounds('WEDDING')){
  const design=createOccasionDesign('WEDDING',{backgroundImage:background.src});
  assert.equal(design.backgroundImage,background.src);assert.ok(validateSafeLayout(design));
 }
});

test('all stationery compositions keep event information and the QR safe',()=>{for(const layout of ['classic','editorial','storybook','celebration']){const design=createOccasionDesign('WEDDING',{title:'Alex & Maya',eventDate:'2026-12-20',hostName:'Family',layout});assert.ok(validateSafeLayout(design),layout);assert.equal(design.elements.filter(e=>e.type==='qr').length,1);assert.ok(design.elements.some(e=>e.type==='sticker'));assert.ok(design.elements.some(e=>e.id==='event-details'));}});
