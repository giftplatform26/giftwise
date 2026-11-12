'use client';
import {useEffect,useState} from 'react';
import {useParams} from 'next/navigation';
import Link from 'next/link';
import {localizeText,activeLocale} from '../../../lib/localization.mjs';
import {useLocalizationRender} from '../../../components/LocalizationProvider';
import {eventDate} from '../../../lib/focused.mjs';
import {MODES,dayLabel,toLocalInput} from '../../../lib/travel.mjs';
import '../../../components/guest-forms.css';

type Leg={at:string|null;mode:string|null;ref:string|null;point:string|null};
type Travel={eventTitle:string;eventDate:string|null;hostName:string|null;guestName:string;partySize:number;arrival:Leg;departure:Leg;pickupNeeded:boolean;dropNeeded:boolean;guestNote:string|null;submitted:boolean;
 stay:{name:string;address:string|null;phone:string|null;mapUrl:string|null;checkIn:string|null;checkOut:string|null;room:string|null}|null;invitationPath:string};
type LegDraft={at:string;mode:string;ref:string;point:string};
const draftOf=(l:Leg):LegDraft=>({at:toLocalInput(l.at),mode:l.mode??'',ref:l.ref??'',point:l.point??''});
const body=(l:LegDraft)=>({at:l.at||null,mode:l.mode||null,ref:l.ref||null,point:l.point||null});

/** A family's private travel form: they share arrival and departure, ask for a pickup, and see their hotel and room. No account needed. */
export default function TravelForm(){useLocalizationRender();
 const {token}=useParams();const t=String(token??'');const locale=activeLocale();
 const [v,setV]=useState<Travel|null>(null),[gone,setGone]=useState(false),[error,setError]=useState(''),[saved,setSaved]=useState(false),[busy,setBusy]=useState(false);
 const [f,setF]=useState<{party:string;arrival:LegDraft;departure:LegDraft;pickup:boolean;drop:boolean;note:string}|null>(null);
 function take(x:Travel){setV(x);setF({party:String(x.partySize),arrival:draftOf(x.arrival),departure:draftOf(x.departure),pickup:x.pickupNeeded,drop:x.dropNeeded,note:x.guestNote??''})}
 useEffect(()=>{if(!/^[0-9a-f-]{36}$/i.test(t)){setGone(true);return}fetch(`/backend/api/public/travel/${t}`,{cache:'no-store'}).then(r=>r.ok?r.json():Promise.reject()).then(take).catch(()=>setGone(true))},[t]);
 async function save(e:React.FormEvent){e.preventDefault();if(!f)return;setBusy(true);setError('');setSaved(false);
  try{const r=await fetch(`/backend/api/public/travel/${t}`,{method:'PUT',headers:{'Content-Type':'application/json'},body:JSON.stringify({partySize:Number(f.party)||1,arrival:body(f.arrival),departure:body(f.departure),pickupNeeded:f.pickup,dropNeeded:f.drop,guestNote:f.note||null})});
   const j=await r.json().catch(()=>({}));if(!r.ok)throw new Error(j.error||'Could not save. Please try again.');take(j);setSaved(true)}catch(err){setError(err instanceof Error?err.message:'Could not save')}finally{setBusy(false)}}
 if(gone)return <main className="gfPage"><section className="gfCard"><h1>{localizeText('This link is not valid')}</h1><p>{localizeText('Ask the host to send you a new travel link.')}</p></section></main>;
 if(!v||!f)return <main className="gfPage"><p role="status">{localizeText('Opening…')}</p></main>;
 const setLeg=(w:'arrival'|'departure',k:keyof LegDraft,x:string)=>setF({...f,[w]:{...f[w],[k]:x}});
 const leg=(w:'arrival'|'departure')=><fieldset className="gfLegend"><legend>{localizeText(w==='arrival'?'Your arrival':'Your return')}</legend>
  <div className="gfRow"><label>{localizeText('Date & time')}<input type="datetime-local" value={f[w].at} onChange={e=>setLeg(w,'at',e.target.value)}/></label>
   <label>{localizeText('Travelling by')}<select value={f[w].mode} onChange={e=>setLeg(w,'mode',e.target.value)}><option value="">—</option>{Object.entries(MODES).map(([k,x])=><option key={k} value={k}>{localizeText(x)}</option>)}</select></label></div>
  <div className="gfRow"><label>{localizeText('Flight / train number')}<input value={f[w].ref} maxLength={40} onChange={e=>setLeg(w,'ref',e.target.value)} placeholder="6E 512"/></label>
   <label>{localizeText(w==='arrival'?'Arriving at':'Leaving from')}<input value={f[w].point} maxLength={120} onChange={e=>setLeg(w,'point',e.target.value)} placeholder={localizeText('Airport, station or city')}/></label></div>
  <label className="gfCheck"><input type="checkbox" checked={w==='arrival'?f.pickup:f.drop} onChange={e=>setF({...f,[w==='arrival'?'pickup':'drop']:e.target.checked})}/>{localizeText(w==='arrival'?'Please arrange a pickup':'Please arrange a drop')}</label></fieldset>;
 return <main className="gfPage"><form className="gfCard" onSubmit={save}>
  <p className="gfKicker">{localizeText('Travel plans')}</p><h1>{v.eventTitle}</h1><p className="gfMeta">{eventDate(v.eventDate)}{v.hostName?` · ${v.hostName}`:''}</p>
  <p>{localizeText('Hello {{n}}! Tell us when you are arriving and leaving so we can arrange your stay and transport.').replace('{{n}}',v.guestName)}</p>
  {v.stay&&<div className="gfStay"><strong>{localizeText('Your stay')}: {v.stay.name}{v.stay.room?` · ${localizeText('Room')} ${v.stay.room}`:''}</strong>
   {v.stay.address&&<p>{v.stay.address}</p>}{v.stay.checkIn&&<p>{localizeText('Check-in')}: {dayLabel(v.stay.checkIn,locale)}{v.stay.checkOut?` · ${localizeText('Check-out')}: ${dayLabel(v.stay.checkOut,locale)}`:''}</p>}
   <p>{v.stay.phone&&<a href={`tel:${v.stay.phone}`}>{v.stay.phone}</a>}{v.stay.phone&&v.stay.mapUrl?' · ':''}{v.stay.mapUrl&&<a href={v.stay.mapUrl} target="_blank" rel="noopener noreferrer">{localizeText('Open map')}</a>}</p></div>}
  <label>{localizeText('How many people are travelling')}<input inputMode="numeric" value={f.party} onChange={e=>setF({...f,party:e.target.value.replace(/\D/g,'').slice(0,2)})}/></label>
  {leg('arrival')}{leg('departure')}
  <label>{localizeText('Anything we should know?')}<textarea rows={3} maxLength={300} value={f.note} onChange={e=>setF({...f,note:e.target.value})} placeholder={localizeText('e.g. Travelling with a wheelchair, late check-in')}/></label>
  {error&&<p className="gfErr" role="alert">{localizeText(error)}</p>}
  {saved&&<p className="gfOk" role="status">{localizeText('Thank you! Your travel plans are saved. You can come back to this link to change them.')}</p>}
  <button className="gfButton" disabled={busy}>{localizeText(v.submitted?'Update my plans':'Send my plans')}</button>
  <Link className="gfFine" href={v.invitationPath}>{localizeText('Open the invitation')}</Link></form></main>;
}
