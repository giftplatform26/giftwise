'use client';
import {useEffect,useState} from 'react';
import Link from 'next/link';
import {useSearchParams} from 'next/navigation';
import {localizeText} from '../../lib/localization.mjs';
import {useLocalizationRender} from '../../components/LocalizationProvider';
import {hostRequest} from '../../lib/host-api.mjs';
import '../../components/gift-books.css';
import '../../components/functions.css';

type Helping={eventId:string;title:string;eventDate:string|null;hostName:string|null;role:string;capabilities:string[];features:Record<string,boolean>};
const day=(v:string|null)=>v?new Intl.DateTimeFormat(undefined,{dateStyle:'long'}).format(new Date(`${v}T12:00:00`)):'';

export default function HelpingHub(){useLocalizationRender();
 const joined=useSearchParams().get('joined')==='1';
 const [list,setList]=useState<Helping[]|null>(null),[error,setError]=useState('');
 async function load(){try{setList(await hostRequest(fetch,'/v1/me/co-hosting') as Helping[])}catch(e){setError(e instanceof Error?e.message:'Could not load')}}
 useEffect(()=>{void load()},[]);
 async function leave(h:Helping){if(!confirm(localizeText('Stop helping with {{title}}?').replace('{{title}}',h.title)))return;try{await hostRequest(fetch,`/v1/me/co-hosting/${h.eventId}/leave`,{method:'POST'});await load()}catch(e){setError(e instanceof Error?e.message:'Could not leave')}}
 return <main className="focusPage gbPage">
  <header className="focusPageHeading"><div><p className="focusEyebrow">{localizeText('Co-hosting')}</p><h1>{localizeText('Celebrations I help with')}</h1></div></header>
  {joined&&<p className="gbNotice" role="status">{localizeText('You have joined the team. Thank you for helping!')}</p>}
  {error&&<p className="focusError" role="alert">{localizeText(error)}</p>}
  {list&&list.length===0&&<p className="gbEmpty">{localizeText('When a host invites you to help, their celebration appears here.')}</p>}
  <div className="hpGrid">{list?.map(h=>{const can=new Set(h.capabilities);const tools:[boolean,string,string][]=[
   [can.has('REGISTER_ADD')&&h.features.SHAGUN_REGISTER,`/helping/${h.eventId}/register`,'Gift register'],
   [can.has('GUESTS')&&(h.features.RSVP||h.features.GUESTBOOK),`/helping/${h.eventId}/celebration`,'Guests & wishes'],
   [can.has('LIVE_WALL')&&h.features.LIVE_WALL,`/helping/${h.eventId}/live-wall`,'Live venue wall'],
   [can.has('FUNCTIONS')&&h.features.EVENT_FUNCTIONS,`/helping/${h.eventId}/functions`,'Schedule & functions'],
   [can.has('CHECKIN')&&h.features.GUEST_CHECKIN,`/helping/${h.eventId}/checkin`,'Guest check-in'],
   [can.has('PLANNING')&&h.features.PLANNING_CHECKLIST,`/helping/${h.eventId}/checklist`,'Planning checklist'],
   [can.has('PLANNING')&&h.features.RUN_SHEET,`/helping/${h.eventId}/run-sheet`,'Run sheet'],
   [can.has('UPDATES')&&h.features.LIVE_UPDATES,`/helping/${h.eventId}/updates`,'Live updates']];
   const open=tools.filter(t=>t[0]);
   return <article key={h.eventId} className="hpCard"><p className="focusEyebrow">{localizeText(h.role==='CO_HOST'?'Co-host':'Gift-desk helper')}</p><h2>{h.title}</h2>
    <p className="gbFine">{[h.hostName&&localizeText('Hosted by {{name}}').replace('{{name}}',h.hostName),day(h.eventDate)].filter(Boolean).join(' · ')}</p>
    {open.length===0?<p className="gbFine">{localizeText('The host has not turned on any tools you can use yet.')}</p>:<nav className="hpTools">{open.map(([,href,label])=><Link key={href} href={href}>{localizeText(label)} →</Link>)}</nav>}
    <button type="button" className="gbPlain" onClick={()=>void leave(h)}>{localizeText('Stop helping')}</button></article>})}</div>
 </main>;
}
