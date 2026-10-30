import test from 'node:test';
import assert from 'node:assert/strict';
import {validateAsset,adminGallery,assetImageUrl} from '../lib/admin-gallery.mjs';

const file={name:'image.png',type:'image/png',size:100};
test('valid asset metadata',()=>{
 assert.equal(validateAsset({category:'BACKGROUND',occasion:'BIRTHDAY',title:'  Party  ',file}).title,'Party');
});
test('rejects invalid categories, occasions, title, files and size',()=>{
 for(const changes of [{category:'UNKNOWN'},{occasion:'bad occasion'},{title:' '},{file:null},{file:{type:'text/plain',size:3}},{file:{type:'image/png',size:2097153}}]){
  assert.throws(()=>validateAsset({category:'BACKGROUND',occasion:'BIRTHDAY',title:'Party',file,...changes}));
 }
});
test('admin list uses cookie credentials',async()=>{
 const calls=[];
 const result=await adminGallery.list(async(url,opts)=>{calls.push({url,opts});return {ok:true,json:async()=>[]};});
 assert.deepEqual(result,[]);
 assert.equal(calls[0].url,'/backend/api/admin/invitation-assets');
 assert.equal(calls[0].opts.credentials,'include');
});
test('publish and unpublish send CSRF header',async()=>{
 const calls=[];
 const fetcher=async(url,opts)=>{calls.push({url,opts});return {ok:true,json:async()=>({published:true})};};
 const id='11111111-1111-1111-1111-111111111111';
 await adminGallery.setPublished(fetcher,id,true,{bootstrap:false,cookie:'XSRF-TOKEN=test123'});
 await adminGallery.setPublished(fetcher,id,false,{bootstrap:false,cookie:'XSRF-TOKEN=test123'});
 assert.match(calls[0].url,/\/publish$/);
 assert.match(calls[1].url,/\/unpublish$/);
 assert.equal(calls[0].opts.headers['X-XSRF-TOKEN'],'test123');
});
test('rejects malformed action identifiers',async()=>{
 await assert.rejects(adminGallery.setPublished(async()=>({ok:true}), '../admin',true,{bootstrap:false,cookie:'XSRF-TOKEN=abc'}));
});
test('image URL is admin-only endpoint',()=>assert.equal(assetImageUrl('abc'),'/backend/api/admin/invitation-assets/abc/image'));
