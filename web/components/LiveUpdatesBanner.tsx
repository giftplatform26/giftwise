'use client';
import {useEffect,useState} from 'react';
import {localizeText} from '../lib/localization.mjs';
import {useLocalizationRender} from './LocalizationProvider';
import './eventops.css';

type View={updates:{id:string;message:string;postedAt:string}[];stream:{url:string;label:string|null}|null};
const safe=(u?:string|null)=>u&&/^https:\/\/[^\s<>"]+$/.test(u)?u:null;

/** Guest invitation: latest host announcement and the livestream button. Quietly refreshes every minute. */
export default function LiveUpdatesBanner({token}:{token:string}){useLocalizationRender();
 const [v,setV]=useState<View|null>(null);
 useEffect(()=>{let active=true;const load=()=>fetch(`/backend/api/public/events/${encodeURIComponent(token)}/live-updates`,{cache:'no-store'}).then(r=>r.ok?r.json():null).then(d=>{if(active&&d)setV(d)}).catch(()=>{});
  void load();const t=setInterval(()=>{if(document.visibilityState==='visible')void load()},60000);return()=>{active=false;clearInterval(t)}},[token]);
 const latest=v?.updates?.[0];const stream=safe(v?.stream?.url);
 if(!latest&&!stream)return null;
 return <aside className="luBanner" aria-live="polite">{latest&&<p><span className="luDot" aria-hidden="true"/><strong>{localizeText('Update from the hosts')}</strong> {latest.message}</p>}
  {stream&&<a className="focusPrimary" href={stream} target="_blank" rel="noopener noreferrer">{v?.stream?.label||localizeText('Watch live')}</a>}</aside>;
}
