// Read-only smoke check in the installed app's development runtime.
// Requires Node 24, a running Android development build, and Metro on port 8097.
import fs from 'node:fs';
import WebSocket from 'ws';
let target;
const readyDeadline=Date.now()+20000;
while(!target && Date.now()<readyDeadline){
 const targets=await fetch('http://127.0.0.1:8097/json/list').then(r=>r.json());
 target=targets.find(t=>t.title?.includes('com.giftwise.preview'));
 if(!target)await new Promise(resolve=>setTimeout(resolve,500));
}
if(!target)throw Error('Installed GiftWise runtime is not connected to Metro.');
const debuggerURL=new URL(target.webSocketDebuggerUrl);
debuggerURL.hostname='127.0.0.1';
debuggerURL.port='8097';
const socket=new WebSocket(debuggerURL.toString(), { origin: 'http://127.0.0.1:8097' });
socket.on('unexpected-response',(_,response)=>{let body='';response.on('data',chunk=>{body+=chunk});response.on('end',()=>{console.error('Debugger handshake:',response.statusCode,body.slice(0,400));socket.terminate();});});
const pending=new Map();let id=0;
const timer=setTimeout(()=>{console.error('Native backend verification timed out.');socket.close();process.exitCode=1;},30000);
socket.addEventListener('message',event=>{const message=JSON.parse(String(event.data));if(message.id&&pending.has(message.id)){const p=pending.get(message.id);pending.delete(message.id);message.error?p.reject(Error(JSON.stringify(message.error))):p.resolve(message.result);}});
await new Promise((resolve,reject)=>{socket.addEventListener('open',resolve,{once:true});socket.addEventListener('error',event=>reject(Error(event.message||'Debugger connection failed')),{once:true});});
function request(method,params={}){return new Promise((resolve,reject)=>{const requestId=++id;pending.set(requestId,{resolve,reject});socket.send(JSON.stringify({id:requestId,method,params}));});}
try{
 await request('Runtime.enable');
 await request('Runtime.evaluate',{expression:`globalThis.__giftwiseBackendProbe={state:'pending'};fetch('http://127.0.0.1:3100/backend/api/public/experience',{cache:'no-store'}).then(async response=>{globalThis.__giftwiseBackendProbe={state:'complete',status:response.status,data:await response.json()};}).catch(error=>{globalThis.__giftwiseBackendProbe={state:'failed',error:String(error)};});true;`});
 let value;
 for(let i=0;i<20;i++){
  const result=await request('Runtime.evaluate',{expression:'globalThis.__giftwiseBackendProbe',returnByValue:true});value=result.result?.value;
  if(value?.state&&value.state!=='pending')break;
  await new Promise(resolve=>setTimeout(resolve,500));
 }
 if(value?.status!==200||!['CLASSIC','FOCUSED'].includes(value?.data?.viewMode))throw Error('Backend smoke check failed: '+JSON.stringify(value));
 const report={verifiedAt:new Date().toISOString(),from:'installed Android GiftWise JavaScript runtime',package:'com.giftwise.preview',request:'http://127.0.0.1:3100/backend/api/public/experience',...value};
 fs.mkdirSync(new URL('../artifacts/',import.meta.url),{recursive:true});
 fs.writeFileSync(new URL('../artifacts/backend-verification.json',import.meta.url),JSON.stringify(report,null,2)+'\n');
 console.log(JSON.stringify(report,null,2));
}finally{clearTimeout(timer);socket.close();}
