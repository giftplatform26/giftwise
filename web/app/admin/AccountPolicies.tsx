'use client';
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
export default function AccountPolicies({hosts,events}:{hosts:Host[];events:EventRow[]}){
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
 return <section className="acPanel"><h2>Registration and recovery policies</h2>
  <p>Platform administrators manage platform, host, and event restrictions. Child policies cannot weaken mandatory verified contact requirements. These settings do not by themselves activate OTP delivery or account recovery.</p>
  <div className="acRows"><label>Policy level<select aria-label="Policy level" value={scope} onChange={e=>setScope(e.target.value)}><option value="PLATFORM">Entire platform</option><option value="HOST">Specific host</option><option value="EVENT">Specific event</option></select></label>
  {scope==='HOST'&&<label>Host<select aria-label="Policy host" value={host} onChange={e=>setHost(e.target.value)}><option value="">Select host</option>{hosts.map(h=><option key={h.id} value={h.id}>{h.username} · {h.email}</option>)}</select></label>}
  {scope==='EVENT'&&<label>Event<select aria-label="Policy event" value={event} onChange={e=>setEvent(e.target.value)}><option value="">Select event</option>{events.map(e=><option key={e.id} value={e.id}>{e.title} · {e.host_name}</option>)}</select></label>}
  {fields.map(f=><label key={f.key}><span>{f.label}</span><input type="checkbox" checked={!!value?.[f.key]} disabled={!value||busy||f.key!=='allowGroupPin'} onChange={e=>setValue(v=>v?{...v,[f.key]:e.target.checked}:v)}/></label>)}
  </div>
  {effective&&<p>Effective policy loaded. Email/mobile collection and verification requirements are mandatory security minimums; PIN login may be disabled.</p>}
  {error&&<p role="alert" style={{color:'#a52a2a'}}>{error}</p>}
  {message&&<p role="status">{message}</p>}
  <button disabled={!value||!id||busy} onClick={()=>void save()}>{busy?'Saving…':'Save policy'}</button>
 </section>;
}
