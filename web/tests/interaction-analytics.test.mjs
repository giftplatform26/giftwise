import test from 'node:test';
import assert from 'node:assert/strict';
import {safeRoute,routeScope,classifyAction} from '../lib/interaction-analytics.mjs';

test('routes exclude public tokens, queries and personally identifiable path values',()=>{
 const token='11111111-1111-1111-1111-111111111111';
 assert.equal(safeRoute(`/e/${token}?email=guest@example.org`),'/e/:id');
 assert.equal(safeRoute('https://payments.example/?secret=123'),'external');
 assert.deepEqual(routeScope(`/groups/${token}/22222222-2222-2222-2222-222222222222`),{token});
 assert.deepEqual(routeScope(`/events/${token}/qr`),{eventId:token});
});
test('important controls receive stable action types without storing their labels',()=>{
 assert.equal(classifyAction({label:'download invitation'}),'INVITATION_DOWNLOAD');
 assert.equal(classifyAction({label:'download qr'}),'QR_DOWNLOAD');
 assert.equal(classifyAction({href:'/groups/abc/def'}),'MEMBER_LINK');
 assert.equal(classifyAction({href:'upi://pay?pa=secret'}),'PAYMENT_LINK');
 assert.equal(classifyAction({href:'/e/abc'}),'INVITATION_LINK');
 assert.equal(classifyAction({label:'Private user supplied name'}),'BUTTON_CLICK');
});

test('real clicks, copy outcomes and request correlation are recorded without clipboard content',async()=>{
 const listeners=new Map(),requests=[],posts=[];
 const original={window:globalThis.window,document:globalThis.document,location:globalThis.location,Element:globalThis.Element,navigator:Object.getOwnPropertyDescriptor(globalThis,'navigator')};
 class Element{tagName='BUTTON';parentElement=null;textContent='Copy link';getAttribute(name){return name==='data-event-id'?null:null}hasAttribute(){return false}closest(selector){return selector==='[data-event-id]'?null:this}matches(){return false}}
 const location=new URL('https://gift.example/events/11111111-1111-1111-1111-111111111111/qr');
 const fetch=async(input,init)=>{requests.push({input,init});if(String(input).includes('/interactions'))posts.push(...JSON.parse(init.body).events);return {ok:true,status:204}};
 globalThis.window={fetch};globalThis.location=location;globalThis.Element=Element;
 globalThis.document={cookie:'XSRF-TOKEN=test',visibilityState:'visible',addEventListener:(n,f)=>listeners.set(n,f),removeEventListener:n=>listeners.delete(n)};
 Object.defineProperty(globalThis,'navigator',{configurable:true,value:{clipboard:{writeText:async()=>{}}}});
 const analytics=await import('../lib/interaction-analytics.mjs?isolated-browser-test');const cleanup=analytics.installInteractionTracking();
 async function flush(){document.visibilityState='hidden';listeners.get('visibilitychange')();await new Promise(resolve=>setImmediate(resolve));document.visibilityState='visible';}
 try{
  // Programmatic link clicks made by downloads must not inflate click totals.
  listeners.get('click')({isTrusted:false,target:new Element()});await flush();assert.equal(posts.length,0);
  listeners.get('click')({isTrusted:true,target:new Element()});
  await window.fetch('/backend/api/v1/events/11111111-1111-1111-1111-111111111111/qr/png');
  await analytics.trackedCopy('https://gift.example/e/22222222-2222-2222-2222-222222222222?private=secret');await flush();
  const click=posts.find(p=>p.kind==='CLICK');const done=posts.find(p=>p.outcome==='SUCCESS');
  assert.ok(done);assert.equal(done.id,click.id);assert.equal(done.action,'INVITATION_LINK_COPY');
  assert.equal(new Set(posts.map(p=>p.id)).size,1);assert.ok(!JSON.stringify(posts).includes('private=secret'));
  const api=requests.find(r=>String(r.input).includes('/qr/png'));assert.equal(api.init.headers.get('X-Gift-Interaction'),click.id);
  assert.equal(api.init.headers.get('X-Gift-Event-Id'),'11111111-1111-1111-1111-111111111111');
  await window.fetch('https://third-party.example/');assert.equal(requests.at(-1).init,undefined);
  listeners.get('click')({isTrusted:true,target:new Element()});listeners.get('invalid')();await flush();
  assert.ok(posts.some(p=>p.outcome==='FAILURE'&&p.detail==='FORM_VALIDATION_BLOCKED'));
  class LinkElement extends Element{tagName='A';getAttribute(key){return key==='href'?'/host/account':null}}
  listeners.get('click')({isTrusted:true,target:new LinkElement()});location.pathname='/host/account';analytics.trackPage();await flush();
  assert.ok(posts.some(p=>p.outcome==='SUCCESS'&&p.detail==='IN_APP_DESTINATION_RENDERED'));
  assert.ok(posts.some(p=>p.kind==='PAGE_VIEW'&&p.parentId));
  navigator.clipboard.writeText=async()=>{throw Error('Denied private detail')};
  await assert.rejects(analytics.trackedCopy('private-bank-account'));await flush();
  assert.ok(posts.some(p=>p.outcome==='FAILURE'));assert.ok(!JSON.stringify(posts).includes('private-bank-account'));assert.ok(!JSON.stringify(posts).includes('Denied private detail'));
 }finally{
  cleanup();for(const key of ['window','document','location','Element']){if(original[key]===undefined)delete globalThis[key];else globalThis[key]=original[key]}
  if(original.navigator)Object.defineProperty(globalThis,'navigator',original.navigator);else delete globalThis.navigator;
 }
});
