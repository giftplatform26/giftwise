'use client';
import {useEffect,useState} from 'react';
import Link from 'next/link';
import {localizeText} from '../lib/localization.mjs';
import {useLocalizationRender} from './LocalizationProvider';
import './attention.css';

type Step={code:string;label:string;hint:string;done:boolean;href:string};

/** Celebration overview: a setup guide listing only the tools switched on for this event, ticked as they're used. */
export default function EventSetupGuide({eventId}:{eventId:string}){useLocalizationRender();
 const [steps,setSteps]=useState<Step[]|null>(null),[hidden,setHidden]=useState(false);
 useEffect(()=>{if(!/^[0-9a-f-]{36}$/i.test(eventId))return;try{setHidden(localStorage.getItem(`gift.setup.hidden.${eventId}`)==='1')}catch{}
  let active=true;fetch(`/backend/api/v1/events/${eventId}/setup`,{credentials:'include',cache:'no-store'}).then(r=>r.ok?r.json():null).then(v=>{if(active&&v)setSteps(v.steps)}).catch(()=>{});return()=>{active=false}},[eventId]);
 if(!steps||steps.length<2)return null;
 const done=steps.filter(s=>s.done).length;const pct=Math.round(done/steps.length*100);
 if(hidden||done===steps.length)return null;
 const next=steps.find(s=>!s.done);
 return <section className="sgPanel" aria-labelledby="sg-title"><div className="sgHead"><div><h2 id="sg-title">{localizeText('Set up your celebration')}</h2><p>{localizeText('{{d}} of {{t}} done').replace('{{d}}',String(done)).replace('{{t}}',String(steps.length))}{next?` · ${localizeText('Next: {{s}}').replace('{{s}}',localizeText(next.label))}`:''}</p></div>
  <div className="sgRing" style={{'--pct':`${pct}%`} as React.CSSProperties} aria-hidden="true"><span>{pct}%</span></div></div>
  <ol className="sgSteps">{steps.map(s=><li key={s.code} className={s.done?'is-done':''}><Link href={s.href}><span className="sgTick" aria-hidden="true">{s.done?'✓':''}</span><span><strong>{localizeText(s.label)}</strong><small>{localizeText(s.hint)}</small></span></Link></li>)}</ol>
  <button type="button" className="sgHide" onClick={()=>{setHidden(true);try{localStorage.setItem(`gift.setup.hidden.${eventId}`,'1')}catch{}}}>{localizeText('Hide this guide')}</button></section>;
}
