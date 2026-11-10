import test from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
const login=readFileSync(new URL('../app/login/page.tsx',import.meta.url),'utf8');
const gallery=readFileSync(new URL('../app/admin/invitation-assets/page.tsx',import.meta.url),'utf8');
test('mobile login uses the authenticated customer role and signs other roles out',()=>{assert.match(login,/signedIn\?\.role/);assert.match(login,/role!=='CUSTOMER'/);assert.match(login,/await hostApi.logout/);assert.doesNotMatch(login,/isAdmin\?'\/admin'/);});
test('host next parameter excludes normalized administrator paths',()=>{assert.match(login,/!isAdminPath\(next\)/);});
test('gallery returns to admin dashboard',()=>{assert.match(gallery,/href=\x22\/admin\x22/);assert.doesNotMatch(gallery,/href=\x22\/events\x22/);});
