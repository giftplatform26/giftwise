import test from 'node:test';
import assert from 'node:assert/strict';
import {normalizeGrowth,GROWTH_FEATURES,applyOccasionStarter,occasionStarters,giftThumbnail} from '../lib/growth.mjs';
import {existsSync} from 'node:fs';
import {fileURLToPath} from 'node:url';
test('unknown or unavailable feature settings fail closed',()=>{
 assert.equal(Object.keys(normalizeGrowth(null)).length,Object.keys(GROWTH_FEATURES).length);
 assert.ok(Object.values(normalizeGrowth(null)).every(x=>x===false));
 assert.equal(normalizeGrowth({features:{PUBLIC_DEMO:'true'}}).PUBLIC_DEMO,false);
 assert.equal(normalizeGrowth({features:{PUBLIC_DEMO:true}}).PUBLIC_DEMO,true);
 assert.equal(normalizeGrowth({features:{UNKNOWN:true}}).UNKNOWN,undefined);
});
test('occasion suggestions preserve host-written wording and theme',()=>{
 const custom={occasionType:'BIRTHDAY',title:'Maya turns five',description:'Our family wording',theme:'ELEGANT_GOLD'};
 const result=applyOccasionStarter(custom,'WEDDING',true);
 assert.equal(result.title,custom.title);assert.equal(result.description,custom.description);assert.equal(result.theme,custom.theme);
 const suggested={occasionType:'BIRTHDAY',...occasionStarters.BIRTHDAY};
 const changed=applyOccasionStarter(suggested,'WEDDING',true);
 assert.equal(changed.title,occasionStarters.WEDDING.title);assert.equal(changed.description,occasionStarters.WEDDING.description);
});
test('disabled suggestions change only the occasion',()=>{
 const before={occasionType:'BIRTHDAY',title:'',description:'',theme:'MODERN_MINIMAL'};
 assert.deepEqual(applyOccasionStarter(before,'WEDDING',false),{...before,occasionType:'WEDDING'});
});
test('every occasion starter uses an existing illustration',()=>{
 for(const starter of Object.values(occasionStarters))assert.ok(existsSync(fileURLToPath(new URL('../public/invitation-backgrounds/'+starter.art+'.svg',import.meta.url))),starter.art);
});
test('thumbnail requests are restricted to the authorized gift-photo route',()=>{
 const url='/backend/api/public/events/11111111-1111-1111-1111-111111111111/gifts/22222222-2222-2222-2222-222222222222/photo';
 assert.equal(giftThumbnail(url),url+'?thumbnail=true');
 for(const other of ['https://private.example/image','/backend/api/public/guestbook/photo','/image.svg',url+'?x=1'])assert.equal(giftThumbnail(other),other);
});
