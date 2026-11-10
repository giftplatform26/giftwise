'use client';
/* localization:instrumented */
import {localize} from '../lib/localization.mjs';
import {useLocalizationRender} from './LocalizationProvider';

import {useEffect,useState} from 'react';
type Details={giftingEnabled:boolean;welcomeMessage:string;venue:string|null;arrivalNotes:string;date:string|null;startTime:string|null;timezone:string;durationMinutes:number};
export default function CelebrationDetails({token}:{token:string}){useLocalizationRender();
 const [details,setDetails]=useState<Details|null>(null);
 useEffect(()=>{let active=true;const controller=new AbortController();fetch(`/backend/api/public/events/${encodeURIComponent(token)}/celebration-details`,{cache:'no-store',signal:controller.signal}).then(r=>r.ok?r.json():null).then(value=>{if(active)setDetails(value)}).catch(()=>{});return()=>{active=false;controller.abort()}},[token]);
 if(!details||(!details.welcomeMessage&&!details.venue&&!details.startTime&&details.giftingEnabled))return null;
 return <section className="focusPanel" style={{maxWidth:820,margin:'24px auto',padding:24}} aria-label={localize("ui.b4fa3ff1ed868a0d",{defaultValue:"Your celebration details"})}>{!details.giftingEnabled&&<h2>{localize("ui.38d6f3b51dadeec1",{defaultValue:"Your presence is our favourite gift."})}</h2>}{details.welcomeMessage&&<p style={{whiteSpace:'pre-wrap'}}>{details.welcomeMessage}</p>}{details.startTime&&<p>{localize("ui.f2daa026118cab58",{defaultValue:"Starts at "})}{details.startTime.slice(0,5)} · {details.timezone}</p>}{details.venue&&<><h3>{localize("ui.df4aafd332fec7c2",{defaultValue:"We’ll see you here"})}</h3><address style={{fontStyle:'normal'}}>{details.venue}</address><a target="_blank" rel="noopener noreferrer" href={`https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(details.venue)}`}>{localize("ui.d43fd6a9ef44719d",{defaultValue:"Open directions ↗"})}</a>{details.arrivalNotes&&<p style={{whiteSpace:'pre-wrap'}}>{details.arrivalNotes}</p>}</>}</section>;
}
