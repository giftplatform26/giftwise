'use client';
/* localization:instrumented */
import {localize,localizeText} from '../lib/localization.mjs';
import {useLocalizationRender} from './LocalizationProvider';

import {useCallback,useEffect,useMemo,useState} from 'react';
import {commercialApi} from './CommercePurchase';
import RoutingCommissionControl from './RoutingCommissionControl';
import AdminPaymentMethods,{PAYMENT_METHODS} from './admin/AdminPaymentMethods';
import {EmptyState} from './admin/AdminUi';
type Scope={id:string;username?:string;email?:string;title?:string;host_name?:string};
type History={revision:number;scope:string;scope_id:string;channel:string;reason:string;before_enabled:boolean|null;after_enabled:boolean|null;actor_id:string|null;created_at:string};
const base='/api/admin/console/payments';
const label=(channel:string)=>localizeText(PAYMENT_METHODS.find(m=>m.channel===channel)?.label||channel);
const state=(v:boolean|null,scope:string)=>v===null?localizeText(scope==='PLATFORM'?'Not set':'Follows platform'):v?localizeText('On'):localizeText('Off');

/**
 * Payment setup below the platform switches: the Route commission, per-event overrides and the change history.
 * Platform switches live in AdminPaymentMethods (Payments section); host switches in the Hosts & access detail.
 */
export default function PaymentControls(){useLocalizationRender();
 const [hosts,setHosts]=useState<Scope[]>([]),[events,setEvents]=useState<Scope[]>([]),[eventId,setEventId]=useState(''),[eventQuery,setEventQuery]=useState('');
 const [history,setHistory]=useState<History[]>([]),[error,setError]=useState(''),[busy,setBusy]=useState(false);
 const historyLoad=useCallback(async()=>{setHistory(await commercialApi<History[]>(base+'/history'))},[]);
 useEffect(()=>{let active=true;Promise.all([commercialApi<Scope[]>('/api/admin/console/hosts'),commercialApi<Scope[]>('/api/admin/console/events')]).then(([h,e])=>{if(active){setHosts(h);setEvents(e)}}).catch(e=>{if(active)setError(e.message)});void historyLoad().catch(e=>setError(e.message));return()=>{active=false}},[historyLoad]);
 const names=useMemo(()=>{const m=new Map<string,string>();hosts.forEach(h=>m.set(h.id,h.username||h.email||h.id));events.forEach(e=>m.set(e.id,e.title||e.id));return m},[hosts,events]);
 const matches=useMemo(()=>{const q=eventQuery.trim().toLowerCase();return events.filter(e=>!q||`${e.title} ${e.host_name}`.toLowerCase().includes(q)).slice(0,100)},[events,eventQuery]);
 const event=events.find(e=>e.id===eventId);
 const where=(h:History)=>h.scope==='PLATFORM'?localizeText('Whole platform'):`${localizeText(h.scope==='HOST'?'Host':'Event')}: ${names.get(h.scope_id)||h.scope_id}`;
 return <><RoutingCommissionControl/>
  <section className="acPanel" aria-labelledby="event-payments-title"><h2 id="event-payments-title">{localizeText('Payment methods for one event')}</h2>
   <p>{localizeText('Use this only for exceptions. An event follows its host, and the host follows the platform. To change a host, open them under Hosts & access.')}</p>
   <div className="admToolbar"><label className="admField">{localizeText('Find an event')}<input type="search" value={eventQuery} placeholder={localizeText('Title or host')} onChange={e=>setEventQuery(e.target.value)}/></label>
    <label className="admField">{localize("ui.4e1f49a9c8ae8a15",{defaultValue:"Event"})}<select value={eventId} onChange={e=>setEventId(e.target.value)}><option value="">{localizeText('Choose an event')}</option>{matches.map(x=><option key={x.id} value={x.id}>{x.title} · {x.host_name||'—'}</option>)}</select></label></div>
   {eventId&&<AdminPaymentMethods key={eventId} scope="EVENT" scopeId={eventId} name={event?.title} onChanged={()=>void historyLoad()}/>}
  </section>
  <section className="acPanel" aria-labelledby="payment-history-title"><h2 id="payment-history-title">{localize("ui.dc30be10fb39fca8",{defaultValue:"Payment control history"})}</h2>
   <button type="button" className="admButton" disabled={busy} onClick={()=>void historyLoad().catch(e=>setError(String(e)))}>{localizeText('Refresh')}</button>
   {error&&<p role="alert" className="acError">{localizeText(error)}</p>}
   {history.length===0?<EmptyState title={localizeText('No payment changes recorded yet.')}/>:<ul className="admQueue">{history.map(h=><li key={h.revision}>
    <strong>{label(h.channel)} · {state(h.before_enabled,h.scope)} → {state(h.after_enabled,h.scope)}</strong>
    <small>{where(h)} · {new Date(h.created_at).toLocaleString()} · {h.actor_id?(names.get(h.actor_id)||localizeText('Administrator')):localize("ui.4ff7e030f35bcbbb",{defaultValue:"System migration"})}</small>
    <small>{localizeText('Reason')}: {h.reason}</small></li>)}</ul>}
   {history.length>=50&&<button type="button" className="admButton" disabled={busy} onClick={async()=>{setBusy(true);try{const older=await commercialApi<History[]>(`${base}/history?beforeRevision=${history[history.length-1].revision}`);setHistory([...history,...older])}catch(e){setError(String(e))}finally{setBusy(false)}}}>{localize("ui.4a0e25b6dcc99396",{defaultValue:"Load earlier changes"})}</button>}
  </section></>;
}
