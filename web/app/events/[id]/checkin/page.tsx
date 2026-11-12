'use client';
import {useCallback,useEffect,useMemo,useRef,useState} from 'react';
import {useParams} from 'next/navigation';
import {localizeText} from '../../../../lib/localization.mjs';
import {useLocalizationRender} from '../../../../components/LocalizationProvider';
import {hostRequest} from '../../../../lib/host-api.mjs';
import {searchGuests,percent} from '../../../../lib/eventops.mjs';
import '../../../../components/gift-books.css';
import '../../../../components/eventops.css';
import PassScanner from '../../../../components/PassScanner';

type Guest={key:string;name:string;expectedParty:number;source:string;arrived:boolean;arrivedParty:number|null;arrivedAt:string|null};
type Board={guests:Guest[];expectedPeople:number;arrivedGuests:number;arrivedPeople:number;walkIns:number;recent:{key:string;name:string;partySize:number;walkIn:boolean;at:string;by:string|null}[]};
const time=(v:string)=>new Intl.DateTimeFormat(undefined,{hour:'numeric',minute:'2-digit'}).format(new Date(v));

/** Built for a phone at the door: big search, one tap per family, quick party-size change, undo. */
export default function CheckIn(){useLocalizationRender();
 const {id}=useParams();const eventId=String(id??'');const route=`/v1/events/${eventId}/checkin`;
 const [b,setB]=useState<Board|null>(null),[q,setQ]=useState(''),[error,setError]=useState(''),[picked,setPicked]=useState<Guest|null>(null),[party,setParty]=useState(1),[toast,setToast]=useState<{text:string;key:string}|null>(null),[walkIn,setWalkIn]=useState(false);
 const search=useRef<HTMLInputElement>(null);
 const [passes,setPasses]=useState(false);
 useEffect(()=>{if(/^[0-9a-f-]{36}$/i.test(eventId))fetch(`/backend/api/v1/events/${eventId}/features`,{credentials:'include',cache:'no-store'}).then(r=>r.ok?r.json():{}).then((f:Record<string,boolean>)=>setPasses(Boolean(f.GUEST_PASSES))).catch(()=>{})},[eventId]);
 const load=useCallback(async()=>{try{setB(await hostRequest(fetch,route) as Board);setError('')}catch(e){setError(e instanceof Error?e.message:'Could not load guests')}},[route]);
 useEffect(()=>{if(!/^[0-9a-f-]{36}$/i.test(eventId))return;void load();const t=setInterval(()=>{if(document.visibilityState==='visible')void load()},15000);return()=>clearInterval(t)},[eventId,load]);
 useEffect(()=>{if(!toast)return;const t=setTimeout(()=>setToast(null),6000);return()=>clearTimeout(t)},[toast]);
 const list=useMemo(()=>searchGuests(b?.guests??[],q).slice(0,60) as Guest[],[b,q]);
 function pick(g:Guest){setPicked(g);setParty(g.arrivedParty??g.expectedParty);setWalkIn(false)}
 async function confirm(name:string,size:number,isWalkIn:boolean){try{const r=await hostRequest(fetch,route,{method:'POST',payload:{guestName:name,partySize:size,walkIn:isWalkIn}}) as {key:string};
  setToast({text:localizeText('{{name}} checked in · {{n}}').replace('{{name}}',name).replace('{{n}}',String(size)),key:r.key});setPicked(null);setWalkIn(false);setQ('');search.current?.focus();await load()}
  catch(e){setError(e instanceof Error?e.message:'Could not check in')}}
 async function undo(key:string){try{await hostRequest(fetch,`${route}/undo`,{method:'POST',payload:{key}});setToast(null);await load()}catch(e){setError(e instanceof Error?e.message:'Could not undo')}}
 const pct=percent(b?.arrivedPeople??0,b?.expectedPeople??0);

 return <main className="focusPage gbPage eoCheckin">
  <header className="focusPageHeading"><div><p className="focusEyebrow">{localizeText('At the entrance')}</p><h1>{localizeText('Guest check-in')}</h1></div></header>
  {error&&<p className="focusError" role="alert">{localizeText(error)}</p>}
  {b&&<section className="eoHeadcount" aria-live="polite"><div><strong>{b.arrivedPeople}</strong><span>{localizeText('people here')}</span></div>
   <div className="eoBar" aria-hidden="true"><span className="eoBar--paid" style={{width:`${pct}%`}}/></div>
   <p>{localizeText('{{a}} of about {{e}} expected · {{g}} families · {{w}} walk-ins').replace('{{a}}',String(b.arrivedPeople)).replace('{{e}}',String(b.expectedPeople)).replace('{{g}}',String(b.arrivedGuests)).replace('{{w}}',String(b.walkIns))}</p></section>}
  {passes&&<PassScanner eventId={eventId} onDone={()=>void load()}/>}
  <div className="eoSearch"><input ref={search} type="search" autoFocus value={q} onChange={e=>{setQ(e.target.value);setPicked(null)}} placeholder={localizeText('Type a guest’s name')} aria-label={localizeText('Search guests')}/></div>
  {picked&&<section className="gbEntry eoPick"><h2>{picked.name}</h2>{picked.arrived&&<p className="gbQueued">{localizeText('Already checked in at {{t}}. Saving updates the number of people.').replace('{{t}}',picked.arrivedAt?time(picked.arrivedAt):'')}</p>}
   <div className="eoParty"><span>{localizeText('How many people?')}</span><button type="button" aria-label={localizeText('Fewer')} onClick={()=>setParty(p=>Math.max(1,p-1))}>−</button><strong aria-live="polite">{party}</strong><button type="button" aria-label={localizeText('More')} onClick={()=>setParty(p=>Math.min(30,p+1))}>+</button></div>
   <div className="gbEntryActions"><button type="button" className="focusPrimary" onClick={()=>void confirm(picked.name,party,false)}>{localizeText('Check in')}</button><button type="button" className="gbPlain" onClick={()=>setPicked(null)}>{localizeText('Cancel')}</button></div></section>}
  {!picked&&b&&<ul className="gbRows eoGuests">{list.map(g=><li key={g.key}><button type="button" onClick={()=>pick(g)}>
   <span className="gbWho"><strong>{g.name}</strong><small>{g.arrived?localizeText('Arrived {{t}} · {{n}}').replace('{{t}}',g.arrivedAt?time(g.arrivedAt):'').replace('{{n}}',String(g.arrivedParty)):localizeText('Expected {{n}}').replace('{{n}}',String(g.expectedParty))}</small></span>
   <span className={g.arrived?'eoArrived':'eoCheckBtn'}>{g.arrived?'✓':localizeText('Check in')}</span></button></li>)}
   {q.trim()&&!walkIn&&<li><button type="button" className="eoWalkIn" onClick={()=>{setWalkIn(true);setParty(1)}}>{localizeText('Not on the list? Add “{{name}}” as a walk-in').replace('{{name}}',q.trim())}</button></li>}</ul>}
  {walkIn&&q.trim()&&<section className="gbEntry eoPick"><h2>{q.trim()} <small>{localizeText('walk-in')}</small></h2>
   <div className="eoParty"><span>{localizeText('How many people?')}</span><button type="button" onClick={()=>setParty(p=>Math.max(1,p-1))}>−</button><strong>{party}</strong><button type="button" onClick={()=>setParty(p=>Math.min(30,p+1))}>+</button></div>
   <div className="gbEntryActions"><button type="button" className="focusPrimary" onClick={()=>void confirm(q.trim(),party,true)}>{localizeText('Add walk-in')}</button><button type="button" className="gbPlain" onClick={()=>setWalkIn(false)}>{localizeText('Cancel')}</button></div></section>}
  {b&&b.guests.length===0&&!q&&<p className="gbEmpty">{localizeText('No expected guests yet. RSVPs, schedule answers and your WhatsApp guest list appear here. You can still add walk-ins by typing a name.')}</p>}
  {toast&&<div className="gbToast" role="status">{toast.text}<button type="button" onClick={()=>void undo(toast.key)}>{localizeText('Undo')}</button></div>}
 </main>;
}
