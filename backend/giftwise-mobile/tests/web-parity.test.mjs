import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
const root = new URL('../', import.meta.url);
const snapshot = JSON.parse(fs.readFileSync(new URL('brand/web-source-snapshot.json',root),'utf8'));
test('shared source retains the original routes while mobile middleware excludes administration',()=>{
 const routes=Object.keys(snapshot.files).filter(name=>name.startsWith('app/')&&name.endsWith('/page.tsx'));
 assert.equal(routes.length,28);
 for(const route of routes)assert.ok(fs.existsSync(new URL('web/'+route,root)),route);
 assert.ok(fs.existsSync(new URL('web/app/admin/login/page.tsx',root)));
});
test('mobile login accepts customers and retains the shared web reference authorization checks',()=>{
 const login=fs.readFileSync(new URL('web/app/login/page.tsx',root),'utf8');
 assert.match(login,/role!=='CUSTOMER'/);assert.match(login,/await hostApi.logout/);assert.doesNotMatch(login,/isAdmin\?'\/admin'/);
 const admin=fs.readFileSync(new URL('web/app/admin/page.tsx',root),'utf8');
 assert.match(admin,/X-XSRF-TOKEN/);assert.match(admin,/Platform administrator access required/);assert.match(admin,/hostApi.logout/);
});
test('all snapshotted modules and public assets remain present',()=>{
 for(const file of Object.keys(snapshot.files))assert.ok(fs.existsSync(new URL('web/'+file,root)),file);
});
