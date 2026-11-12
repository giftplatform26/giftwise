'use client';
import {useCallback,useEffect,useState} from 'react';
import {localizeText} from '../lib/localization.mjs';
import {useLocalizationRender} from './LocalizationProvider';
import {bootstrapCsrf} from '../lib/host-api.mjs';
import {csrfToken} from '../lib/gift-api.mjs';
import {VENDOR_CATEGORIES} from '../lib/vendors.mjs';

async function admin(path:string,method='GET',body?:unknown){
 if(method!=='GET')await bootstrapCsrf(fetch);
 const r=await fetch(`/backend/api/admin/console${path}`,{method,credentials:'include',cache:'no-store',headers:{'Accept':'application/json',...(body!==undefined?{'Content-Type':'application/json'}:{}),...(method!=='GET'?{'X-XSRF-TOKEN':csrfToken(document.cookie)}:{})},...(body!==undefined?{body:JSON.stringify(body)}:{})});
 if(!r.ok){let reason='';try{reason=(await r.json()).error??''}catch{}throw Error(reason||`Request failed (${r.status})`)}
 return r.json();
}

type Vendor={id:string;name:string;category:string;city:string;description:string|null;phone:string|null;website:string|null;instagram:string|null;priceHint:string|null;verified:boolean;active:boolean};
const emptyVendor={name:'',category:'DECOR',city:'',description:'',phone:'',website:'',instagram:'',priceHint:'',verified:false,active:true};

export function AdminVendors(){useLocalizationRender();
 const [rows,setRows]=useState<Vendor[]>([]),[form,setForm]=useState<typeof emptyVendor>(emptyVendor),[editing,setEditing]=useState<string|null>(null),[error,setError]=useState(''),[busy,setBusy]=useState(false);
 const load=useCallback(async()=>{try{setRows(await admin('/vendors'))}catch(e){setError(e instanceof Error?e.message:'Could not load vendors')}},[]);
 useEffect(()=>{void load()},[load]);
 async function save(e:React.FormEvent){e.preventDefault();setBusy(true);setError('');try{await admin(editing?`/vendors/${editing}`:'/vendors',editing?'PUT':'POST',form);setForm(emptyVendor);setEditing(null);await load()}catch(err){setError(err instanceof Error?err.message:'Could not save')}finally{setBusy(false)}}
 const f=(k:keyof typeof emptyVendor,label:string,props:React.InputHTMLAttributes<HTMLInputElement>={})=><label>{localizeText(label)}<input value={String(form[k]??'')} onChange={e=>setForm({...form,[k]:e.target.value})} {...props}/></label>;
 return <section className="acPanel"><h2>{localizeText('Vendor directory')}</h2><p>{localizeText('Hosts with the Vendor directory module see active vendors. Only list businesses you have checked; mark them verified once confirmed.')}</p>
  {error&&<p role="alert">{localizeText(error)}</p>}
  <form onSubmit={save} style={{display:'grid',gap:10,gridTemplateColumns:'repeat(auto-fit,minmax(200px,1fr))',margin:'12px 0'}}>
   {f('name','Name',{required:true,maxLength:120})}
   <label>{localizeText('Category')}<select value={form.category} onChange={e=>setForm({...form,category:e.target.value})}>{VENDOR_CATEGORIES.map(([k,l]:[string,string])=><option key={k} value={k}>{localizeText(l)}</option>)}</select></label>
   {f('city','City',{required:true,maxLength:80})}{f('phone','Phone (+91…)',{maxLength:20})}{f('website','Website (https://)',{maxLength:300})}{f('instagram','Instagram handle',{maxLength:60})}{f('priceHint','Price hint',{maxLength:60})}{f('description','Description',{maxLength:600})}
   <label><input type="checkbox" checked={form.verified} onChange={e=>setForm({...form,verified:e.target.checked})}/> {localizeText('Verified')}</label>
   <label><input type="checkbox" checked={form.active} onChange={e=>setForm({...form,active:e.target.checked})}/> {localizeText('Visible to hosts')}</label>
   <div><button disabled={busy}>{localizeText(editing?'Save vendor':'Add vendor')}</button>{editing&&<button type="button" onClick={()=>{setEditing(null);setForm(emptyVendor)}}>{localizeText('Cancel')}</button>}</div></form>
  <div className="acTable"><table><thead><tr><th>{localizeText('Vendor')}</th><th>{localizeText('Category')}</th><th>{localizeText('City')}</th><th>{localizeText('Status')}</th><th/></tr></thead>
   <tbody>{rows.map(v=><tr key={v.id}><td>{v.name}{v.verified?' ✓':''}</td><td>{v.category}</td><td>{v.city}</td><td>{localizeText(v.active?'Visible':'Hidden')}</td>
    <td><button type="button" onClick={()=>{setEditing(v.id);setForm({name:v.name,category:v.category,city:v.city,description:v.description??'',phone:v.phone??'',website:v.website??'',instagram:v.instagram??'',priceHint:v.priceHint??'',verified:v.verified,active:v.active})}}>{localizeText('Edit')}</button></td></tr>)}</tbody></table></div>
 </section>;
}

type Referral={id:string;referrer:string;referrerEmail:string;friend:string;status:string;joinedAt:string;rewardedAt:string|null};
export function AdminReferrals(){useLocalizationRender();
 const [rows,setRows]=useState<Referral[]>([]),[error,setError]=useState('');
 const load=useCallback(async()=>{try{setRows(await admin('/referrals'))}catch(e){setError(e instanceof Error?e.message:'Could not load referrals')}},[]);
 useEffect(()=>{void load()},[load]);
 async function reward(r:Referral){if(!confirm(localizeText('Mark the reward for {{name}} as given? Grant the reward itself (for example a premium plan) first.').replace('{{name}}',r.referrer)))return;
  try{await admin(`/referrals/${r.id}/reward`,'POST');await load()}catch(e){setError(e instanceof Error?e.message:'Could not save')}}
 return <section className="acPanel"><h2>{localizeText('Referral rewards')}</h2><p>{localizeText('A referral qualifies when the invited friend publishes their first celebration. Give the reward, then mark it here so it is recorded in the audit log.')}</p>
  {error&&<p role="alert">{localizeText(error)}</p>}
  {rows.length===0?<p>{localizeText('No referrals yet.')}</p>:<div className="acTable"><table><thead><tr><th>{localizeText('Referrer')}</th><th>{localizeText('Friend')}</th><th>{localizeText('Joined')}</th><th>{localizeText('Status')}</th><th/></tr></thead>
   <tbody>{rows.map(r=><tr key={r.id}><td>{r.referrer}<br/><small>{r.referrerEmail}</small></td><td>{r.friend}</td><td>{new Date(r.joinedAt).toLocaleDateString()}</td><td>{r.status}</td>
    <td>{r.status==='QUALIFIED'&&<button type="button" onClick={()=>void reward(r)}>{localizeText('Mark rewarded')}</button>}</td></tr>)}</tbody></table></div>}
 </section>;
}

type ShowcaseRow={eventId:string;eventTitle:string;hostName:string;displayTitle:string;blurb:string|null;city:string|null;status:string;submittedAt:string;wishes:{id:string;guestName:string;message:string}[]};
export function AdminShowcase(){useLocalizationRender();
 const [rows,setRows]=useState<ShowcaseRow[]>([]),[status,setStatus]=useState('PENDING'),[error,setError]=useState('');
 const load=useCallback(async()=>{try{setRows(await admin(`/showcase?status=${status}`))}catch(e){setError(e instanceof Error?e.message:'Could not load')}},[status]);
 useEffect(()=>{void load()},[load]);
 async function decide(r:ShowcaseRow,decision:string){const note=decision==='REJECTED'?prompt(localizeText('Reason shown to the host (optional)'))??'':'';
  try{await admin(`/showcase/${r.eventId}/review`,'POST',{decision,note});await load()}catch(e){setError(e instanceof Error?e.message:'Could not save')}}
 return <section className="acPanel"><h2>{localizeText('Showcase submissions')}</h2><p>{localizeText('Hosts submit celebrations with up to six approved wishes. Nothing is public until you approve it. Check that wishes contain nothing private or inappropriate.')}</p>
  {error&&<p role="alert">{localizeText(error)}</p>}
  <div role="tablist">{['PENDING','APPROVED','REJECTED','WITHDRAWN'].map(s=><button key={s} type="button" role="tab" aria-selected={status===s} onClick={()=>setStatus(s)}>{localizeText(s.charAt(0)+s.slice(1).toLowerCase())}</button>)}</div>
  {rows.length===0?<p>{localizeText('Nothing here.')}</p>:rows.map(r=><article key={r.eventId} style={{border:'1px solid #e6dfe2',borderRadius:12,padding:14,margin:'10px 0'}}>
   <strong>{r.displayTitle}</strong> <small>({r.eventTitle} · {r.hostName}{r.city?` · ${r.city}`:''})</small>{r.blurb&&<p>{r.blurb}</p>}
   <ul>{r.wishes.map(w=><li key={w.id}><strong>{w.guestName}</strong>: {w.message}</li>)}</ul>
   {r.status==='PENDING'&&<div><button type="button" onClick={()=>void decide(r,'APPROVED')}>{localizeText('Approve')}</button> <button type="button" onClick={()=>void decide(r,'REJECTED')}>{localizeText('Reject')}</button></div>}</article>)}
 </section>;
}

type Preset={code:string;name:string;description:string;modules:string[]};
type Pick={id:string;label:string};
/** One-click feature packs for the platform, one host, every host or one event. Presets only switch features on. */
export function AdminPresets(){useLocalizationRender();
 const [presets,setPresets]=useState<Preset[]>([]),[preset,setPreset]=useState('WEDDING'),[scope,setScope]=useState('HOST'),[query,setQuery]=useState(''),[options,setOptions]=useState<Pick[]>([]),[target,setTarget]=useState<Pick|null>(null),[result,setResult]=useState(''),[error,setError]=useState(''),[busy,setBusy]=useState(false);
 useEffect(()=>{admin('/presets').then(setPresets).catch(e=>setError(e.message))},[]);
 useEffect(()=>{setTarget(null);setOptions([]);if(!['HOST','EVENT'].includes(scope)||query.trim().length<2)return;const t=setTimeout(()=>{
  admin(scope==='HOST'?`/hosts?search=${encodeURIComponent(query)}`:`/events?search=${encodeURIComponent(query)}`).then((rows:Record<string,string>[])=>setOptions(rows.slice(0,8).map(r=>({id:r.id,label:scope==='HOST'?`${r.username??''} · ${r.email??''}`:`${r.title} · ${r.host_name??''}`})))).catch(()=>{})},300);return()=>clearTimeout(t)},[query,scope]);
 const chosen=presets.find(p=>p.code===preset);
 async function apply(){if(scope==='ALL_HOSTS'&&!confirm(localizeText('Switch these features on for every host?')))return;setBusy(true);setError('');setResult('');
  try{const r=await admin(`/presets/${preset}/apply`,'POST',{scope,hostId:scope==='HOST'?target?.id:undefined,eventId:scope==='EVENT'?target?.id:undefined}) as {changed:number;skipped:string[]};
   setResult(localizeText('{{n}} switches turned on.').replace('{{n}}',String(r.changed))+(r.skipped.length?' '+localizeText('Not event-level: {{m}}.').replace('{{m}}',r.skipped.join(', ')):''))}
  catch(e){setError(e instanceof Error?e.message:'Could not apply')}finally{setBusy(false)}}
 return <section className="acPanel"><h2>{localizeText('Feature presets')}</h2><p>{localizeText('Turn on a whole set of features in one step. Presets only switch features on; to switch something off, use the individual module switches below. Every apply is recorded in Activity & audit.')}</p>
  {error&&<p role="alert">{localizeText(error)}</p>}
  <div style={{display:'grid',gap:10,gridTemplateColumns:'repeat(auto-fit,minmax(220px,1fr))'}}>
   <label>{localizeText('Preset')}<select value={preset} onChange={e=>setPreset(e.target.value)}>{presets.map(p=><option key={p.code} value={p.code}>{localizeText(p.name)}</option>)}</select></label>
   <label>{localizeText('Apply to')}<select value={scope} onChange={e=>{setScope(e.target.value);setQuery('')}}><option value="PLATFORM">{localizeText('The platform (master switches)')}</option><option value="HOST">{localizeText('One host')}</option><option value="ALL_HOSTS">{localizeText('Every host')}</option><option value="EVENT">{localizeText('One event')}</option></select></label>
   {['HOST','EVENT'].includes(scope)&&<label>{localizeText(scope==='HOST'?'Find host':'Find event')}<input value={target?target.label:query} onChange={e=>{setTarget(null);setQuery(e.target.value)}} placeholder={localizeText('Type at least 2 letters')}/></label>}</div>
  {options.length>0&&!target&&<ul style={{listStyle:'none',padding:0,margin:'8px 0',display:'grid',gap:4}}>{options.map(o=><li key={o.id}><button type="button" onClick={()=>{setTarget(o);setOptions([])}}>{o.label}</button></li>)}</ul>}
  {chosen&&<p><small>{localizeText(chosen.description)} <b>{chosen.modules.length}</b> {localizeText('modules')}: {chosen.modules.join(', ')}</small></p>}
  <button type="button" disabled={busy||(['HOST','EVENT'].includes(scope)&&!target)} onClick={()=>void apply()}>{busy?localizeText('Applying…'):localizeText('Apply preset')}</button>
  {result&&<p role="status">{result}</p>}
  <p><small>{localizeText('Tip: a feature works only when the platform, the host and (for event tools) the event are all switched on. To set up a new wedding host, apply “Wedding pack” to the platform, the host, then their event.')}</small></p></section>;
}

type Growth={days:number;events:Record<string,number>;registrations:number;published:number;tryClaimedThenPublished:number;offerClaimedThenPublished:number;referralsAttributed:number;referralsQualified:number;whatsappSent:number};
/** Did the acquisition features work? Business events only (no tracking cookies). */
export function AdminGrowthResults(){useLocalizationRender();
 const [days,setDays]=useState(30),[g,setG]=useState<Growth|null>(null),[error,setError]=useState('');
 useEffect(()=>{admin(`/growth-results?days=${days}`).then(setG).catch(e=>setError(e.message))},[days]);
 const e=g?.events??{};
 const funnel:[string,number|undefined][]=[['Visitors who started “Try it free”',e.TRY_STARTED],['…who created an account and kept their draft',e.TRY_CLAIMED],['…who then published a celebration',g?.tryClaimedThenPublished]];
 const rows:[string,number|undefined][]=[['New host accounts',g?.registrations],['Celebrations published',g?.published],['Status & Story cards shared',e.STORY_CARD_SHARED],['First-celebration-free offers claimed',e.OFFER_CLAIMED],['…of those who published',g?.offerClaimedThenPublished],['Referral sign-ups',g?.referralsAttributed],['Referrals that published',g?.referralsQualified],['Partner handovers accepted',e.HANDOVER_ACCEPTED],['Group gifts started',e.COLLECTION_CREATED],['Showcase celebrations approved',e.SHOWCASE_APPROVED],['WhatsApp messages delivered',g?.whatsappSent],['Guest passes scanned at the door',e.PASS_SCANNED]];
 return <section className="acPanel"><h2>{localizeText('Growth results')}</h2><p>{localizeText('How the customer-acquisition features are performing. Counted from what actually happened (accounts, publishes, shares, claims), not from cookies.')}</p>
  {error&&<p role="alert">{localizeText(error)}</p>}
  <label>{localizeText('Period')}<select value={days} onChange={x=>setDays(Number(x.target.value))}><option value={7}>{localizeText('Last 7 days')}</option><option value={30}>{localizeText('Last 30 days')}</option><option value={90}>{localizeText('Last 90 days')}</option></select></label>
  {g&&<><h3>{localizeText('Try without an account')}</h3><ol>{funnel.map(([l,v],i)=><li key={l}>{localizeText(l)}: <b>{v??0}</b>{i>0&&funnel[i-1][1]?` (${Math.round(((v??0)/(funnel[i-1][1]||1))*100)}%)`:''}</li>)}</ol>
   <div className="acTable"><table><tbody>{rows.map(([l,v])=><tr key={l}><td>{localizeText(l)}</td><td style={{textAlign:'right'}}><b>{v??0}</b></td></tr>)}</tbody></table></div></>}</section>;
}
