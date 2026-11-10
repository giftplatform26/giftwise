'use client';
/* localization:instrumented */
import {localize,localizeText} from '../../lib/localization.mjs';
import {useLocalizationRender} from '../../components/LocalizationProvider';

import {useEffect,useState} from 'react';

type Host={id:string;username:string;email:string};
type EventRow={id:string;title:string;status:string;occasion_type:string;host_name:string};
type Policy={requireEmail:boolean;requireMobile:boolean;verifyEmail:boolean;verifyMobile:boolean;allowGroupPin:boolean};
type PolicyResponse={effective:Policy;override:Record<string,unknown>[]};
const ROOT='00000000-0000-0000-0000-000000000000';
const fields: {key:keyof Policy; label:string}[]=[
 {key:'requireEmail',label:'Require email address'},
 {key:'requireMobile',label:'Require mobile number'},
 {key:'verifyEmail',label:'Require verified email'},
 {key:'verifyMobile',label:'Require verified mobile'},
 {key:'allowGroupPin',label:'Allow group PIN login'},
];
async function policyRequest(scope:string,id:string,update?:Policy):Promise<PolicyResponse>{
 const modifying=!!update;
 if(modifying){const init=await fetch('/backend/api/auth/csrf',{credentials:'include',cache:'no-store'});if(!init.ok)throw Error('Could not initialize secure session');}
 const cookie=typeof document==='undefined'?'':document.cookie.split(';').map(x=>x.trim()).find(x=>x.startsWith('XSRF-TOKEN='));
 const token=cookie?decodeURIComponent(cookie.slice('XSRF-TOKEN='.length)):'';
 if(modifying&&!token)throw Error('CSRF token missing');
 const response=await fetch('/backend/api/admin/account-policies/'+scope+'/'+encodeURIComponent(id),{
  method:modifying?'PUT':'GET',credentials:'include',cache:'no-store',
  headers:{'Accept':'application/json',...(modifying?{'Content-Type':'application/json','X-XSRF-TOKEN':token}:{})},
  ...(modifying?{body:JSON.stringify(update)}:{})
 });
 if(!response.ok)throw Error(response.status===403?'Administrator access or CSRF check failed':`Policy request failed (HTTP ${response.status})`);
 return response.json();
}
export default function AccountPolicies({hosts,events}:{hosts:Host[];events:EventRow[]}){useLocalizationRender();
 const [scope,setScope]=useState('PLATFORM'),[host,setHost]=useState(''),[event,setEvent]=useState('');
 const [value,setValue]=useState<Policy|null>(null),[effective,setEffective]=useState<Policy|null>(null);
 const [error,setError]=useState(''),[message,setMessage]=useState(''),[busy,setBusy]=useState(false);
 const id=scope==='PLATFORM'?ROOT:scope==='HOST'?host:event;
 useEffect(()=>{let active=true;setValue(null);setEffective(null);setError('');setMessage('');if(!id)return;
  void policyRequest(scope,id).then(result=>{if(active){setValue(result.effective);setEffective(result.effective);}}).catch(e=>{if(active)setError(String(e));});
  return()=>{active=false};
 },[scope,id]);
 async function save(){if(!value||!id||busy)return;setBusy(true);setError('');setMessage('');
  try{const result=await policyRequest(scope,id,value);setValue(result.effective);setEffective(result.effective);setMessage('Policy saved and audited. Effective restrictions shown.');}
  catch(e){setError(String(e));}finally{setBusy(false);}}
 return <section className="acPanel"><h2>{localize("ui.243ff0638e109270",{defaultValue:"Registration and recovery policies"})}</h2>
  <p>{localize("ui.216b5158dc07c5a1",{defaultValue:"Platform administrators manage platform, host, and event restrictions. The global verification switches determine whether contact checks apply. Host and event restrictions apply when their channel is enabled."})}</p>
  <div className="acRows"><label>{localize("ui.1464bf3de3861a63",{defaultValue:"Policy level"})}<select aria-label={localize("ui.1464bf3de3861a63",{defaultValue:"Policy level"})} value={scope} onChange={e=>setScope(e.target.value)}><option value="PLATFORM">{localize("ui.7d1bd7a521e6ef30",{defaultValue:"Entire platform"})}</option><option value="HOST">{localize("ui.594e2c18c684f087",{defaultValue:"Specific host"})}</option><option value="EVENT">{localize("ui.0ba7f92fc4172520",{defaultValue:"Specific event"})}</option></select></label>
  {scope==='HOST'&&<label>{localize("ui.4a823118b9ba8baa",{defaultValue:"Host"})}<select aria-label={localize("ui.26e576ad82c95d26",{defaultValue:"Policy host"})} value={host} onChange={e=>setHost(e.target.value)}><option value="">{localize("ui.f05e43a498a59f81",{defaultValue:"Select host"})}</option>{hosts.map(h=><option key={h.id} value={h.id}>{h.username} · {h.email}</option>)}</select></label>}
  {scope==='EVENT'&&<label>{localize("ui.4e1f49a9c8ae8a15",{defaultValue:"Event"})}<select aria-label={localize("ui.d26cf42d38c394cc",{defaultValue:"Policy event"})} value={event} onChange={e=>setEvent(e.target.value)}><option value="">{localize("ui.ff25e66f618f2984",{defaultValue:"Select event"})}</option>{events.map(e=><option key={e.id} value={e.id}>{e.title} · {e.host_name}</option>)}</select></label>}
  {fields.map(f=><label key={f.key}><span>{f.label}</span><input type="checkbox" checked={!!value?.[f.key]} disabled={!value||busy||f.key!=='allowGroupPin'} onChange={e=>setValue(v=>v?{...v,[f.key]:e.target.checked}:v)}/></label>)}
  </div>
  {effective&&<p>{localize("ui.4a5c5cf4da5ea709",{defaultValue:"Effective policy loaded. Contact collection remains required. Global switches above control verification; PIN login may be disabled."})}</p>}
  {error&&<p role="alert" style={{color:'#a52a2a'}}>{localizeText(error)}</p>}
  {message&&<p role="status">{localizeText(message)}</p>}
  <button disabled={!value||!id||busy} onClick={()=>void save()}>{busy?localize("ui.23e39291d6135814",{defaultValue:"Saving…"}):localize("ui.57ee14ce1425ca9b",{defaultValue:"Save policy"})}</button>
 </section>;
}
