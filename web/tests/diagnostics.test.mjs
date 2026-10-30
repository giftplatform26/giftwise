import test from 'node:test';
import assert from 'node:assert/strict';
import {routeLabel,recordMetric,diagnosticsSnapshot,recordFlow} from '../lib/diagnostics.mjs';
test('diagnostics removes identifiers and query data before storing paths',()=>{
 const path='/backend/api/public/groups/123e4567-e89b-12d3-a456-426614174000/members/42?email=private@example.com#secret';
 assert.equal(routeLabel(path),'/backend/api/public/groups/:id/members/:id');
 recordMetric('api_ms',125,path);const last=diagnosticsSnapshot().samples.at(-1);assert.equal(last.route,'/backend/api/public/groups/:id/members/:id');assert.ok(!JSON.stringify(last).includes('private'));
});
test('diagnostic sample memory is bounded and snapshots cannot mutate it',()=>{
 for(let i=0;i<250;i++)recordMetric('api_ms',i,'/dashboard');
 const snapshot=diagnosticsSnapshot();assert.equal(snapshot.samples.length,200);snapshot.samples[0].value=-1;assert.notEqual(diagnosticsSnapshot().samples[0].value,-1);
 recordMetric('api_ms',NaN);recordMetric('api_ms',-1);assert.equal(diagnosticsSnapshot().samples.length,200);
});
test('funnel counters are opt-in and reject unrecognised labels',()=>{
 const before=diagnosticsSnapshot().flows;recordFlow('guest_contribution','confirmed');assert.deepEqual(diagnosticsSnapshot().flows,before);
 process.env.NEXT_PUBLIC_DIAGNOSTICS_ENABLED='true';
 try{recordFlow('guest_contribution','submitted_unverified');recordFlow('guest_contribution','confirmed');recordFlow('private@example.com','confirmed');assert.equal(diagnosticsSnapshot().flows['guest_contribution:submitted_unverified'],1);assert.equal(diagnosticsSnapshot().flows['guest_contribution:confirmed'],1);assert.ok(!JSON.stringify(diagnosticsSnapshot()).includes('private@example.com'));}finally{delete process.env.NEXT_PUBLIC_DIAGNOSTICS_ENABLED;}
});
