'use client';
import {useCallback,useEffect,useState} from 'react';
import {useParams} from 'next/navigation';
import {localizeText} from '../../../../lib/localization.mjs';
import {useLocalizationRender} from '../../../../components/LocalizationProvider';
import {hostRequest} from '../../../../lib/host-api.mjs';
import '../../../../components/gift-books.css';
import '../../../../components/eventops.css';

type Update={id:string;message:string;postedAt:string;postedBy:string|null};
type View={updates:Update[];stream:{url:string;label:string|null}|null};
const QUICK=['Dinner is now being served','The ceremony begins in 15 minutes','Buses back to the hotel leave at 10 pm','Please gather for the group photo'];
const ago=(v:string)=>{const m=Math.round((Date.now()-new Date(v).getTime())/60000);return m<1?localizeText('just now'):m<60?localizeText('{{n}} min ago').replace('{{n}}',String(m)):new Intl.DateTimeFormat(undefined,{dateStyle:'medium',timeStyle:'short'}).format(new Date(v))};

export default function LiveUpdates(){useLocalizationRender();
 const {id}=useParams();const eventId=String(id??'');const route=`/v1/events/${eventId}/live-updates`;
 const [v,setV]=useState<View|null>(null),[msg,setMsg]=useState(''),[url,setUrl]=useState(''),[label,setLabel]=useState(''),[error,setError]=useState(''),[notice,setNotice]=useState(''),[busy,setBusy]=useState(false);
 const load=useCallback(async()=>{try{const d=await hostRequest(fetch,route) as View;setV(d);setUrl(d.stream?.url??'');setLabel(d.stream?.label??'');setError('')}catch(e){setError(e instanceof Error?e.message:'Could not load')}},[route]);
 useEffect(()=>{if(/^[0-9a-f-]{36}$/i.test(eventId))void load()},[eventId,load]);
 async function post(text:string){if(!text.trim())return;setBusy(true);setError('');try{await hostRequest(fetch,route,{method:'POST',payload:{message:text}});setMsg('');setNotice('Posted. Guests see it on the invitation and the live wall.');await load()}catch(e){setError(e instanceof Error?e.message:'Could not post')}finally{setBusy(false)}}
 async function remove(u:Update){try{await hostRequest(fetch,`${route}/${u.id}/remove`,{method:'POST'});await load()}catch(e){setError(e instanceof Error?e.message:'Could not remove')}}
 async function saveStream(e:React.FormEvent){e.preventDefault();try{await hostRequest(fetch,`${route}/stream`,{method:'PUT',payload:{url,label}});setNotice(url?'Stream link saved. It appears on the invitation.':'Stream link removed.');await load()}catch(err){setError(err instanceof Error?err.message:'Could not save')}}
 return <main className="focusPage gbPage">
  <header className="focusPageHeading"><div><p className="focusEyebrow">{localizeText('Keep everyone in the loop')}</p><h1>{localizeText('Live updates')}</h1>
   <p>{localizeText('Short announcements appear at the top of your invitation for 24 hours and on the live wall for 6 hours.')}</p></div></header>
  {error&&<p className="focusError" role="alert">{localizeText(error)}</p>}
  {notice&&<p className="gbNotice" role="status">{localizeText(notice)}</p>}
  <form className="gbEntry" onSubmit={e=>{e.preventDefault();void post(msg)}}><h2>{localizeText('Post an update')}</h2>
   <label>{localizeText('Message')}<input value={msg} maxLength={200} onChange={e=>setMsg(e.target.value)} placeholder={localizeText('e.g. Dinner is now being served')}/><small>{msg.length}/200</small></label>
   <div className="gbChips">{QUICK.map(t=><button type="button" key={t} onClick={()=>setMsg(localizeText(t))}>{localizeText(t)}</button>)}</div>
   <div className="gbEntryActions"><button className="focusPrimary" disabled={busy||!msg.trim()}>{localizeText('Post to guests')}</button></div></form>
  {v&&<section><h2 className="gbSubhead">{localizeText('Posted')}</h2>{v.updates.length===0?<p className="gbEmpty">{localizeText('Nothing posted yet.')}</p>:
   <ul className="gbRows">{v.updates.map(u=><li key={u.id}><div><span className="gbWho"><strong>{u.message}</strong><small>{ago(u.postedAt)}{u.postedBy?` · ${u.postedBy}`:''}</small></span><button type="button" className="gbPlain" onClick={()=>void remove(u)}>{localizeText('Remove')}</button></div></li>)}</ul>}</section>}
  {v&&<form className="gbEntry" onSubmit={saveStream}><h2>{localizeText('Livestream for relatives far away')}</h2>
   <div className="gbEntryRow"><label className="gbGrow">{localizeText('Stream link')}<input type="url" value={url} maxLength={500} onChange={e=>setUrl(e.target.value)} placeholder="https://youtube.com/live/…"/></label>
    <label>{localizeText('Button text')}<input value={label} maxLength={80} onChange={e=>setLabel(e.target.value)} placeholder={localizeText('Watch live')}/></label></div>
   <small className="gbFine">{localizeText('YouTube, Zoom, Google Meet or any https link. Leave empty to remove.')}</small>
   <div className="gbEntryActions"><button className="focusSecondary">{localizeText('Save stream link')}</button></div></form>}
 </main>;
}
