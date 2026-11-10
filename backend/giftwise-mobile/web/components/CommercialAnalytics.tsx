'use client';
/* localization:instrumented */
import {localize,localizeText} from '../lib/localization.mjs';
import {useLocalizationRender} from './LocalizationProvider';

import {useEffect,useState} from 'react';
import {commercialApi,commercialMoney} from './CommercePurchase';
type Event={id:string;title:string};
type Analytics={dailyContributions:Array<{day:string;count:number;host_net_paise:number}>;affiliateClicks:number};
export default function CommercialAnalytics(){useLocalizationRender();
 const [events,setEvents]=useState<Event[]>([]),[selected,setSelected]=useState(''),[data,setData]=useState<Analytics|null>(null),[error,setError]=useState(''),[busy,setBusy]=useState(false);
 useEffect(()=>{let active=true;commercialApi<Event[]>('/api/v1/events').then(x=>{if(active)setEvents(x)}).catch(e=>{if(active)setError(e.message)});return()=>{active=false}},[]);
 async function load(){if(!selected)return;setBusy(true);setError('');setData(null);try{setData(await commercialApi<Analytics>(`/api/v1/commerce/analytics/${selected}`))}catch(e){setError(e instanceof Error?e.message:'Could not load analytics')}finally{setBusy(false)}}
 return <section className="hostPanel"><h2>{localize("ui.fd96f0715dcc1486",{defaultValue:"Event analytics"})}</h2><label>{localize("ui.4e1f49a9c8ae8a15",{defaultValue:"Event"})}<select value={selected} onChange={e=>{setSelected(e.target.value);setData(null)}}><option value="">{localize("ui.ff25e66f618f2984",{defaultValue:"Select event"})}</option>{events.map(e=><option key={e.id} value={e.id}>{e.title}</option>)}</select></label><button type="button" disabled={busy||!selected} onClick={()=>void load()}>{busy?localize("ui.ba3bbbe10d8bef66",{defaultValue:"Loading…"}):localize("ui.eb8e0a4b8ee292e0",{defaultValue:"Load analytics"})}</button>{error&&<p role="alert">{localizeText(error)}</p>}{data&&<><p>{localize("ui.75da55cd5946cf07",{defaultValue:"Affiliate product clicks: "})}{data.affiliateClicks}</p><table><caption>{localize("ui.a2d0f17a72f5752c",{defaultValue:"Verified captured contributions by date, after platform fees"})}</caption><thead><tr><th>{localize("ui.99c40ab405926cb5",{defaultValue:"Date"})}</th><th>{localize("ui.7219a0256285ca25",{defaultValue:"Contributions"})}</th><th>{localize("ui.ba44921f49a1193e",{defaultValue:"Host net"})}</th></tr></thead><tbody>{data.dailyContributions.map(row=><tr key={row.day}><td>{row.day}</td><td>{row.count}</td><td>{commercialMoney(Number(row.host_net_paise))}</td></tr>)}</tbody></table></>}</section>;
}
