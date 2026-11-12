'use client';
import {useEffect,useState} from 'react';
import {useParams} from 'next/navigation';
import Link from 'next/link';
import {localizeText} from '../../../../lib/localization.mjs';
import {useLocalizationRender} from '../../../../components/LocalizationProvider';
import {eventDate} from '../../../../lib/focused.mjs';
import '../../../../components/guest-forms.css';

type Form={eventTitle:string;eventDate:string|null;hostName:string|null;open:boolean;question:string|null};
const ASPECTS:[string,string][]=[['food','Food'],['venue','Venue'],['arrangements','Arrangements'],['hospitality','Hospitality']];

function Stars({label,value,onChange}:{label:string;value:number;onChange:(n:number)=>void}){
 return <div className="gfAspect"><span>{localizeText(label)}</span><div className="gfStars" role="radiogroup" aria-label={localizeText(label)}>{[1,2,3,4,5].map(n=>
  <button key={n} type="button" role="radio" aria-checked={value===n} aria-pressed={n<=value} aria-label={localizeText('{{n}} stars').replace('{{n}}',String(n))} onClick={()=>onChange(value===n?0:n)}>★</button>)}</div></div>;
}

/** Guests rate the celebration after it is over (only while the hosts have feedback open). No account needed. */
export default function GuestFeedback(){useLocalizationRender();
 const {token}=useParams();const t=String(token??'');const url=`/backend/api/public/celebrations/${t}/feedback`;
 const [f,setF]=useState<Form|null>(null),[gone,setGone]=useState(false),[done,setDone]=useState(false),[error,setError]=useState(''),[busy,setBusy]=useState(false);
 const [v,setV]=useState<Record<string,number>>({overall:0,food:0,venue:0,arrangements:0,hospitality:0}),[comment,setComment]=useState(''),[answer,setAnswer]=useState(''),[name,setName]=useState('');
 useEffect(()=>{if(!/^[0-9a-f-]{36}$/i.test(t)){setGone(true);return}fetch(url,{cache:'no-store'}).then(r=>r.ok?r.json():Promise.reject()).then(setF).catch(()=>setGone(true))},[t,url]);
 async function send(e:React.FormEvent){e.preventDefault();if(!v.overall){setError(localizeText('Choose an overall rating'));return}setBusy(true);setError('');
  try{const r=await fetch(url,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({...v,comment:comment||null,answer:answer||null,guestName:name||null})});const j=await r.json().catch(()=>({}));if(!r.ok)throw new Error(j.error||'Could not send. Please try again.');setDone(true)}
  catch(err){setError(err instanceof Error?err.message:'Could not send')}finally{setBusy(false)}}
 if(gone)return <main className="gfPage"><section className="gfCard"><h1>{localizeText('Feedback is not available')}</h1></section></main>;
 if(!f)return <main className="gfPage"><p role="status">{localizeText('Opening…')}</p></main>;
 const back=<Link className="gfFine" href={`/e/${t}`}>{localizeText('Back to the invitation')}</Link>;
 if(!f.open)return <main className="gfPage"><section className="gfCard"><p className="gfKicker">{localizeText('Feedback')}</p><h1>{f.eventTitle}</h1><p>{localizeText('The hosts are not collecting feedback right now.')}</p>{back}</section></main>;
 if(done)return <main className="gfPage"><section className="gfCard"><p className="gfKicker">{localizeText('Feedback')}</p><h1>{localizeText('Thank you!')}</h1><p>{localizeText('Your feedback has been sent to the hosts.')}</p>{back}</section></main>;
 return <main className="gfPage"><form className="gfCard" onSubmit={send}><p className="gfKicker">{localizeText('How was it?')}</p><h1>{f.eventTitle}</h1><p className="gfMeta">{eventDate(f.eventDate)}{f.hostName?` · ${f.hostName}`:''}</p>
  <Stars label="Overall" value={v.overall} onChange={n=>setV({...v,overall:n})}/>
  <p className="gfFine">{localizeText('Optional — rate what you noticed:')}</p>
  {ASPECTS.map(([k,l])=><Stars key={k} label={l} value={v[k]} onChange={n=>setV({...v,[k]:n})}/>)}
  {f.question&&<label>{f.question}<input value={answer} maxLength={500} onChange={e=>setAnswer(e.target.value)}/></label>}
  <label>{localizeText('Anything you would like to tell the hosts?')}<textarea rows={4} maxLength={1000} value={comment} onChange={e=>setComment(e.target.value)}/></label>
  <label>{localizeText('Your name (optional)')}<input value={name} maxLength={80} onChange={e=>setName(e.target.value)}/></label>
  {error&&<p className="gfErr" role="alert">{localizeText(error)}</p>}
  <button className="gfButton" disabled={busy}>{localizeText('Send feedback')}</button>{back}</form></main>;
}
