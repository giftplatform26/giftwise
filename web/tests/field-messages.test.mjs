import test from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync,readdirSync,statSync} from 'node:fs';
import {join} from 'node:path';
import {formatRequirement,knownPatterns} from '../lib/field-messages.mjs';

const field=(pattern,title='')=>({title,getAttribute:n=>n==='pattern'?pattern:null});
test('known patterns get a plain-language requirement',()=>{
 assert.equal(formatRequirement(field('[0-9]{6}')),'Enter the 6-digit code.');
 assert.match(formatRequirement(field('[A-Za-z0-9][A-Za-z0-9_]{3,29}')),/4–30 letters/);
});
test('a field title wins over the shared message',()=>assert.equal(formatRequirement(field('[0-9]{6}','Use the code from your email')),'Use the code from your email'));
test('unknown patterns fall back to the browser message',()=>assert.equal(formatRequirement(field('[a-z]{3}')),null));
test('every pattern used in the app has a message or its own title',()=>{
 const files=[];const walk=d=>{for(const f of readdirSync(d)){const p=join(d,f);if(statSync(p).isDirectory())walk(p);else if(p.endsWith('.tsx'))files.push(p)}};walk('app');walk('components');
 for(const file of files){const src=readFileSync(file,'utf8');
  for(const tag of src.match(/<input[^>]*pattern=[^>]*>/g)||[]){if(/\btitle=/.test(tag))continue;
   const literals=[...tag.matchAll(/pattern=(?:"([^"]+)"|\{[^}]*?'([^']+)'(?:\s*:\s*'([^']+)')?)/g)].flatMap(m=>[m[1],m[2],m[3]]).filter(p=>p&&p.includes('[')).map(p=>p.replace(/\\\\/g,'\\'));
   for(const p of literals)assert.ok(knownPatterns.includes(p),`${file}: no message for pattern ${p}`);}}
});
