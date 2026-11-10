import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import vm from 'node:vm';
const source=fs.readFileSync(new URL('../src/platform/bridge.ts',import.meta.url),'utf8').replace('export function nativeBridge(origin: string)', 'function nativeBridge(origin)');
const generator=vm.createContext({});vm.runInContext(source,generator);
function harness(pageOrigin='https://celebrate.example') {
 const messages=[]; const fetched=[]; let normalClicks=0;
 function Anchor(href, download='') {this.href=href;this.download=download;}
 Anchor.prototype.hasAttribute=function(name){return name==='download';};
 Anchor.prototype.click=function(){normalClicks++;};
 const ctx={location:{origin:pageOrigin},navigator:{},URL,HTMLAnchorElement:Anchor,document:{addEventListener(){}},fetch:async url=>{fetched.push(url);return {ok:true,blob:async()=>({size:10,type:'text/calendar;charset=utf-8'})};},FileReader:class {readAsDataURL(){this.result='data:text/calendar;base64,aGVsbG8=';this.onload();}},};
 ctx.window={ReactNativeWebView:{postMessage:raw=>messages.push(JSON.parse(raw))},alert:()=>{}};
 vm.runInNewContext(generator.nativeBridge('https://celebrate.example'),ctx);
 return {ctx,messages,fetched,normalClicks:()=>normalClicks,Anchor};
}
test('native bridge captures detached programmatic downloads and strips charset',async()=>{
 const h=harness();new h.Anchor('blob:https://celebrate.example/export','celebration.ics').click();
 await new Promise(resolve=>setImmediate(resolve));
 assert.equal(h.normalClicks(),0);assert.equal(h.fetched.length,1);
 assert.deepEqual(h.messages[0],{type:'export',name:'celebration.ics',mime:'text/calendar',base64:'aGVsbG8='});
});
test('bridge never installs handlers on a third-party checkout page',()=>{
 const h=harness('https://checkout.example');new h.Anchor('https://checkout.example/report','report.csv').click();
 assert.equal(h.normalClicks(),1);assert.equal(h.messages.length,0);
});
test('native sharing sends plain text and a link, without cookies',async()=>{
 const h=harness();await h.ctx.navigator.share({title:'Join us',text:'A celebration',url:'https://celebrate.example/e/test'});
 assert.deepEqual(h.messages[0],{type:'share',text:'Join us\nA celebration',url:'https://celebrate.example/e/test'});
});
