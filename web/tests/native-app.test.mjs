import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import {isAdminPath,isNativeAppUserAgent} from '../lib/native-app.mjs';

test('recognises only the GiftWise app user agent',()=>{
  assert.equal(isNativeAppUserAgent('Mozilla/5.0 (Linux; Android 15) AppleWebKit/537.36 Chrome/131 Mobile Safari/537.36 GiftWiseApp/0.3.0'),true);
  for(const ua of [undefined,null,'','Mozilla/5.0 Chrome/131','NotGiftWiseApp/1.0','GiftWiseApplication'])assert.equal(isNativeAppUserAgent(ua),false,String(ua));
});
test('admin pages and APIs are detected, including encoded and traversal variants',()=>{
  for(const path of ['/admin','/admin/login','/ADMIN','/%61dmin','/%2561dmin','/events/../admin','/events/%2e%2e/admin','/admin%2flogin','/backend/api/admin/console','/api/admin/console','/admin\\login'])assert.equal(isAdminPath(path),true,path);
  for(const path of ['/login','/events','/events/new','/gifts','/host/account','/e/token','/administrator-guide','/backend/api/auth/me'])assert.equal(isAdminPath(path),false,path);
});
test('middleware and login enforce web-only administration for the app',()=>{
  const middleware=fs.readFileSync(new URL('../middleware.ts',import.meta.url),'utf8');
  assert.match(middleware,/isNativeAppUserAgent\(request\.headers\.get\('user-agent'\)\) && isAdminPath\(request\.nextUrl\.pathname\)/);
  assert.match(middleware,/status: 404/);
  const login=fs.readFileSync(new URL('../app/login/page.tsx',import.meta.url),'utf8');
  assert.match(login,/isAdmin&&isNativeAppUserAgent\(navigator\.userAgent\)\)\{await hostApi\.logout/);
});
