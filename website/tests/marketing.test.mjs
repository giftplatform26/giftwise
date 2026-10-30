import test from 'node:test';
import assert from 'node:assert/strict';
import {readFile,stat} from 'node:fs/promises';
const html=await readFile(new URL('../out/index.html',import.meta.url),'utf8');
test('export has one main heading and complete section navigation',()=>{assert.equal((html.match(/<h1[ >]/g)||[]).length,1);const ids=new Set([...html.matchAll(/\bid="([^"]+)"/g)].map(x=>x[1]));for(const [,target] of html.matchAll(/href="#([^"]+)"/g))assert.ok(ids.has(target),'Missing anchor '+target);for(const id of ['wishlist','how-it-works','group-gifting','main'])assert.ok(ids.has(id))});
test('export includes metadata and crawler endpoints',async()=>{assert.match(html,/<meta name="description"/);assert.match(html,/<link rel="canonical"/);assert.match(html,/application\/ld\+json/);for(const file of ['sitemap.xml','robots.txt'])assert.ok((await stat(new URL('../out/'+file,import.meta.url))).size>0)});
test('hero and product assets ship locally within a reasonable budget',async()=>{for(const file of ['meaningful-gifts-hero.webp','wishlist-products.webp','group-gifting.webp']){const asset=await stat(new URL('../public/images/'+file,import.meta.url));assert.ok(asset.size>1000&&asset.size<500000)}});
