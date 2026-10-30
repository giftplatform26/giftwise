import test from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
const bell=readFileSync(new URL('../app/components/HostNotificationBell.tsx',import.meta.url),'utf8');
const dash=readFileSync(new URL('../app/dashboard/page.tsx',import.meta.url),'utf8');
test('dashboard renders notification bell',()=>assert.match(dash,/<HostNotificationBell\s*\/>/));
test('host inbox uses session credentials and periodic refresh',()=>{assert.match(bell,/credentials:'include'/);assert.match(bell,/setInterval/);});
test('read mutation includes CSRF and event-specific link',()=>{assert.match(bell,/X-XSRF-TOKEN/);assert.match(bell,/\/contributions/);});
