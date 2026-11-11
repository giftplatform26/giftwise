'use client';
import {useEffect,useState} from 'react';
import Link from 'next/link';
import {localizeText} from '../lib/localization.mjs';
import {useLocalizationRender} from './LocalizationProvider';
import {ShowcaseStrip} from './Showcase';

/** Home page: "Try it free" button and the showcase strip, each only when its platform switch is on. */
export function TryFreeLink(){useLocalizationRender();
 const [on,setOn]=useState(false);
 useEffect(()=>{fetch('/backend/api/public/acquisition-modules').then(r=>r.ok?r.json():{}).then((v:{TRY_WITHOUT_ACCOUNT?:boolean})=>setOn(Boolean(v.TRY_WITHOUT_ACCOUNT))).catch(()=>{})},[]);
 return on?<Link className="focusSecondary" href="/try" data-track-action="TRY_START">{localizeText('Try it free — no sign-up')}</Link>:null;
}
export function LandingShowcase(){return <ShowcaseStrip limit={3}/>}
