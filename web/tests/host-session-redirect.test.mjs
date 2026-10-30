import test from 'node:test';
import assert from 'node:assert/strict';
import {redirectIfSessionExpired,isProtectedHostRequest} from '../lib/host-session.mjs';
function location(){const calls=[];return {pathname:'/gifts',search:'?eventId=123',replace:path=>calls.push(path),calls}}
test('401 redirects once with the current page as return path',()=>{const l=location();assert.equal(redirectIfSessionExpired({status:401},'/backend/api/v1/events/123/gifts',l),true);redirectIfSessionExpired({status:401},'/backend/api/auth/me',l);assert.equal(l.calls.length,1);assert.equal(l.calls[0],'/login?reason=session-expired&next=%2Fgifts%3FeventId%3D123')});
test('invalid credentials and guest authentication never redirect host session',()=>{const l=location();for(const path of ['/backend/api/auth/login','/backend/api/public/groups/123/account','/backend/api/public/experience'])assert.equal(redirectIfSessionExpired({status:401},path,l),false);assert.equal(l.calls.length,0)});
test('403, server errors and successful requests do not redirect',()=>{const l=location();for(const status of [200,403,404,500,502])assert.equal(redirectIfSessionExpired({status},'/backend/api/v1/events',l),false);assert.equal(l.calls.length,0)});
test('login page cannot redirect to itself',()=>{const l={...location(),pathname:'/login'};assert.equal(redirectIfSessionExpired({status:401},'/backend/api/auth/me',l),false)});
test('absolute URLs and direct host API paths match',()=>{assert.ok(isProtectedHostRequest('http://localhost:3000/backend/api/v1/host/notifications'));assert.ok(isProtectedHostRequest('/api/v1/events/123/contributions'));assert.ok(!isProtectedHostRequest('/backend/api/auth/register'))});

test('email verification owns its session-check sign-in prompt',()=>{const l={...location(),pathname:'/verify-email',search:''};assert.equal(redirectIfSessionExpired({status:401},'/backend/api/auth/me',l),false);assert.equal(l.calls.length,0)});
test('other protected requests still redirect while on verification',()=>{const l={...location(),pathname:'/verify-email',search:''};assert.equal(redirectIfSessionExpired({status:401},'/backend/api/v1/events',l),true);assert.equal(l.calls.length,1)});
