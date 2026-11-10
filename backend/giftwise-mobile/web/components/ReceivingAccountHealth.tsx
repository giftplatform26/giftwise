'use client';
/* localization:instrumented */
import {localize,localizeText} from '../lib/localization.mjs';
import {useLocalizationRender} from './LocalizationProvider';

import {useEffect,useState} from 'react';
import {hostRequest} from '../lib/host-api.mjs';
import './payment-clarity.css';
type Health={id:string;method:string;label:string|null;status:string;message:string;guestVisible:boolean};
export default function ReceivingAccountHealth({eventId,pending}:{eventId:string;pending:boolean}){useLocalizationRender();
 const [rows,setRows]=useState<Health[]>([]),[loading,setLoading]=useState(true),[error,setError]=useState(''),[retry,setRetry]=useState(0);
 useEffect(()=>{if(pending||!eventId)return;let active=true;setLoading(true);setError('');hostRequest(fetch,`/v1/events/${eventId}/receiving-health`).then((data:Health[])=>{if(active)setRows(data)}).catch(()=>{if(active)setError('Could not check account availability. Please try again.')}).finally(()=>{if(active)setLoading(false)});return()=>{active=false}},[eventId,pending,retry]);
 if(!eventId)return null;const unavailable=rows.filter(r=>!r.guestVisible);
 return <section className="account-health" aria-label={localize("ui.ff2f2ff2a50a1946",{defaultValue:"Guest account availability"})}><div className="account-health-heading"><div><h2>{localize("ui.dbc49519e99e2611",{defaultValue:"What guests can use"})}</h2><p>{localize("ui.05c10e58d863ed81",{defaultValue:"Check whether linked accounts can appear on this invitation."})}</p></div><button type="button" disabled={loading||pending} onClick={()=>setRetry(v=>v+1)}>{localize("ui.d0521c5f68365b07",{defaultValue:"Refresh check"})}</button></div>{loading?<p role="status">{localize("ui.7b641b6e0d99ccbb",{defaultValue:"Checking linked accounts…"})}</p>:error?<p role="alert">{localizeText(error)}</p>:rows.length===0?<p>{localize("ui.d8b4bed492f7f637",{defaultValue:"Link an account to this event, then enable it and select Preferred."})}</p>:<><p className="account-health-summary">{rows.filter(r=>r.guestVisible).length} {localize("ui.eb8d328e7bb48295",{defaultValue:" ready · "})}{unavailable.length} {localize("ui.34df33ab2debde98",{defaultValue:" need attention"})}</p><details open={unavailable.length>0}><summary>{unavailable.length?localize("ui.b270b96b583132c7",{defaultValue:"See what needs attention"}):localize("ui.86ad7d12992e9560",{defaultValue:"View account availability"})}</summary><ul>{rows.map(row=><li key={`${row.method}-${row.id}`}><div><strong>{row.label||row.method+localize("ui.b0b1262a5d410f4c",{defaultValue:" account"})} <span className={`payment-badge ${row.guestVisible?'success':'attention'}`}>{localizeText(row.guestVisible?localize("ui.54b029cccc658443",{defaultValue:"Ready for guests"}):row.status.replaceAll('_',' ').toLowerCase())}</span></strong><p>{row.message}</p></div></li>)}</ul></details></>}</section>;
}
