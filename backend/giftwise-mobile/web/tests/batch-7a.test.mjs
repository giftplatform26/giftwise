import test from 'node:test';import assert from 'node:assert/strict';import {readFileSync} from 'node:fs';import {validateEvent} from '../lib/host-api.mjs';
const valid={title:'Birthday',description:'',occasionType:'BIRTHDAY',eventDate:'',addressLine1:'10 Main Street',city:'Chennai',state:'Tamil Nadu',postalCode:'600001',country:'India',contactName:'Host',contactPhone:'+919876543210',contactEmail:'host@example.com'};
test('valid event contact and address',()=>assert.equal(validateEvent(valid).postalCode,'600001'));
test('missing street rejected',()=>assert.throws(()=>validateEvent({...valid,addressLine1:''})));
test('invalid Indian PIN rejected',()=>assert.throws(()=>validateEvent({...valid,postalCode:'000000'})));
test('invalid phone rejected',()=>assert.throws(()=>validateEvent({...valid,contactPhone:'abc'})));
test('invalid email rejected',()=>assert.throws(()=>validateEvent({...valid,contactEmail:'wrong'})));
test('login supports a safe post-authentication redirect',()=>{
 const source=readFileSync('app/login/page.tsx','utf8');
 assert.match(source,/router\.replace\(/);
 assert.match(source,/next\.startsWith\('\/'\)/);
 assert.match(source,/next\.startsWith\('\/\/'\)/);
 assert.match(source,/isAdminPath\(next\)/);
 assert.match(source,/'\/events'/);
});
test('shared navigation includes dashboard and sign out',()=>{const shell=readFileSync('app/components/HostGlobalNav.tsx','utf8');assert.match(shell,/HostWorkspace/);const s=readFileSync('components/HostWorkspace.tsx','utf8');assert.match(s,/href:'\/dashboard'/);assert.match(s,/hostApi.logout/)});
test('preview has back to events',()=>assert.match(readFileSync('app/events/[id]/preview/page.tsx','utf8'),/Back to Events/));
