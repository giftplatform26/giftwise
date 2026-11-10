import test from 'node:test';
import assert from 'node:assert/strict';
import {addSticker,validStickerElement} from '../lib/invitation-stickers.mjs';
import {platformDecorationId} from '../lib/platform-decorations.mjs';
import {initialDesign} from '../lib/design.mjs';
const id='b2be22ab-903c-4a7b-8010-d1d47a13dd07';
test('owned premium decorations preserve protected URLs in saved designs',()=>{
 const design=addSticker(structuredClone(initialDesign),platformDecorationId(id),true);
 const sticker=design.elements.find(e=>e.type==='sticker');
 assert.ok(sticker);assert.equal(sticker.src,`/backend/api/v1/commerce/templates/${id}/image`);assert.equal(validStickerElement(sticker),true);
 assert.equal(validStickerElement({...sticker,src:`https://evil.example/backend/api/v1/commerce/templates/${id}/image`}),false);
 assert.equal(validStickerElement({...sticker,src:`/backend/api/v1/commerce/templates/${id}/image?bypass=true`}),false);
});
test('public decorations retain their original public image URLs',()=>{
 const design=addSticker(structuredClone(initialDesign),platformDecorationId(id));
 assert.equal(design.elements.find(e=>e.type==='sticker').src,`/backend/api/public/invitation-assets/${id}/image`);
});
