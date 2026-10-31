'use client';
import {useEffect,useState} from 'react';
import {commercialApi,commercialMoney} from './CommercePurchase';
type Event={id:string;title:string};
type Analytics={dailyContributions:Array<{day:string;count:number;host_net_paise:number}>;affiliateClicks:number};
export default function CommercialAnalytics(){
 const [events,setEvents]=useState<Event[]>([]),[selected,setSelected]=useState(''),[data,setData]=useState<Analytics|null>(null),[error,setError]=useState(''),[busy,setBusy]=useState(false);
 useEffect(()=>{let active=true;commercialApi<Event[]>('/api/v1/events').then(x=>{if(active)setEvents(x)}).catch(e=>{if(active)setError(e.message)});return()=>{active=false}},[]);
 async function load(){if(!selected)return;setBusy(true);setError('');setData(null);try{setData(await commercialApi<Analytics>(`/api/v1/commerce/analytics/${selected}`))}catch(e){setError(e instanceof Error?e.message:'Could not load analytics')}finally{setBusy(false)}}
 return <section className="hostPanel"><h2>Event analytics</h2><label>Event<select value={selected} onChange={e=>{setSelected(e.target.value);setData(null)}}><option value="">Select event</option>{events.map(e=><option key={e.id} value={e.id}>{e.title}</option>)}</select></label><button type="button" disabled={busy||!selected} onClick={()=>void load()}>{busy?'Loading…':'Load analytics'}</button>{error&&<p role="alert">{error}</p>}{data&&<><p>Affiliate product clicks: {data.affiliateClicks}</p><table><caption>Verified captured contributions by date, after platform fees</caption><thead><tr><th>Date</th><th>Contributions</th><th>Host net</th></tr></thead><tbody>{data.dailyContributions.map(row=><tr key={row.day}><td>{row.day}</td><td>{row.count}</td><td>{commercialMoney(Number(row.host_net_paise))}</td></tr>)}</tbody></table></>}</section>;
}
