'use client';
import {useEffect,useState} from 'react';
import {useParams} from 'next/navigation';
import {localizeText} from '../../../lib/localization.mjs';
import {useLocalizationRender} from '../../../components/LocalizationProvider';
import {clock} from '../../../lib/eventops.mjs';
import '../../../components/eventops.css';

type Item={id:string;runDate:string|null;startsAt:string;title:string;ownerName:string|null;location:string|null;note:string|null;functionName:string|null;done:boolean};
type Sheet={eventTitle:string;eventDate:string|null;items:Item[]};
const dayTitle=(d:string|null)=>d?new Intl.DateTimeFormat(undefined,{weekday:'long',day:'numeric',month:'long'}).format(new Date(`${d}T12:00:00`)):localizeText('On the day');

/** Read-only run sheet for vendors; refreshes every minute so last-minute changes show up. */
export default function SharedRunSheet(){useLocalizationRender();
 const {token}=useParams();const t=String(token??'');
 const [s,setS]=useState<Sheet|null>(null),[gone,setGone]=useState(false);
 useEffect(()=>{if(!/^[0-9a-f-]{36}$/i.test(t)){setGone(true);return}let active=true;
  const load=()=>fetch(`/backend/api/public/run-sheet/${t}`,{cache:'no-store'}).then(r=>{if(r.status===404){if(active)setGone(true);return null}return r.ok?r.json():null}).then(v=>{if(active&&v)setS(v)}).catch(()=>{});
  void load();const timer=setInterval(load,60000);return()=>{active=false;clearInterval(timer)}},[t]);
 if(gone)return <main className="eoPublic"><h1>{localizeText('This run sheet is not shared right now')}</h1><p>{localizeText('Ask the host for a new link.')}</p></main>;
 if(!s)return <main className="eoPublic"><p role="status">{localizeText('Opening the run sheet…')}</p></main>;
 const days=[...new Set(s.items.map(i=>i.runDate??''))];
 return <main className="eoPublic"><p className="focusEyebrow">{localizeText('Run sheet · view only')}</p><h1>{s.eventTitle}</h1>
  {days.map(d=><section key={d||'none'}><h2>{dayTitle(d||null)}</h2><ol className="eoRun">{s.items.filter(i=>(i.runDate??'')===d).map(i=><li key={i.id} className={i.done?'is-done':''}>
   <span className="eoRunTime">{clock(i.startsAt)}</span><div className="eoTaskBody"><strong>{i.title}</strong><small>{[i.ownerName,i.location,i.functionName,i.note].filter(Boolean).join(' · ')}</small></div></li>)}</ol></section>)}
  <p className="eoFine">{localizeText('Updates automatically every minute.')}</p></main>;
}
