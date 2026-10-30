import test from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
const login=readFileSync(new URL('../app/login/page.tsx',import.meta.url),'utf8');
const gallery=readFileSync(new URL('../app/admin/invitation-assets/page.tsx',import.meta.url),'utf8');
test('admin login uses authenticated server role',()=>{assert.match(login,/signedIn\?\.role/);assert.match(login,/role===\x27PLATFORM_ADMIN\x27/);assert.match(login,/isAdmin\?\x27\/admin\x27/);});
test('host next parameter cannot route into admin pages',()=>{assert.match(login,/!next\.startsWith\(\x27\/admin\x27\)/);});
test('gallery returns to admin dashboard',()=>{assert.match(gallery,/href=\x22\/admin\x22/);assert.doesNotMatch(gallery,/href=\x22\/events\x22/);});
