'use client';
import DashboardInsights from '../DashboardInsights';
/* localization:instrumented */
import {localize,localizeText} from '../../lib/localization.mjs';
import {useLocalizationRender} from '../LocalizationProvider';

import {useEffect,useState} from 'react';
import Link from 'next/link';
import {hostApi} from '../../lib/host-api.mjs';
import {summarizeEvents} from '../../lib/dashboard.mjs';
import {eventDate} from '../../lib/focused.mjs';
import HostNotificationBell from '../../app/components/HostNotificationBell';
type Event={id:string;title:string;status:string;eventDate:string|null};
export default function Dashboard(){useLocalizationRender();
 const [events,setEvents]=useState<Event[]>([]),[loading,setLoading]=useState(true),[error,setError]=useState('');
 useEffect(()=>{let active=true;hostApi.events(fetch).then(data=>{if(active)setEvents(data)}).catch(e=>{if(active)setError(e.message)}).finally(()=>{if(active)setLoading(false)});return()=>{active=false}},[]);
 const summary=summarizeEvents(events);
 const upcoming=events.filter(e=>e.status==='PUBLISHED').sort((a,b)=>(a.eventDate||'9999').localeCompare(b.eventDate||'9999')).slice(0,3);
 return <main className="focusPage"><header className="focusPageHeading"><div><p className="focusEyebrow">{localize("ui.76d28ef015b69d01",{defaultValue:"Host overview"})}</p><h1>{localize("ui.aa8da0b93f6eb7fa",{defaultValue:"Your celebration workspace"})}</h1><p>{localize("ui.aee96310b85225e5",{defaultValue:"A quick look at your events and what’s next."})}</p></div><HostNotificationBell/><Link className="focusPrimary" href="/events/new">{localize("ui.0969d0c619cbffe4",{defaultValue:"Create celebration ＋"})}</Link></header>{error&&<p className="focusError" role="alert">{localizeText(error)}</p>}{loading?<p role="status">{localize("ui.be6e64345eb4f883",{defaultValue:"Loading your overview…"})}</p>:<><DashboardInsights/><section className="focusSummary" aria-label={localize("ui.cfd135bf4643c812",{defaultValue:"Celebration summary"})}>{[['All celebrations',summary.total],['Published',summary.published],['Private drafts',summary.draft],['Completed',summary.completed]].map(([label,count])=><div key={label}><span>{localizeText(label)}</span><strong>{count}</strong></div>)}</section><section className="focusPanel"><h2>{localize("ui.2effb4b01333d9f0",{defaultValue:"Ready to share"})}</h2>{upcoming.length?upcoming.map(e=><p key={e.id}><Link href={`/events/${e.id}`}>{e.title}</Link> · {eventDate(e.eventDate)}</p>):<p>{localize("ui.565472a7a19706fe",{defaultValue:"Your published celebrations will appear here."})}</p>}<Link className="focusTextLink" href="/events">{localize("ui.a74905124e028905",{defaultValue:"View all celebrations →"})}</Link></section><section className="focusPanel"><h2>{localize("ui.34ca8a4540f5238f",{defaultValue:"Make it yours"})}</h2><p>{summary.draft?localize("ui.3ed4ddbb427fb940",{defaultValue:"{{value0}} private draft{{value1}} waiting for your finishing touches.",value0:(summary.draft),value1:(summary.draft===1?' is':'s are')}):localize("ui.e725163b68566f20",{defaultValue:"Start a new celebration and build a wishlist for your guests."})}</p><div className="giftButtons"><Link className="focusPrimary" href="/events">{localize("ui.1f923623781627be",{defaultValue:"Manage celebrations"})}</Link><Link className="focusTextLink" href="/host/payment-settings">{localize("ui.1c4673a97426764e",{defaultValue:"Receiving accounts →"})}</Link></div></section></>}
 </main>;
}
