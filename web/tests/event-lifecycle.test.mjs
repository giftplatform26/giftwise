import test from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
import {hostApi,OCCASIONS} from '../lib/host-api.mjs';
import {OCCASION_DETAILS,STATUS_DETAILS,lifecycleActions,occasionIcon,occasionTheme} from '../lib/event-labels.mjs';

const enumValues=path=>[...readFileSync(new URL(path,import.meta.url),'utf8').matchAll(/^\s*([A-Z_]+)\s*[,}]?\s*$/gm)].map(m=>m[1]);
const id='aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa';
const valid={title:'Party',occasionType:'BIRTHDAY',hostName:'Asha',addressLine1:'10 Main Street',city:'Chennai',state:'Tamil Nadu',postalCode:'600001',country:'India',contactName:'Asha',contactPhone:'+919876543210'};

test('every backend occasion has an icon, label and theme',()=>{
  const backend=enumValues('../../backend/src/main/java/com/giftplatform/event/entity/OccasionType.java');
  assert.deepEqual(backend,OCCASIONS);
  assert.deepEqual(Object.keys(OCCASION_DETAILS),backend);
  for(const type of backend){assert.ok(occasionIcon(type));assert.ok(occasionTheme(type));}
});
test('every backend status has a label',()=>{
  assert.deepEqual(Object.keys(STATUS_DETAILS),enumValues('../../backend/src/main/java/com/giftplatform/event/entity/EventStatus.java'));
});
test('lifecycle actions mirror backend transitions',()=>{
  assert.deepEqual(lifecycleActions('DRAFT'),{canEdit:true,canComplete:false,canCancel:true});
  assert.deepEqual(lifecycleActions('PUBLISHED'),{canEdit:true,canComplete:true,canCancel:true});
  assert.deepEqual(lifecycleActions('COMPLETED'),{canEdit:false,canComplete:false,canCancel:false});
  assert.deepEqual(lifecycleActions('CANCELLED'),{canEdit:false,canComplete:false,canCancel:false});
});

async function withCsrf(run){const original=globalThis.document;globalThis.document={cookie:'XSRF-TOKEN=abc'};try{return await run()}finally{globalThis.document=original}}
function recorder(final){const calls=[];const fetcher=async(path,opts)=>{calls.push([path,opts]);return path.endsWith('/csrf')?{ok:true,status:204}:final};return {calls,fetcher};}

test('updateEvent sends a validated PUT for the event',()=>withCsrf(async()=>{
  const {calls,fetcher}=recorder({ok:true,status:200,json:async()=>({id})});
  await hostApi.updateEvent(fetcher,id,{...valid,title:'  Party  '});
  const [path,opts]=calls.at(-1);
  assert.equal(path,`/backend/api/v1/events/${id}`);assert.equal(opts.method,'PUT');assert.equal(opts.headers['X-XSRF-TOKEN'],'abc');
  assert.equal(JSON.parse(opts.body).title,'Party');
}));
test('updateEvent rejects invalid ids and details before calling the API',async()=>{
  let calls=0;const fetcher=async()=>{calls++;return {ok:true,status:200}};
  assert.throws(()=>hostApi.updateEvent(fetcher,'../admin',valid),/Invalid event identifier/);
  assert.throws(()=>hostApi.updateEvent(fetcher,id,{...valid,title:' '}),/title/);
  assert.equal(calls,0);
});
test('complete and cancel POST to their lifecycle routes',()=>withCsrf(async()=>{
  for(const [call,suffix] of [[hostApi.completeEvent,'complete'],[hostApi.cancelEvent,'cancel']]){
    const {calls,fetcher}=recorder({ok:true,status:200,json:async()=>({id})});
    await call(fetcher,id);
    assert.deepEqual([calls.at(-1)[0],calls.at(-1)[1].method],[`/backend/api/v1/events/${id}/${suffix}`,'POST']);
  }
}));
test('host sees the server reason for conflicts, with a generic fallback',()=>withCsrf(async()=>{
  const reason='Add a preferred receiving account before publishing';
  await assert.rejects(()=>hostApi.publishEvent(recorder({ok:false,status:409,json:async()=>({error:reason})}).fetcher,id),new RegExp(reason));
  await assert.rejects(()=>hostApi.publishEvent(recorder({ok:false,status:409,json:async()=>{throw Error('no body')}}).fetcher,id),/Conflicting or duplicate details/);
  await assert.rejects(()=>hostApi.publishEvent(recorder({ok:false,status:500,json:async()=>({error:'stack trace'})}).fetcher,id),/HTTP 500/);
}));

test('deleteAccount posts password with explicit confirmation and refuses empty passwords',()=>withCsrf(async()=>{
  let calls=0;assert.throws(()=>hostApi.deleteAccount(async()=>{calls++;return {ok:true,status:204}},''),/password/);assert.equal(calls,0);
  const {calls:seen,fetcher}=recorder({ok:true,status:204});
  assert.equal(await hostApi.deleteAccount(fetcher,'secret'),null);
  const [path,opts]=seen.at(-1);
  assert.equal(path,'/backend/api/auth/profile/delete-account');assert.equal(opts.method,'POST');
  assert.deepEqual(JSON.parse(opts.body),{password:'secret',confirmation:'DELETE'});
  await assert.rejects(()=>hostApi.deleteAccount(recorder({ok:false,status:409,json:async()=>({error:'You have 1 live celebration.'})}).fetcher,'secret'),/1 live celebration/);
}));
