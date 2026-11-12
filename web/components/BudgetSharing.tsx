'use client';
import {useCallback,useEffect,useState} from 'react';
import {localizeText} from '../lib/localization.mjs';
import {useLocalizationRender} from './LocalizationProvider';
import {hostRequest} from '../lib/host-api.mjs';

type Member={userId:string;name:string;access:string;joinedAt:string};
type Invite={id:string;label:string;access:string;status:string;expiresAt:string;acceptedBy:string|null};

/** Owner-only panel on the budget page: share the budget and vendor bookings with family, view-only or with edit rights. Hidden when the feature is off. */
export default function BudgetSharing({eventId}:{eventId:string}){useLocalizationRender();
 const route=`/v1/events/${eventId}/budget/sharing`;
 const [data,setData]=useState<{members:Member[];invites:Invite[]}|null>(null),[off,setOff]=useState(false),[error,setError]=useState(''),[form,setForm]=useState({label:'',access:'VIEW'}),[link,setLink]=useState('');
 const load=useCallback(async()=>{try{setData(await hostRequest(fetch,route) as {members:Member[];invites:Invite[]})}catch{setOff(true)}},[route]);
 useEffect(()=>{void load()},[load]);
 if(off||!data)return null;
 async function call(fn:()=>Promise<unknown>){setError('');try{await fn();await load()}catch(e){setError(e instanceof Error?e.message:'Something went wrong')}}
 const create=(e:React.FormEvent)=>{e.preventDefault();void call(async()=>{const r=await hostRequest(fetch,`${route}/invites`,{method:'POST',payload:form}) as {token:string};setLink(`${window.location.origin}/budget-invite/${r.token}`);setForm({label:'',access:'VIEW'})})};
 const open=data.invites.filter(i=>i.status==='OPEN');
 return <section className="gbEntry"><h2>{localizeText('Share with family')}</h2>
  <p className="gbFine">{localizeText('Let family see the budget and vendor bookings, or help update them. Co-hosts do not see the budget unless you share it here.')}</p>
  {error&&<p className="focusError" role="alert">{localizeText(error)}</p>}
  {data.members.length>0&&<ul className="gbRows">{data.members.map(m=><li key={m.userId}><div><span className="gbWho"><strong>{m.name}</strong></span>
   <span className="hhQuick"><select value={m.access} aria-label={localizeText('Access for {{n}}').replace('{{n}}',m.name)} onChange={e=>void call(()=>hostRequest(fetch,`${route}/members/${m.userId}/access`,{method:'POST',payload:{access:e.target.value}}))}>
    <option value="VIEW">{localizeText('Can view')}</option><option value="EDIT">{localizeText('Can edit')}</option></select>
    <button type="button" className="gbPlain" onClick={()=>{if(confirm(localizeText('Stop sharing the budget with {{n}}?').replace('{{n}}',m.name)))void call(()=>hostRequest(fetch,`${route}/members/${m.userId}/remove`,{method:'POST'}))}}>{localizeText('Remove')}</button></span></div></li>)}</ul>}
  <form className="gbEntryRow" onSubmit={create}><label className="gbGrow">{localizeText('Who is this link for?')}<input required value={form.label} maxLength={80} onChange={e=>setForm({...form,label:e.target.value})} placeholder={localizeText('e.g. Mom')}/></label>
   <label>{localizeText('Access')}<select value={form.access} onChange={e=>setForm({...form,access:e.target.value})}><option value="VIEW">{localizeText('Can view')}</option><option value="EDIT">{localizeText('Can edit')}</option></select></label>
   <button className="focusSecondary">{localizeText('Create link')}</button></form>
  {link&&<p className="gbNotice" role="status">{localizeText('Send this link (valid 7 days, works once). They sign in to GiftWise to open it:')} <code>{link}</code>{' '}
   <button type="button" className="gbPlain" onClick={()=>void navigator.clipboard?.writeText(link)}>{localizeText('Copy')}</button>{' '}
   <a href={`https://wa.me/?text=${encodeURIComponent(localizeText('I have shared our celebration budget with you on GiftWise: {{u}}').replace('{{u}}',link))}`} target="_blank" rel="noopener noreferrer">{localizeText('Send on WhatsApp')}</a></p>}
  {open.length>0&&<p className="gbFine">{localizeText('Open links')}: {open.map(i=><span key={i.id}>{i.label} ({localizeText(i.access==='EDIT'?'can edit':'can view')}) <button type="button" className="gbPlain" onClick={()=>void call(()=>hostRequest(fetch,`${route}/invites/${i.id}/revoke`,{method:'POST'}))}>{localizeText('Cancel')}</button> </span>)}</p>}
 </section>;
}
