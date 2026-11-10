import test from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
const source=readFileSync(new URL('../app/admin/page.tsx',import.meta.url),'utf8');
test('refresh retrieves selected host permissions alongside platform settings',()=>{
 assert.match(source,/selectedHostModules]=await Promise\.all/);
 assert.match(source,/selected\?api<HostModule\[]>\('\/hosts\/'\+selected\+'\/modules'\)/);
 assert.match(source,/setHostModules\(selectedHostModules\)/);
 assert.match(source,/setHostModules\(\[\]\)/);assert.match(source,/if\(selectedRef.current===selected\)/);
});
test('module updates are sent to backend and CSRF protection remains',()=>{
 assert.match(source,/await api\(path,\{method:'POST'/);
 assert.match(source,/X-XSRF-TOKEN/);
 assert.match(source,/disabled=\{busy\|\|!m\.platform_enabled\}/);
});
