'use client';
import {useEffect,useState} from 'react';
import Link from 'next/link';
import {hostApi} from '../../lib/host-api.mjs';
import {summarizeEvents} from '../../lib/dashboard.mjs';
import {eventDate} from '../../lib/focused.mjs';
type Event={id:string;title:string;status:string;eventDate:string|null};
export default function Dashboard(){
 const [events,setEvents]=useState<Event[]>([]),[loading,setLoading]=useState(true),[error,setError]=useState('');
 useEffect(()=>{let active=true;hostApi.events(fetch).then(data=>{if(active)setEvents(data)}).catch(e=>{if(active)setError(e.message)}).finally(()=>{if(active)setLoading(false)});return()=>{active=false}},[]);
 const summary=summarizeEvents(events);
 const upcoming=events.filter(e=>e.status==='PUBLISHED').sort((a,b)=>(a.eventDate||'9999').localeCompare(b.eventDate||'9999')).slice(0,3);
 return <main className="focusPage"><header className="focusPageHeading"><div><p className="focusEyebrow">Host overview</p><h1>Your celebration workspace</h1><p>A quick look at your events and what’s next.</p></div><Link className="focusPrimary" href="/events/new">Create celebration ＋</Link></header>{error&&<p className="focusError" role="alert">{error}</p>}{loading?<p role="status">Loading your overview…</p>:<><section className="focusSummary" aria-label="Celebration summary">{[['All celebrations',summary.total],['Published',summary.published],['Private drafts',summary.draft],['Completed',summary.completed]].map(([label,count])=><div key={label}><span>{label}</span><strong>{count}</strong></div>)}</section><section className="focusPanel"><h2>Ready to share</h2>{upcoming.length?upcoming.map(e=><p key={e.id}><Link href={`/events/${e.id}`}>{e.title}</Link> · {eventDate(e.eventDate)}</p>):<p>Your published celebrations will appear here.</p>}<Link className="focusTextLink" href="/events">View all celebrations →</Link></section><section className="focusPanel"><h2>Make it yours</h2><p>{summary.draft?`${summary.draft} private draft${summary.draft===1?' is':'s are'} waiting for your finishing touches.`:'Start a new celebration and build a wishlist for your guests.'}</p><div className="giftButtons"><Link className="focusPrimary" href="/events">Manage celebrations</Link><Link className="focusTextLink" href="/host/payment-settings">Receiving accounts →</Link></div></section></>}
 </main>;
}
