'use client';
import {useEffect,useState} from 'react';
type Placement={id:string;title:string;description:string;category:string;url:string};
export default function CommercialPlacements({stream='SPONSORED_GIFT',occasion=''}:{stream?:string;occasion?:string}){
 const [rows,setRows]=useState<Placement[]>([]);
 useEffect(()=>{let active=true;setRows([]);fetch(`/backend/api/public/commerce/placements?stream=${encodeURIComponent(stream)}&occasion=${encodeURIComponent(occasion)}`,{cache:'no-store'}).then(async r=>{if(!r.ok)throw Error();return r.json()}).then(x=>{if(active)setRows(x)}).catch(()=>{});return()=>{active=false}},[stream,occasion]);
 if(!rows.length)return null;
 return <section aria-label={stream==='SPONSORED_GIFT'?'Sponsored gift recommendations':'Partner vendors'} style={{margin:'24px 0',padding:20,border:'1px solid #ddd',borderRadius:14}}><h2>{stream==='SPONSORED_GIFT'?'Sponsored gift recommendations':'Partner vendors'}</h2>{rows.map(r=><article key={r.id}><h3>{r.title}</h3><p>{r.description}</p><a href={r.url} target="_blank" rel="sponsored noopener noreferrer">{stream==='SPONSORED_GIFT'?'Sponsored':'Paid partner listing'} · {r.category} →</a></article>)}</section>;
}
