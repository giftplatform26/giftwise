import test from 'node:test';
import assert from 'node:assert/strict';
import {hostRequest} from '../lib/host-api.mjs';

test('profile-name retries only when CSRF cookie rotates', async () => {
 const prior=globalThis.document;
 let cookie='XSRF-TOKEN=old';
 globalThis.document={get cookie(){return cookie;}};
 const calls=[];
 try {
  const fetcher=async (url,options)=>{
   calls.push({url,method:options.method,token:options.headers?.['X-XSRF-TOKEN']});
   if(url.endsWith('/csrf')){cookie='XSRF-TOKEN=new';return {ok:true,status:204};}
   if(calls.filter(c=>c.url.endsWith('/profile-name')).length===1)return {ok:false,status:403};
   return {ok:true,status:200,json:async()=>({fullName:'Example'})};
  };
  const result=await hostRequest(fetcher,'/auth/profile-name',{method:'POST',payload:{fullName:'Example'},bootstrap:false});
  assert.equal(result.fullName,'Example');
  assert.deepEqual(calls.filter(c=>c.url.endsWith('/profile-name')).map(c=>c.token),['old','new']);
 } finally {globalThis.document=prior;}
});

test('does not replay event creation on 403', async()=>{
 const prior=globalThis.document;
 globalThis.document={cookie:'XSRF-TOKEN=old'};
 let calls=0;
 try {
  await assert.rejects(hostRequest(async()=>{calls++;return {ok:false,status:403};},'/v1/events',{method:'POST',payload:{title:'Test'},bootstrap:false}),/403|Access denied/);
  assert.equal(calls,1);
 } finally {globalThis.document=prior;}
});
