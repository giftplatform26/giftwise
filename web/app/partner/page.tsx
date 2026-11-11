'use client';
import {useCallback,useEffect,useState} from 'react';
import Link from 'next/link';
import {localizeText} from '../../lib/localization.mjs';
import {useLocalizationRender} from '../../components/LocalizationProvider';
import {hostRequest,bootstrapCsrf} from '../../lib/host-api.mjs';
import {csrfToken} from '../../lib/gift-api.mjs';
import {slugify} from '../../lib/acquisition.mjs';
import '../../components/gift-books.css';
import '../../components/functions.css';
import '../../components/acquisition.css';

type Profile={exists:boolean;slug:string|null;businessName:string|null;kind:string;city:string|null;phone:string|null;website:string|null;brandColor:string;bio:string|null;logoDataUrl:string|null};
type Client={eventId:string;title:string;occasion:string;eventDate:string|null;status:string;clientLabel:string|null;handover:string;owned:boolean;rsvpYes:number};
const KINDS:[string,string][]=[['PLANNER','Event planner'],['VENUE','Venue / banquet hall'],['DECORATOR','Decorator'],['CATERER','Caterer'],['PHOTOGRAPHER','Photographer'],['OTHER','Other']];
const HANDOVER:Record<string,string>={NOT_STARTED:'Not handed over',INVITED:'Invite sent',HANDED_OVER:'With the client'};

export default function PartnerWorkspace(){useLocalizationRender();
 const [p,setP]=useState<Profile|null>(null),[clients,setClients]=useState<Client[]>([]),[error,setError]=useState(''),[notice,setNotice]=useState(''),[busy,setBusy]=useState(false),[edit,setEdit]=useState(false),[link,setLink]=useState<{eventId:string;url:string}|null>(null);
 const [f,setF]=useState({businessName:'',slug:'',kind:'PLANNER',city:'',phone:'',website:'',brandColor:'#683C55',bio:''});
 const load=useCallback(async()=>{try{const pr=await hostRequest(fetch,'/v1/me/partner') as Profile;setP(pr);setEdit(!pr.exists);
  setF({businessName:pr.businessName??'',slug:pr.slug??'',kind:pr.kind,city:pr.city??'',phone:pr.phone??'',website:pr.website??'',brandColor:pr.brandColor,bio:pr.bio??''});
  setClients(await hostRequest(fetch,'/v1/me/partner/clients') as Client[]);setError('')}catch(e){setError(e instanceof Error?e.message:'Could not load')}},[]);
 useEffect(()=>{void load()},[load]);
 const set=(k:string,v:string)=>setF(x=>({...x,[k]:v}));
 async function save(e:React.FormEvent){e.preventDefault();setBusy(true);setError('');try{await hostRequest(fetch,'/v1/me/partner',{method:'PUT',payload:{...f,slug:f.slug||slugify(f.businessName)}});setNotice('Profile saved.');await load()}catch(err){setError(err instanceof Error?err.message:'Could not save')}finally{setBusy(false)}}
 async function logo(file?:File){if(!file)return;setBusy(true);try{await bootstrapCsrf(fetch);const b=new FormData();b.append('file',file);const r=await fetch('/backend/api/v1/me/partner/logo',{method:'POST',credentials:'include',headers:{'X-XSRF-TOKEN':csrfToken(document.cookie)},body:b});
  if(!r.ok){const j=await r.json().catch(()=>({}));throw Error(j.error||'Could not upload the logo')}await load()}catch(err){setError(err instanceof Error?err.message:'Could not upload')}finally{setBusy(false)}}
 async function handover(c:Client){const label=prompt(localizeText('Client name (only you see this)'),c.clientLabel??'');if(label===null)return;setError('');
  try{const r=await hostRequest(fetch,`/v1/events/${c.eventId}/partner/handover`,{method:'POST',payload:{clientLabel:label}}) as {token:string};setLink({eventId:c.eventId,url:`${window.location.origin}/handover/${r.token}`});await load()}
  catch(err){setError(err instanceof Error?err.message:'Could not create the link')}}
 const page=p?.slug&&typeof window!=='undefined'?`${window.location.origin}/p/${p.slug}`:'';

 return <main className="focusPage gbPage">
  <header className="focusPageHeading"><div><p className="focusEyebrow">{localizeText('For planners, venues and vendors')}</p><h1>{localizeText('Partner workspace')}</h1>
   <p>{localizeText('Set up celebrations for your clients, hand each one over when ready, and stay on to help. Guests see your name on the invitation.')}</p></div>
   <Link className="focusPrimary" href="/events/new">{localizeText('New client celebration')}</Link></header>
  {error&&<p className="focusError" role="alert">{localizeText(error)}</p>}
  {notice&&<p className="gbNotice" role="status">{localizeText(notice)}</p>}
  {p&&!edit&&<section className="ptCard"><div className="ptBrand" style={{borderColor:p.brandColor}}>{p.logoDataUrl?<img src={p.logoDataUrl} alt="" width={64} height={64}/>:<span className="ptMono" style={{background:p.brandColor}}>{(p.businessName??'?').slice(0,1)}</span>}
   <div><h2>{p.businessName}</h2><p className="gbFine">{[KINDS.find(k=>k[0]===p.kind)?.[1],p.city].filter(Boolean).map(x=>localizeText(String(x))).join(' · ')}</p></div></div>
   <div className="gbEntryActions"><a className="focusSecondary gbButtonLink" href={page} target="_blank" rel="noopener noreferrer">{localizeText('View your public page')}</a><button type="button" className="gbPlain" onClick={()=>setEdit(true)}>{localizeText('Edit profile')}</button></div></section>}
  {edit&&<form className="gbEntry" onSubmit={save}><h2>{p?.exists?localizeText('Edit your business profile'):localizeText('Create your business profile')}</h2>
   <div className="gbEntryRow"><label className="gbGrow">{localizeText('Business name')}<input value={f.businessName} maxLength={120} onChange={e=>{set('businessName',e.target.value);if(!p?.exists)set('slug',slugify(e.target.value))}}/></label>
    <label>{localizeText('Type')}<select value={f.kind} onChange={e=>set('kind',e.target.value)}>{KINDS.map(([k,l])=><option key={k} value={k}>{localizeText(l)}</option>)}</select></label></div>
   <label>{localizeText('Page address')}<div className="ptSlug"><span>/p/</span><input value={f.slug} maxLength={40} onChange={e=>set('slug',e.target.value.toLowerCase().replace(/[^a-z0-9-]/g,''))}/></div></label>
   <div className="gbEntryRow"><label>{localizeText('City')}<input value={f.city} maxLength={80} onChange={e=>set('city',e.target.value)}/></label>
    <label>{localizeText('Phone (+91…)')}<input value={f.phone} maxLength={20} onChange={e=>set('phone',e.target.value)}/></label>
    <label className="gbGrow">{localizeText('Website (https://)')}<input value={f.website} maxLength={300} onChange={e=>set('website',e.target.value)}/></label></div>
   <label>{localizeText('About your business')}<textarea rows={3} maxLength={500} value={f.bio} onChange={e=>set('bio',e.target.value)}/></label>
   <label className="ptColor">{localizeText('Brand colour')}<input type="color" value={f.brandColor} onChange={e=>set('brandColor',e.target.value.toUpperCase())}/></label>
   {p?.exists&&<label className="focusSecondary rdUpload">{localizeText('Upload logo')}<input type="file" accept="image/png,image/jpeg,image/webp" onChange={e=>void logo(e.target.files?.[0])}/></label>}
   <div className="gbEntryActions"><button className="focusPrimary" disabled={busy}>{localizeText('Save profile')}</button>{p?.exists&&<button type="button" className="gbPlain" onClick={()=>setEdit(false)}>{localizeText('Cancel')}</button>}</div></form>}
  {link&&<section className="focusPanel chLink"><h2>{localizeText('Handover link')}</h2><p>{localizeText('Send this to your client. When they accept, the celebration becomes theirs and they add their own UPI or bank account for gifts. You stay on as their planner. Valid for 14 days.')}</p>
   <div className="lwCopy"><input readOnly value={link.url} onFocus={e=>e.currentTarget.select()}/><a className="focusSecondary gbButtonLink" href={`https://wa.me/?text=${encodeURIComponent(link.url)}`} target="_blank" rel="noopener noreferrer">{localizeText('WhatsApp')}</a></div></section>}
  {p?.exists&&<section className="gbList"><h2 className="gbSubhead">{localizeText('Client celebrations')}</h2>
   {clients.length===0?<p className="gbEmpty">{localizeText('Create a celebration for a client, set it up, then hand it over from here.')}</p>:
   <ul className="gbRows">{clients.map(c=><li key={c.eventId}><div>
    <span className="gbWho"><strong>{c.title}</strong><small>{[c.clientLabel,c.eventDate&&new Intl.DateTimeFormat(undefined,{dateStyle:'medium'}).format(new Date(`${c.eventDate}T12:00:00`)),localizeText(HANDOVER[c.handover]),`${c.rsvpYes} ${localizeText('coming')}`].filter(Boolean).join(' · ')}</small></span>
    <span className="chMemberTools"><Link href={c.owned?`/events/${c.eventId}`:`/helping`}>{localizeText('Open')}</Link>{c.owned&&c.handover!=='HANDED_OVER'&&<button type="button" className="focusSecondary" onClick={()=>void handover(c)}>{localizeText(c.handover==='INVITED'?'New handover link':'Hand over')}</button>}</span></div></li>)}</ul>}</section>}
 </main>;
}
