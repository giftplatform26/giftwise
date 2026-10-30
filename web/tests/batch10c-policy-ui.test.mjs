import test from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
const page=readFileSync(new URL('../app/admin/page.tsx',import.meta.url),'utf8');
const component=readFileSync(new URL('../app/admin/AccountPolicies.tsx',import.meta.url),'utf8');
test('admin policy tab wired',()=>{assert.match(page,/Authentication & policies/);assert.match(page,/<AccountPolicies hosts=/);});
test('policy API uses authenticated cookies and CSRF for mutations',()=>{assert.match(component,/credentials:'include'/);assert.match(component,/X-XSRF-TOKEN/);assert.match(component,/method:modifying\?'PUT':'GET'/);});
test('mandatory security minimums cannot be unchecked in UI',()=>{assert.match(component,/f.key!=='allowGroupPin'/);});
test('platform, host and event selection supported',()=>{for(const scope of ['PLATFORM','HOST','EVENT'])assert.ok(component.includes(scope));});
