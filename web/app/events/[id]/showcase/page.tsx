'use client';
import {useCallback,useEffect,useState} from 'react';
import {useParams} from 'next/navigation';
import {localizeText} from '../../../../lib/localization.mjs';
import {useLocalizationRender} from '../../../../components/LocalizationProvider';
import {hostRequest} from '../../../../lib/host-api.mjs';
import '../../../../components/gift-books.css';
import '../../../../components/acquisition.css';

type Wish={id:string;guestName:string;message:string;photo:boolean};
type View={status:string;displayTitle:string;blurb:string|null;city:string|null;featuredWishIds:string[];reviewNote:string|null;approvedWishes:Wish[];published:boolean};
const STATUS:Record<string,[string,string]>={NONE:['Not shared','neutral'],PENDING:['Waiting for review','warning'],APPROVED:['Live in the showcase','good'],REJECTED:['Not accepted','danger'],WITHDRAWN:['Withdrawn','neutral']};

export default function ShowcaseSubmit(){useLocalizationRender();
 const {id}=useParams();const eventId=String(id??'');const route=`/v1/events/${eventId}/showcase`;
 const [v,setV]=useState<View|null>(null),[f,setF]=useState({displayTitle:'',blurb:'',city:'',wishes:[] as string[]}),[consent,setConsent]=useState(false),[error,setError]=useState(''),[busy,setBusy]=useState(false);
 const load=useCallback(async()=>{try{const d=await hostRequest(fetch,route) as View;setV(d);setF({displayTitle:d.displayTitle??'',blurb:d.blurb??'',city:d.city??'',wishes:d.featuredWishIds});setError('')}catch(e){setError(e instanceof Error?e.message:'Could not load')}},[route]);
 useEffect(()=>{if(/^[0-9a-f-]{36}$/i.test(eventId))void load()},[eventId,load]);
 const toggle=(w:string)=>setF(x=>({...x,wishes:x.wishes.includes(w)?x.wishes.filter(y=>y!==w):x.wishes.length>=6?x.wishes:[...x.wishes,w]}));
 async function submit(e:React.FormEvent){e.preventDefault();if(!consent){setError('Please confirm you have permission to share these wishes');return}setBusy(true);setError('');
  try{await hostRequest(fetch,route,{method:'PUT',payload:{displayTitle:f.displayTitle,blurb:f.blurb||null,city:f.city||null,featuredWishIds:f.wishes}});setConsent(false);await load()}catch(err){setError(err instanceof Error?err.message:'Could not submit')}finally{setBusy(false)}}
 async function withdraw(){if(!confirm(localizeText('Remove this celebration from the showcase?')))return;try{await hostRequest(fetch,`${route}/withdraw`,{method:'POST'});await load()}catch(err){setError(err instanceof Error?err.message:'Could not withdraw')}}
 const st=STATUS[v?.status??'NONE'];
 return <main className="focusPage gbPage">
  <header className="focusPageHeading"><div><p className="focusEyebrow">{localizeText('Inspire other hosts')}</p><h1>{localizeText('Share in the showcase')}</h1>
   <p>{localizeText('Show your celebration on the GiftWise showcase with a few favourite wishes. Only first names appear; amounts, contacts and private photos never do. The GiftWise team reviews it first.')}</p></div></header>
  {error&&<p className="focusError" role="alert">{localizeText(error)}</p>}
  {v&&<p className={`shStatus shStatus--${st[1]}`}>{localizeText(st[0])}{v.reviewNote?` — ${v.reviewNote}`:''}</p>}
  {v&&!v.published&&<p className="gbQueued">{localizeText('Publish your invitation before sharing it.')}</p>}
  {v&&v.published&&<form className="gbEntry" onSubmit={submit}>
   <div className="gbEntryRow"><label className="gbGrow">{localizeText('Public title')}<input value={f.displayTitle} maxLength={120} onChange={e=>setF({...f,displayTitle:e.target.value})} placeholder={localizeText('e.g. Meera & Arjun’s wedding')}/></label>
    <label>{localizeText('City')}<input value={f.city} maxLength={80} onChange={e=>setF({...f,city:e.target.value})}/></label></div>
   <label>{localizeText('One line about it (optional)')}<input value={f.blurb} maxLength={300} onChange={e=>setF({...f,blurb:e.target.value})} placeholder={localizeText('A monsoon wedding by the backwaters')}/></label>
   <fieldset className="shWishes"><legend>{localizeText('Featured wishes (up to 6)')} — {f.wishes.length}/6</legend>{v.approvedWishes.length===0?<p className="gbFine">{localizeText('No approved wishes yet.')}</p>:
    v.approvedWishes.map(w=><label key={w.id} className="gbCheck"><input type="checkbox" checked={f.wishes.includes(w.id)} disabled={!f.wishes.includes(w.id)&&f.wishes.length>=6} onChange={()=>toggle(w.id)}/><span><strong>{w.guestName.split(' ')[0]}</strong> {w.message}{w.photo?' 📷':''}</span></label>)}</fieldset>
   <label className="gbCheck"><input type="checkbox" checked={consent} onChange={e=>setConsent(e.target.checked)}/>{localizeText('I’m happy to share this, and my guests would be comfortable with their wishes being shown publicly.')}</label>
   <div className="gbEntryActions"><button className="focusPrimary" disabled={busy}>{v.status==='NONE'||v.status==='WITHDRAWN'?localizeText('Submit for review'):localizeText('Update and resubmit')}</button>
    {(v.status==='PENDING'||v.status==='APPROVED')&&<button type="button" className="gbDanger" onClick={()=>void withdraw()}>{localizeText('Withdraw')}</button>}</div></form>}
 </main>;
}
