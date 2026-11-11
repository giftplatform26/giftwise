'use client';
import {useEffect,useState} from 'react';
import {localizeText} from '../lib/localization.mjs';
import {useLocalizationRender} from './LocalizationProvider';
import {functionWhen,groupByDay,rsvpPayload} from '../lib/functions.mjs';
import './functions.css';

type Fn={id:string;name:string;functionDate:string|null;startTime:string|null;endTime:string|null;venueName:string|null;venueAddress:string|null;mapUrl:string|null;dressCode:string|null;notes:string|null;rsvpEnabled:boolean};
type Answer={attendance?:'YES'|'MAYBE'|'NO';partySize?:number};
const safeMap=(u:string|null)=>u&&/^https:\/\/[^\s<>"]+$/.test(u)?u:null;
const dayTitle=(d:string|null)=>d?new Intl.DateTimeFormat(undefined,{weekday:'long',day:'numeric',month:'long'}).format(new Date(`${d}T12:00:00`)):localizeText('Date to be announced');

/** Guest view: the function-by-function schedule with one RSVP form covering every function. */
export default function EventSchedule({token}:{token:string}){useLocalizationRender();
 const [fns,setFns]=useState<Fn[]>([]),[answers,setAnswers]=useState<Record<string,Answer>>({}),[name,setName]=useState(''),[email,setEmail]=useState('');
 const [busy,setBusy]=useState(false),[result,setResult]=useState(''),[ok,setOk]=useState(false),[showForm,setShowForm]=useState(false);
 useEffect(()=>{let active=true;fetch(`/backend/api/public/celebrations/${encodeURIComponent(token)}/functions`,{cache:'no-store'}).then(r=>r.ok?r.json():[]).then(v=>{if(active&&Array.isArray(v))setFns(v)}).catch(()=>{});return()=>{active=false}},[token]);
 if(fns.length===0)return null;
 const rsvpable=fns.filter(f=>f.rsvpEnabled);
 const answer=(id:string,a:Answer)=>{setOk(false);setResult('');setAnswers(x=>({...x,[id]:{...x[id],...a}}))};
 async function submit(e:React.FormEvent){e.preventDefault();setResult('');let body;try{body=rsvpPayload({guestName:name,guestEmail:email,answers})}catch(err){setOk(false);setResult(err instanceof Error?err.message:'Check your answers');return}
  setBusy(true);try{const r=await fetch(`/backend/api/public/celebrations/${encodeURIComponent(token)}/functions/rsvp`,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(body)});
   const data=await r.json().catch(()=>({}));if(!r.ok)throw Error(typeof data?.error==='string'?data.error:r.status===429?'Too many tries. Please try again later.':'Could not save your answers');
   setOk(true);setResult(data.message||'Thank you! Your answers are saved.')}catch(err){setOk(false);setResult(err instanceof Error?err.message:'Could not save your answers')}finally{setBusy(false)}}

 return <section className="esWrap" id="schedule" aria-labelledby="es-title">
  <header className="esHead"><p className="focusEyebrow">{localizeText('The celebrations')}</p><h2 id="es-title">{localizeText('Schedule')}</h2></header>
  {groupByDay(fns).map(({date,items}:{date:string|null;items:Fn[]})=><div key={date??'tba'} className="esDay"><h3>{dayTitle(date)}</h3><ol>{items.map(f=>{const map=safeMap(f.mapUrl);return <li key={f.id} className="esItem">
   <span className="esTime">{functionWhen({startTime:f.startTime,endTime:f.endTime})||'—'}</span>
   <div><strong>{f.name}</strong>{(f.venueName||f.venueAddress)&&<p>{[f.venueName,f.venueAddress].filter(Boolean).join(', ')}{map&&<> · <a href={map} target="_blank" rel="noopener noreferrer">{localizeText('Directions')}</a></>}</p>}
    {f.dressCode&&<p className="esDress">{localizeText('Dress code')}: {f.dressCode}</p>}{f.notes&&<p className="esNote">{f.notes}</p>}</div></li>})}</ol></div>)}
  {rsvpable.length>0&&(!showForm?<button type="button" className="focusPrimary esCta" onClick={()=>setShowForm(true)}>{localizeText('Tell the hosts which functions you will attend')}</button>:
  <form className="esForm" onSubmit={submit} noValidate><h3>{localizeText('Will you join us?')}</h3>
   {rsvpable.map(f=>{const a=answers[f.id]??{};return <fieldset key={f.id} className="esAnswer"><legend>{f.name}<small>{functionWhen(f)}</small></legend>
    <div className="esChoices">{(['YES','MAYBE','NO'] as const).map(v=><label key={v}><input type="radio" name={`a-${f.id}`} checked={a.attendance===v} onChange={()=>answer(f.id,{attendance:v})}/><span>{localizeText(v==='YES'?'Yes':v==='MAYBE'?'Maybe':'No')}</span></label>)}</div>
    {a.attendance&&a.attendance!=='NO'&&<label className="esParty">{localizeText('How many of you?')}<select value={a.partySize??1} onChange={e=>answer(f.id,{partySize:Number(e.target.value)})}>{Array.from({length:10},(_,i)=><option key={i+1} value={i+1}>{i+1}</option>)}</select></label>}
   </fieldset>})}
   <div className="esWho"><label>{localizeText('Your name')}<input value={name} maxLength={120} autoComplete="name" onChange={e=>setName(e.target.value)}/></label>
    <label>{localizeText('Email')}<input type="email" value={email} maxLength={254} autoComplete="email" onChange={e=>setEmail(e.target.value)}/><small>{localizeText('Use the same email to change your answers later.')}</small></label></div>
   {result&&<p role={ok?'status':'alert'} className={ok?'esOk':'focusError'}>{localizeText(result)}</p>}
   <button className="focusPrimary" disabled={busy}>{busy?localizeText('Sending…'):localizeText('Send my answers')}</button>
  </form>)}
 </section>;
}
