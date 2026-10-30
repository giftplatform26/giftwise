import test from 'node:test';
import assert from 'node:assert/strict';

import {
  platformDecorationId,
  validPlatformDecorationElement,
  eligiblePlatformDecorations
} from '../lib/platform-decorations.mjs';

const id='123e4567-e89b-42d3-a456-426614174000';

test('valid platform decoration uses canonical identifier',()=>{
  assert.equal(
    platformDecorationId(id),
    `platform:${id}`
  );
});

test('rejects malformed decoration identifiers',()=>{
  for(const value of ['', '../admin', 'javascript:alert(1)', '123']){
    assert.throws(()=>platformDecorationId(value));
  }
});

test('accepts matching public image endpoint',()=>{
  assert.equal(validPlatformDecorationElement({
    type:'sticker',
    stickerId:`platform:${id}`,
    src:`/backend/api/public/invitation-assets/${id}/image`
  }),true);
});

test('rejects arbitrary external sticker URLs',()=>{
  assert.equal(validPlatformDecorationElement({
    type:'sticker',
    stickerId:`platform:${id}`,
    src:'https://example.com/unsafe.png'
  }),false);
});

test('rejects admin-only image endpoint',()=>{
  assert.equal(validPlatformDecorationElement({
    type:'sticker',
    stickerId:`platform:${id}`,
    src:`/backend/api/admin/invitation-assets/${id}/image`
  }),false);
});

test('filters unpublished and mismatched decorations',()=>{
  const assets=[
    {id,title:'Flowers',category:'DECORATION',occasion:'WEDDING',published:true},
    {id,title:'Hidden',category:'DECORATION',occasion:'WEDDING',published:false},
    {id,title:'Background',category:'BACKGROUND',occasion:'WEDDING',published:true},
    {id,title:'Birthday',category:'DECORATION',occasion:'BIRTHDAY',published:true}
  ];

  assert.deepEqual(
    eligiblePlatformDecorations(assets,'WEDDING').map(a=>a.title),
    ['Flowers']
  );
});

test('OTHER decorations are available for all occasions',()=>{
  assert.equal(eligiblePlatformDecorations([
    {id,title:'Universal',category:'DECORATION',occasion:'OTHER',published:true}
  ],'BIRTHDAY').length,1);
});
