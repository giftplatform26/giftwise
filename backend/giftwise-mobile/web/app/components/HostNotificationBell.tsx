'use client';
/* localization:instrumented */
import {localize,localizeText} from '../../lib/localization.mjs';
import {useLocalizationRender} from '../../components/LocalizationProvider';

import {useCallback,useEffect,useRef,useState} from 'react';
import Link from 'next/link';
import {Bell} from 'lucide-react';
type Item={id:string;eventId:string;eventTitle:string;subject:string;message:string;evidence:string;sourceKind:string;createdAt:string;read:boolean};
type Inbox={unread:number;entries:Item[]};
export default function HostNotificationBell(){useLocalizationRender();
 const [inbox,setInbox]=useState<Inbox>({unread:0,entries:[]});
 const [open,setOpen]=useState(false);const [error,setError]=useState('');
 const inFlight=useRef(false),failures=useRef(0);
 const load=useCallback(async()=>{
  if(inFlight.current||document.hidden)return;inFlight.current=true;
  try{const r=await fetch('/backend/api/v1/host/notifications?limit=15',{credentials:'include',cache:'no-store'});
   if(!r.ok){failures.current=Math.min(failures.current+1,4);if(r.status!==401)setError(`Notifications unavailable (${r.status})`);return;}
   const d:Inbox=await r.json();if(Array.isArray(d.entries)&&Number.isFinite(d.unread)){setInbox(d);setError('');failures.current=0;}
  }catch{failures.current=Math.min(failures.current+1,4);setError('Notifications unavailable');}finally{inFlight.current=false;}
 },[]);
 useEffect(()=>{let ticks=0;const refresh=()=>{if(!document.hidden&&++ticks>=2**failures.current){ticks=0;void load();}};void load();const timer=window.setInterval(refresh,30000);const visible=()=>{if(!document.hidden){ticks=0;void load();}};document.addEventListener('visibilitychange',visible);return()=>{window.clearInterval(timer);document.removeEventListener('visibilitychange',visible);}},[load]);
 async function markRead(id:string){try{
  const csrf=document.cookie.split('; ').find(c=>c.startsWith('XSRF-TOKEN='))?.split('=')[1];
  if(!csrf){setError('Session security token missing; refresh and sign in');return;}
  const r=await fetch(`/backend/api/v1/host/notifications/${encodeURIComponent(id)}/read`,
   {method:'POST',credentials:'include',headers:{'X-XSRF-TOKEN':decodeURIComponent(csrf)}});
  if(!r.ok)throw Error('Unable to mark as read');await load();
 }catch(e){setError(e instanceof Error?e.message:'Unable to mark as read');}}
 return <div style={{position:'relative'}}><button type="button" aria-label={localize("ui.fe9ec46fdae0300f",{defaultValue:"Notifications, {{value0}} unread",value0:(inbox.unread)})} aria-expanded={open} onClick={()=>setOpen(!open)} style={{display:'inline-flex',alignItems:'center',gap:8}}><Bell size={19}/> {localize("ui.3185b2520fd33953",{defaultValue:" Notifications "})}{inbox.unread>0&&<strong>({inbox.unread})</strong>}</button>
 {open&&<div role="region" aria-label={localize("ui.ea4477adde0ee9d2",{defaultValue:"Recent host notifications"})} style={{position:'absolute',right:0,top:'100%',zIndex:30,width:'min(380px,90vw)',maxHeight:400,overflowY:'auto',background:'white',color:'#252525',border:'1px solid #ccc',borderRadius:8,padding:12,boxShadow:'0 8px 25px #0002'}}>
 <h3>{localize("ui.c8561d85e71b50d1",{defaultValue:"Contribution alerts"})}</h3>{error&&<p role="alert">{localizeText(error)}</p>}
 {inbox.entries.length===0&&<p>{localize("ui.ca02c722b0986174",{defaultValue:"No notifications yet."})}</p>}
 {inbox.entries.map(n=><div key={n.id} style={{padding:'10px 0',borderBottom:'1px solid #ddd'}}>
 <strong>{n.eventTitle}: {n.subject||localize("ui.8d0b5abe7179e452",{defaultValue:"Contribution activity"})}</strong><p>{n.message}</p>
 <small>{new Date(n.createdAt).toLocaleString()} · {n.read?localize("ui.9b9a8d05a7ec353b",{defaultValue:"Read"}):localize("ui.1b9f384c1436f607",{defaultValue:"Unread"})}</small>
 <div style={{display:'flex',gap:12}}><Link href={`/events/${encodeURIComponent(n.eventId)}/contributions`}>{localize("ui.45e1eb06a4e2e64e",{defaultValue:"View report"})}</Link>{!n.read&&<button type="button" onClick={()=>void markRead(n.id)}>{localize("ui.b49c9b6cdf43b308",{defaultValue:"Mark read"})}</button>}</div>
 </div>)}<button type="button" onClick={()=>void load()}>{localize("ui.157e675e3b4fadd3",{defaultValue:"Refresh alerts"})}</button>
 </div>}</div>;
}
