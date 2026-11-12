'use client';
import {useEffect,useState} from 'react';
import Link from 'next/link';
import {localizeText} from '../lib/localization.mjs';
import {useLocalizationRender} from './LocalizationProvider';
import './attention.css';

type Item={kind:string;eventId:string;eventTitle:string;count:number;label:string;href:string;tone:string};

/** Dashboard: what needs the host's attention today. Renders nothing when notifications are off or all is well. */
export default function AttentionPanel(){useLocalizationRender();
 const [items,setItems]=useState<Item[]|null>(null);
 useEffect(()=>{let active=true;fetch('/backend/api/v1/me/attention',{credentials:'include',cache:'no-store'}).then(r=>r.ok?r.json():null).then(v=>{if(active)setItems(v?.items??null)}).catch(()=>{});return()=>{active=false}},[]);
 if(items===null)return null;
 if(items.length===0)return <section className="atPanel atPanel--calm" aria-label={localizeText('Needs your attention')}><span aria-hidden="true">✓</span>{localizeText('All caught up. Nothing needs you right now.')}</section>;
 return <section className="atPanel" aria-labelledby="at-title"><h2 id="at-title">{localizeText('Needs your attention')}</h2>
  <ul>{items.slice(0,8).map(i=><li key={`${i.kind}-${i.eventId}`} className={`atItem atItem--${i.tone}`}><Link href={i.href}><span className="atCount">{i.count}</span><span className="atText"><strong>{localizeText(i.label.replace(/^\d+ /,'{{n}} ')).replace('{{n}}',String(i.count))}</strong><small>{i.eventTitle}</small></span><span aria-hidden="true">→</span></Link></li>)}</ul>
  {items.length>8&&<p className="atMore">{localizeText('And {{n}} more').replace('{{n}}',String(items.length-8))}</p>}</section>;
}
