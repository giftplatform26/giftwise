// Read-only checks in the installed standalone app's WebView.
// First forward its webview_devtools_remote_<pid> socket to local port 9223.
import fs from 'node:fs';
import WebSocket from 'ws';
let target;
const readyDeadline=Date.now()+30000;
while(!target && Date.now()<readyDeadline){
 try{const targets=await fetch('http://127.0.0.1:9223/json/list').then(r=>r.json());target=targets.find(t=>t.url?.startsWith('http://127.0.0.1:3100/'));}catch{}
 if(!target)await new Promise(resolve=>setTimeout(resolve,500));
}
if(!target)throw Error('GiftWise WebView is not available for the local preview check.');
const endpoint=new URL(target.webSocketDebuggerUrl);endpoint.hostname='127.0.0.1';endpoint.port='9223';
const socket=new WebSocket(endpoint.toString());
const pending=new Map();let id=0;
const timer=setTimeout(()=>{for(const p of pending.values())p.reject(Error('WebView check timed out'));socket.close();},20000);
socket.on('message',raw=>{const data=JSON.parse(String(raw));const p=pending.get(data.id);if(p){pending.delete(data.id);data.error?p.reject(Error(JSON.stringify(data.error))):p.resolve(data.result);}});
await new Promise((resolve,reject)=>{socket.once('open',resolve);socket.once('error',reject);});
function request(method,params={}){return new Promise((resolve,reject)=>{const requestId=++id;pending.set(requestId,{resolve,reject});socket.send(JSON.stringify({id:requestId,method,params}));});}
try{
 await request('Runtime.enable');
 const result=await request('Runtime.evaluate',{expression:`(async()=>{const data=await fetch('/backend/api/public/experience',{cache:'no-store',credentials:'include'});const admin=await fetch('/admin',{cache:'no-store'});return {page:location.href,status:data.status,data:await data.json(),adminStatus:admin.status};})()`,awaitPromise:true,returnByValue:true});
 const value=result.result?.value;
 if(value?.status!==200||value?.adminStatus!==404||!['CLASSIC','FOCUSED'].includes(value?.data?.viewMode))throw Error('Installed-app check failed: '+JSON.stringify(result));
 const report={verifiedAt:new Date().toISOString(),from:'installed standalone Android GiftWise WebView',package:'com.giftwise.preview',version:'0.3.0',...value};
 fs.mkdirSync(new URL('../artifacts/',import.meta.url),{recursive:true});fs.writeFileSync(new URL('../artifacts/backend-verification.json',import.meta.url),JSON.stringify(report,null,2)+'\n');console.log(JSON.stringify(report,null,2));
}finally{clearTimeout(timer);socket.close();}
