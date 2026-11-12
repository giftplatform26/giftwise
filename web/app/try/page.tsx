'use client';
import {useEffect,useState} from 'react';
import Link from 'next/link';
import {localizeText} from '../../lib/localization.mjs';
import {useLocalizationRender} from '../../components/LocalizationProvider';
import {OCCASIONS} from '../../lib/host-api.mjs';
import {occasionArt,eventDate} from '../../lib/focused.mjs';
import {applyOccasionStarter} from '../../lib/growth.mjs';
import {tryDraft,readTryDraft,TRY_DRAFT_KEY} from '../../lib/acquisition.mjs';
import '../../components/acquisition.css';

type Form={title:string;description:string;occasionType:string;eventDate:string;hostName:string;city:string;theme:string};
const empty:Form={title:'',description:'',occasionType:'',eventDate:'',hostName:'',city:'',theme:'MODERN_MINIMAL'};
const STEPS=['What are we celebrating?','Who is hosting?','When and where?'];
const label=(o:string)=>o.charAt(0)+o.slice(1).toLowerCase().replace('_',' ');

/** Design an invitation with no account. Saving asks for a free sign-up and carries the draft into the creator. */
export default function TryWithoutAccount(){useLocalizationRender();
 const [on,setOn]=useState<boolean|null>(null),[form,setForm]=useState<Form>(empty),[step,setStep]=useState(0);
 useEffect(()=>{fetch('/backend/api/public/acquisition-modules',{cache:'no-store'}).then(r=>r.ok?r.json():{}).then((v:{TRY_WITHOUT_ACCOUNT?:boolean})=>setOn(Boolean(v.TRY_WITHOUT_ACCOUNT))).catch(()=>setOn(false));
  try{const d=readTryDraft(localStorage);if(d)setForm(f=>({...f,...d}))}catch{}},[]);
 useEffect(()=>{if(!form.occasionType)return;try{localStorage.setItem(TRY_DRAFT_KEY,JSON.stringify(tryDraft(form)))}catch{}},[form]);
 // Count a visitor once per browser session when they start designing.
 useEffect(()=>{if(!form.occasionType)return;try{if(sessionStorage.getItem('gift.try.counted'))return;sessionStorage.setItem('gift.try.counted','1')}catch{}void fetch('/backend/api/public/acquisition-events',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({kind:'TRY_STARTED'})}).catch(()=>{})},[form.occasionType]);
 const set=(k:keyof Form,v:string)=>setForm(f=>({...f,[k]:v}));
 if(on===false)return <main className="tyPage"><section className="tyCard"><h1>{localizeText('Create your free account to start')}</h1><p>{localizeText('Designing without an account is not available right now.')}</p><Link className="focusPrimary" href="/register">{localizeText('Create your celebration')}</Link></section></main>;
 if(on===null)return <main className="tyPage"><p role="status">{localizeText('Loading…')}</p></main>;
 const canNext=step===0?Boolean(form.occasionType&&form.title.trim()):step===1?Boolean(form.hostName.trim()):true;
 return <main className="tyPage">
  <header className="tyHead"><p className="focusEyebrow">{localizeText('No sign-up needed to try')}</p><h1>{localizeText('Design your invitation')}</h1><p>{localizeText('See it come alive as you type. Create a free account only when you want to share it.')}</p></header>
  <div className="tyGrid">
   <section className="gbEntry tyForm" aria-labelledby="ty-step"><div className="tySteps" aria-hidden="true">{STEPS.map((s,i)=><span key={s} className={i<=step?'on':''}/>)}</div>
    <h2 id="ty-step">{localizeText(STEPS[step])}</h2>
    {step===0&&<><div className="tyOccasions" role="radiogroup" aria-label={localizeText('Occasion')}>{OCCASIONS.map((o:string)=><label key={o}><input type="radio" name="occ" checked={form.occasionType===o} onChange={()=>setForm(f=>applyOccasionStarter(f,o,true) as Form)}/><span>{localizeText(label(o))}</span></label>)}</div>
     <label>{localizeText('Title')}<input value={form.title} maxLength={200} onChange={e=>set('title',e.target.value)} placeholder={localizeText('e.g. Asha turns 5')}/></label>
     <label>{localizeText('A note for your guests')}<textarea rows={3} maxLength={2000} value={form.description} onChange={e=>set('description',e.target.value)}/></label></>}
    {step===1&&<label>{localizeText('Your name, as guests know you')}<input autoFocus value={form.hostName} maxLength={200} onChange={e=>set('hostName',e.target.value)} placeholder={localizeText('e.g. Meera & Arjun')}/></label>}
    {step===2&&<><label>{localizeText('Date')}<input type="date" value={form.eventDate} onChange={e=>set('eventDate',e.target.value)}/></label>
     <label>{localizeText('City')}<input value={form.city} maxLength={120} onChange={e=>set('city',e.target.value)} placeholder={localizeText('e.g. Kochi')}/></label></>}
    <div className="gbEntryActions">{step>0&&<button type="button" className="gbPlain" onClick={()=>setStep(s=>s-1)}>{localizeText('Back')}</button>}
     {step<2?<button type="button" className="focusPrimary" disabled={!canNext} onClick={()=>setStep(s=>s+1)}>{localizeText('Next')}</button>:
      <Link className="focusPrimary" href={`/register?next=${encodeURIComponent('/try/claim')}`} data-track-action="TRY_SIGNUP">{localizeText('Save & share — create free account')}</Link>}</div>
    <p className="gbFine">{localizeText('Your draft stays on this device for 7 days. Already have an account?')} <Link href={`/login?next=${encodeURIComponent('/try/claim')}`}>{localizeText('Sign in')}</Link></p></section>
   <section className="tyPreview" aria-label={localizeText('Preview')}><div className="tyPhone"><div className="tyArt" style={{backgroundImage:form.occasionType?`url(/invitation-backgrounds/${occasionArt(form.occasionType)}.svg)`:undefined}}>
    <p className="focusEyebrow">{form.occasionType?localizeText(label(form.occasionType)):localizeText('Your celebration')}</p></div>
    <div className="tyBody"><h3>{form.title||localizeText('Your title appears here')}</h3>{form.hostName&&<p className="tyHost">{localizeText('Hosted by {{name}}').replace('{{name}}',form.hostName)}</p>}
     <p className="tyDate">{eventDate(form.eventDate)}{form.city?` · ${form.city}`:''}</p>{form.description&&<p className="tyNote">{form.description}</p>}
     <span className="tyBtn">{localizeText('RSVP')}</span><span className="tyBtn tyBtn--ghost">{localizeText('Send a wish')}</span></div></div></section>
  </div>
 </main>;
}
