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
