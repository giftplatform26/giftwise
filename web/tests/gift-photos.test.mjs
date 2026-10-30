import test from 'node:test';
import assert from 'node:assert/strict';
import {giftPhotoUrl,validateGiftPhotoFile,giftPhotoDimensions} from '../lib/gift-photos.mjs';
const event='11111111-1111-4111-8111-111111111111',photo='22222222-2222-4222-8222-222222222222';
test('managed photos use an event-scoped backend URL',()=>assert.equal(giftPhotoUrl(event,`gift-photo:${photo}`),`/backend/api/v1/events/${event}/gifts/photos/${photo}`));
test('arbitrary URLs and invalid references cannot become preview URLs',()=>{for(const key of ['https://elsewhere/image.jpg','../../private','gift-photo:bad',''])assert.equal(giftPhotoUrl(event,key),'')});
test('uploads allow JPEG and PNG within the size limit',()=>{for(const type of ['image/jpeg','image/png','image/webp'])assert.equal(validateGiftPhotoFile({type,size:1000}).type,type)});
test('SVG, empty and oversized uploads are rejected before sending',()=>{for(const file of [{type:'image/svg+xml',size:100},{type:'image/jpeg',size:0},{type:'image/png',size:20000001}])assert.throws(()=>validateGiftPhotoFile(file))});

test('large photos preserve their aspect ratio within 1200 pixels',()=>assert.deepEqual(giftPhotoDimensions(4000,3000),{width:1200,height:900}));
test('small, excessive and panoramic photos are rejected clearly',()=>{for(const size of [[150,150],[10000,5000],[4000,160]])assert.throws(()=>giftPhotoDimensions(...size))});
