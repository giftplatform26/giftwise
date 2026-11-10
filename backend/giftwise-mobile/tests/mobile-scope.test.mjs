import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import {isAdminPath,navigationTarget,deepLinkPath} from '../src/platform/policy.mjs';
import {isAdminPath as portalAdminPath} from '../web/lib/mobile-scope.mjs';
const origin='https://celebrate.example';
test('mobile and portal reject admin routes, API pages, and encoded redirect paths',()=>{
 for(const path of ['/admin','/admin/login','/admin/invitation-assets','/ADMIN','/%61dmin','/%2561dmin','/events/../admin','/events/%2e%2e/admin','/admin%2flogin','/backend/api/admin/console','/api/admin/console','/admin\\login']){
  assert.equal(isAdminPath(path),true,path);assert.equal(portalAdminPath(path),true,path);
  assert.equal(navigationTarget(origin+path,origin),'blocked',path);
  assert.equal(deepLinkPath(origin+path,origin),null,path);
  assert.equal(deepLinkPath('giftwise://open'+path,origin),null,path);
 }
});
test('host and guest routes stay available',()=>{
 for(const path of ['/login','/register','/events','/events/new','/gifts','/host/account','/e/token','/groups/token/id']){
  assert.equal(isAdminPath(path),false,path);assert.equal(portalAdminPath(path),false,path);
  assert.equal(navigationTarget(origin+path,origin),'internal',path);
 }
});
test('native home and mobile welcome have no admin entry',()=>{
 const app=fs.readFileSync(new URL('../App.tsx',import.meta.url),'utf8');
 const welcome=fs.readFileSync(new URL('../web/components/GiftWiseWelcome.tsx',import.meta.url),'utf8');
 assert.doesNotMatch(app,/Platform admin sign in|PLATFORM ADMINISTRATION|open\('\/admin/);
 assert.doesNotMatch(welcome,/href="\/admin/);
 const middleware=fs.readFileSync(new URL('../web/middleware.ts',import.meta.url),'utf8');
 assert.match(middleware,/isAdminPath\(request.nextUrl.pathname\)/);assert.match(middleware,/status: 404/);
});
