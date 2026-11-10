'use client';
/* localization:instrumented */
import {localize} from '../lib/localization.mjs';
import {useLocalizationRender} from './LocalizationProvider';

import {useEffect,useState} from 'react';
type Placement={id:string;title:string;description:string;category:string;url:string};
export default function CommercialPlacements({stream='SPONSORED_GIFT',occasion=''}:{stream?:string;occasion?:string}){useLocalizationRender();
 const [rows,setRows]=useState<Placement[]>([]);
 useEffect(()=>{let active=true;setRows([]);fetch(`/backend/api/public/commerce/placements?stream=${encodeURIComponent(stream)}&occasion=${encodeURIComponent(occasion)}`,{cache:'no-store'}).then(async r=>{if(!r.ok)throw Error();return r.json()}).then(x=>{if(active)setRows(x)}).catch(()=>{});return()=>{active=false}},[stream,occasion]);
 if(!rows.length)return null;
 return <section aria-label={stream==='SPONSORED_GIFT'?localize("ui.66a40a0055fa1644",{defaultValue:"Sponsored gift recommendations"}):localize("ui.2b36f5d51d1b318e",{defaultValue:"Partner vendors"})} style={{margin:'24px 0',padding:20,border:'1px solid #ddd',borderRadius:14}}><h2>{stream==='SPONSORED_GIFT'?localize("ui.66a40a0055fa1644",{defaultValue:"Sponsored gift recommendations"}):localize("ui.2b36f5d51d1b318e",{defaultValue:"Partner vendors"})}</h2>{rows.map(r=><article key={r.id}><h3>{r.title}</h3><p>{r.description}</p><a href={r.url} target="_blank" rel="sponsored noopener noreferrer">{stream==='SPONSORED_GIFT'?localize("ui.c01d19506818bbbb",{defaultValue:"Sponsored"}):localize("ui.fd0da266344fd0b3",{defaultValue:"Paid partner listing"})} · {r.category} →</a></article>)}</section>;
}
