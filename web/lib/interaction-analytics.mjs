import {localization} from './localization.mjs';
// Browser telemetry never records button text, field values, full URLs, tokens in paths, or payment data.
export function safeRoute(value){
 if(!value)return '';
 if(!value.startsWith('/'))return ['external','upi','mailto','tel','blob'].includes(value)?value:'external';
 return value.split(/[?#]/,1)[0].split('/').map(part=>/^[a-z][a-z-]{0,40}$/.test(part)||!part?part:':id').join('/');
}
export function routeScope(value){
 try{const url=new URL(value,'http://local');const uuid='([a-f0-9-]{36})';const event=url.pathname.match(new RegExp('^/events/'+uuid+'(?:/|$)','i'));const guest=url.pathname.match(new RegExp('^/(?:e|groups)/'+uuid+'(?:/|$)','i'));const query=url.searchParams.get('eventId');return {...(event?{eventId:event[1]}:query&&/^[a-f0-9-]{36}$/i.test(query)?{eventId:query}:{}),...(guest?{token:guest[1]}:{})};}catch{return {}}
}
export function classifyAction({action='',href='',download=false,label='',tag='button'}){
 if(/^[A-Z][A-Z0-9_]{0,63}$/.test(action))return action;
 if(download)return /qr/i.test(href+' '+label)?'QR_DOWNLOAD':'FILE_DOWNLOAD';
 if(/invitation|invite/.test(label)&&/download/.test(label))return 'INVITATION_DOWNLOAD';
 if(/download.*qr|qr.*download/.test(label))return 'QR_DOWNLOAD';
 if(/member|join|group/.test(label+' '+href)&&/link|copy|join|\/groups\//.test(label+' '+href))return 'MEMBER_LINK';
 if(/upi:|\/pay(?:ment)?(?:\/|\?)|checkout/.test(href)||/pay|payment|contribute|purchase/.test(label))return 'PAYMENT_LINK';
 if(/\/e\//.test(href)||/invitation|invite|share|copy link/.test(label))return 'INVITATION_LINK';
 return tag==='a'?'LINK_CLICK':tag==='select'?'SELECTION_CLICK':'BUTTON_CLICK';
}
let knownBundle=null,knownControls=new Map();
function knownControlKey(text){
 const bundle=localization.getResourceBundle(localization.resolvedLanguage||localization.language||'en','translation');
 if(bundle!==knownBundle){knownBundle=bundle;knownControls=new Map();for(const [key,value] of Object.entries(bundle||{}))if(/^ui\.[a-f0-9]{16}$/.test(key)&&typeof value==='string'&&!value.includes('{{'))knownControls.set(value.trim().replace(/\s+/g,' '),key);}
 return knownControls.get(text.trim().replace(/\s+/g,' '))||'';
}
let queue=[],timer=null,sending=false,retries=0,installed=false,current=null,baseFetch=null,pendingNavigation=null,lastPage=null;
const uuid=()=>globalThis.crypto?.randomUUID?.();
function enqueue(event){if(!installed||!event?.id)return;queue.push(event);if(queue.length>150)queue.splice(0,queue.length-150);if(!timer)timer=setTimeout(flush,350);}
async function flush(){
 timer=null;if(!installed||sending||!queue.length||!baseFetch)return;sending=true;const events=queue.splice(0,50);
 try{
  let token=document.cookie.split(';').map(v=>v.trim()).find(v=>v.startsWith('XSRF-TOKEN='))?.slice(11);
  if(!token){await baseFetch('/backend/api/auth/csrf',{credentials:'include',cache:'no-store'});token=document.cookie.split(';').map(v=>v.trim()).find(v=>v.startsWith('XSRF-TOKEN='))?.slice(11);}
  if(!installed||!baseFetch){queue=[];return;}
  if(!token)throw Error('No CSRF token');
  const response=await baseFetch('/backend/api/public/interactions',{method:'POST',credentials:'include',keepalive:true,headers:{'Content-Type':'application/json','X-XSRF-TOKEN':decodeURIComponent(token),'X-Gift-Analytics-Consent':'v2'},body:JSON.stringify({events})});
  if(!response.ok)throw Error('Tracking unavailable');retries=0;
 }catch{if(installed&&++retries<=3)queue.unshift(...events);else{queue=[];retries=0;}}finally{sending=false;if(installed&&queue.length&&!timer)timer=setTimeout(flush,Math.min(10000,1000*(retries+1)));}
}
function context(){return typeof location==='undefined'?{}:routeScope(location.href);}
export function beginAction(action,scope={}){
 if(!installed)return null;
 const previous=current;
 const event=previous?{...previous.event,action,...scope}:{id:uuid(),kind:'OPERATION',action,page:safeRoute(location.pathname),control:'explicit',destination:'',outcome:'UNKNOWN',detail:'',...context(),...scope};
 const handle={event,started:performance.now()};enqueue(event);return handle;
}
export function completeAction(handle,outcome,detail=''){
 if(!handle)return;if(handle.completed&&['CANCELLED','FAILURE','UNKNOWN'].includes(outcome))return;if(['SUCCESS','BROWSER_READY'].includes(outcome))handle.completed=true;enqueue({...handle.event,outcome,detail,durationMs:Math.min(86400000,Math.max(0,Math.round(performance.now()-handle.started)))});
}
export async function trackedAction(action,operation,scope={}){
 const handle=beginAction(action,scope);try{const value=await operation();completeAction(handle,'SUCCESS','BROWSER_OPERATION_COMPLETED');return value;}catch(error){completeAction(handle,error?.name==='AbortError'?'CANCELLED':'FAILURE',error?.name==='AbortError'?'USER_CANCELLED':'BROWSER_OPERATION_FAILED');throw error;}
}
export function trackPage(){
 if(!installed)return;const now=Date.now();if(lastPage?.path===location.pathname&&now-lastPage.at<1000)return;lastPage={path:location.pathname,at:now};
 const arrived=pendingNavigation?.targetPath===location.pathname?pendingNavigation:null;pendingNavigation=null;
 if(arrived)completeAction(arrived,'SUCCESS','IN_APP_DESTINATION_RENDERED');
 enqueue({id:uuid(),...(arrived?{parentId:arrived.event.id}:{}),kind:'PAGE_VIEW',action:'PAGE_OPEN',page:safeRoute(location.pathname),control:'page',destination:'',outcome:'OPENED',detail:'',...context()});
}
export function installInteractionTracking(){
 if(installed||typeof window==='undefined')return()=>{};installed=true;baseFetch=window.fetch.bind(window);const originalFetch=window.fetch;
 const click=e=>{
  if(!e.isTrusted||!(e.target instanceof Element))return;
  const el=e.target.closest('button,a,input[type=submit],input[type=button],input[type=checkbox],input[type=radio],select,summary,[role=button],[role=tab],[role=menuitem]');if(!el||el.matches(':disabled,[aria-disabled=true]'))return;
  const href=el.getAttribute('href')||'';let destination='';try{if(!href)throw Error();const target=new URL(href,location.href);destination=target.origin===location.origin?safeRoute(target.pathname):['upi:','mailto:','tel:','blob:'].includes(target.protocol)?target.protocol.slice(0,-1):'external';}catch{}
  const scope={...context(),...(!Object.keys(context()).length?routeScope(href):{})};const eventId=el.closest('[data-event-id]')?.getAttribute('data-event-id');if(eventId&&/^[a-f0-9-]{36}$/i.test(eventId))scope.eventId=eventId;
  const action=classifyAction({action:el.getAttribute('data-track-action')||'',href,label:(el.getAttribute('aria-label')||el.textContent||'').toLowerCase(),download:el.hasAttribute('download'),tag:el.tagName.toLowerCase()});
  // A structural selector identifies the control without collecting its text or user input.
  const path=[];let node=el;for(let i=0;node&&i<4;i++,node=node.parentElement)path.unshift(`${node.tagName.toLowerCase()}:nth-child(${node.parentElement?Array.from(node.parentElement.children).indexOf(node)+1:1})`);
  const event={id:uuid(),kind:'CLICK',action,page:safeRoute(location.pathname),control:[knownControlKey(el.getAttribute('aria-label')||el.textContent||''),path.join('>')].filter(Boolean).join(' ').slice(0,160),destination,outcome:href?'HANDED_OFF':'CLICKED',detail:href?'LINK_HANDOFF':'OUTCOME_NOT_OBSERVED',...scope};
  const handle={event,started:performance.now()};current=handle;enqueue(event);if(href&&!el.getAttribute('target')){try{const target=new URL(href,location.href);if(target.origin===location.origin&&target.pathname!==location.pathname)pendingNavigation={...handle,targetPath:target.pathname};}catch{}}setTimeout(()=>{if(current===handle)current=null},0);
 };
 window.fetch=(input,init)=>{
  {try{const url=new URL(typeof input==='string'?input:input instanceof URL?input.href:input.url,location.href);if(url.origin===location.origin&&url.pathname.startsWith('/backend/api/')&&!url.pathname.includes('/interactions')){const headers=new Headers(init?.headers??(input instanceof Request?input.headers:undefined));headers.set('X-Gift-Analytics-Consent','v2');if(current)headers.set('X-Gift-Interaction',current.event.id);const scope=current?.event||context();if(scope.eventId)headers.set('X-Gift-Event-Id',scope.eventId);if(scope.token)headers.set('X-Gift-Event-Token',scope.token);init={...init,headers};}}catch{}}
  return originalFetch.call(window,input,init);
 };
 const invalid=()=>{if(current)completeAction(current,'FAILURE','FORM_VALIDATION_BLOCKED')};
 const hide=()=>{if(document.visibilityState==='hidden')void flush()};
 document.addEventListener('click',click,true);document.addEventListener('invalid',invalid,true);document.addEventListener('visibilitychange',hide);
 return()=>{installed=false;document.removeEventListener('click',click,true);document.removeEventListener('invalid',invalid,true);document.removeEventListener('visibilitychange',hide);if(timer)clearTimeout(timer);queue=[];timer=null;baseFetch=null;pendingNavigation=null;lastPage=null;window.fetch=originalFetch;current=null;};
}

export function trackedCopy(text,scope={}){
 const action=/\/groups\//.test(text)?'MEMBER_LINK_COPY':/\/e\//.test(text)?'INVITATION_LINK_COPY':/^upi:/i.test(text)?'PAYMENT_LINK_COPY':'DETAILS_COPY';
 return trackedAction(action,()=>navigator.clipboard.writeText(text),scope);
}
export function trackedShare(payload){return trackedAction('INVITATION_SHARE',()=>navigator.share(payload));}
