import test from 'node:test';
import assert from 'node:assert/strict';
import {
  occasionTemplates,
  createOccasionDesign,
  validateDesign,
  initialDesign
} from '../lib/design.mjs';

const extras = [
  'ENGAGEMENT','GRADUATION','FESTIVAL','RETIREMENT',
  'FIRST_BIRTHDAY','MILESTONE_BIRTHDAY','REUNION','THANK_YOU'
];

test('eight new templates exist',()=>{
  for(const key of extras)assert.ok(occasionTemplates[key]);
});

test('all new templates preserve mandatory QR',()=>{
  for(const key of extras){
    const d=createOccasionDesign(key);
    assert.equal(validateDesign(d),true,key);
    assert.equal(d.elements.filter(e=>e.id==='qr').length,1);
  }
});

test('event title is retained in every template',()=>{
  for(const key of extras){
    const d=createOccasionDesign(key,{title:'Our Event'});
    assert.equal(
      d.elements.find(e=>e.id==='title').text,
      'Our Event'
    );
  }
});

test('template generation does not mutate baseline',()=>{
  const before=JSON.stringify(initialDesign);
  for(const key of extras)createOccasionDesign(key);
  assert.equal(JSON.stringify(initialDesign),before);
});
