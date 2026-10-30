'use client';
import {useEffect,useState} from 'react';
import {hostRequest} from '../lib/host-api.mjs';
import './payment-clarity.css';
type Health={id:string;method:string;label:string|null;status:string;message:string;guestVisible:boolean};
export default function ReceivingAccountHealth({eventId,pending}:{eventId:string;pending:boolean}){
 const [rows,setRows]=useState<Health[]>([]),[loading,setLoading]=useState(true),[error,setError]=useState(''),[retry,setRetry]=useState(0);
 useEffect(()=>{if(pending||!eventId)return;let active=true;setLoading(true);setError('');hostRequest(fetch,`/v1/events/${eventId}/receiving-health`).then((data:Health[])=>{if(active)setRows(data)}).catch(()=>{if(active)setError('Could not check account availability. Please try again.')}).finally(()=>{if(active)setLoading(false)});return()=>{active=false}},[eventId,pending,retry]);
 if(!eventId)return null;const unavailable=rows.filter(r=>!r.guestVisible);
 return <section className="account-health" aria-label="Guest account availability"><div className="account-health-heading"><div><h2>What guests can use</h2><p>Check whether linked accounts can appear on this invitation.</p></div><button type="button" disabled={loading||pending} onClick={()=>setRetry(v=>v+1)}>Refresh check</button></div>{loading?<p role="status">Checking linked accounts…</p>:error?<p role="alert">{error}</p>:rows.length===0?<p>Link an account to this event, then enable it and select Preferred.</p>:<><p className="account-health-summary">{rows.filter(r=>r.guestVisible).length} ready · {unavailable.length} need attention</p><details open={unavailable.length>0}><summary>{unavailable.length?'See what needs attention':'View account availability'}</summary><ul>{rows.map(row=><li key={`${row.method}-${row.id}`}><div><strong>{row.label||row.method+' account'} <span className={`payment-badge ${row.guestVisible?'success':'attention'}`}>{row.guestVisible?'Ready for guests':row.status.replaceAll('_',' ').toLowerCase()}</span></strong><p>{row.message}</p></div></li>)}</ul></details></>}</section>;
}
