'use client';
import DashboardInsights from '../../components/DashboardInsights';
/* localization:instrumented */
import {localize,localizeText} from '../../lib/localization.mjs';
import {useLocalizationRender} from '../../components/LocalizationProvider';

import {useExperience} from '../../components/ExperienceProvider';
import FocusedDashboard from '../../components/focused/Dashboard';
import {useCallback,useEffect,useState} from 'react';
import Link from 'next/link';
import {CalendarDays, Gift, LayoutDashboard, QrCode, RefreshCw, Sparkles, ArrowUpRight, Plus, LogOut, Landmark} from 'lucide-react';
import {startScreenTiming} from '../../lib/diagnostics.mjs';
import {hostApi} from '../../lib/host-api.mjs';
import {summarizeEvents,eventActions,safeEventDate} from '../../lib/dashboard.mjs';
import HostToast from '../components/HostToast';
import HostNotificationBell from '../components/HostNotificationBell';
import './dashboard.css';
import '../host-responsive.css';

type EventItem={id:string;title:string;occasionType:string;eventDate:string|null;status:string;publicToken?:string};
type Summary={total:number;draft:number;published:number;completed:number;cancelled:number};
const occasionIcons:Record<string,string>={BIRTHDAY:'🎂',WEDDING:'💍',BABY_SHOWER:'🧸',HOUSEWARMING:'🏡',ANNIVERSARY:'🥂',OTHER:'✨'};
function ClassicDashboard(){useLocalizationRender();
 const [events,setEvents]=useState<EventItem[]>([]);
 const [username,setUsername]=useState('');
 const [loading,setLoading]=useState(true);
 const [error,setError]=useState('');
 const load=useCallback(async()=>{const ready=startScreenTiming('/dashboard');setLoading(true);setError('');try{
  const [me,data]=await Promise.all([hostApi.me(fetch),hostApi.events(fetch)]);
  if(!Array.isArray(data))throw Error('Unexpected event response');
  setUsername(typeof me?.username==='string'?me.username:'');setEvents(data);ready();
 }catch(e){setError(e instanceof Error?e.message:'Unable to load your dashboard');}finally{setLoading(false);}},[]);
 useEffect(()=>{void load();},[load]);
 const summary:Summary=summarizeEvents(events);
 async function signOut(){try{await hostApi.logout(fetch);window.location.assign('/login');}catch(e){setError(e instanceof Error?e.message:'Could not sign out');}}
 return <main className="dashRoot"><HostToast/><div className="dashLayout">

 <div className="dashContent"><header className="dashHeader"><div><span className="dashEyebrow">{localize("ui.7377f70debd388e1",{defaultValue:"HOST DASHBOARD"})}</span><h1>{username?localize("ui.7c2759d53dbbbe0b",{defaultValue:"Welcome back, {{value0}}",value0:(username)}):localize("ui.e01881dbf3f71f8d",{defaultValue:"Your celebration space"})}</h1><p>{localize("ui.cf5a1524a1aea05b",{defaultValue:"Everything you need to create memorable moments."})}</p></div><HostNotificationBell/> <Link className="dashCreate" href="/events"><Plus size={18}/> {localize("ui.41d2990076d521dd",{defaultValue:" Create celebration"})}</Link></header>
 {error&&<div role="alert" className="dashError">{localizeText(error)} <Link href="/login">{localize("ui.bfd402b2f6f38125",{defaultValue:"Sign in"})}</Link> {localize("ui.e1a3e78cb5c88586",{defaultValue:" or "})}<button type="button" onClick={()=>void load()}>{localize("ui.942087cc2d41e013",{defaultValue:"Retry"})}</button></div>}
 <DashboardInsights/><section className="dashMetrics" aria-label={localize("ui.3db27f7d9c2933ba",{defaultValue:"Event overview"})}>{([{name:'Total events',value:summary.total},{name:'Published',value:summary.published},{name:'In draft',value:summary.draft},{name:'Completed',value:summary.completed}] as const).map(item=><div className="dashMetric" key={item.name}><span>{item.name}</span><strong>{loading?'—':item.value}</strong></div>)}</section>
 <section className="dashSection"><div className="dashSectionHead"><div><span className="dashEyebrow">{localize("ui.8575189f2523e4dd",{defaultValue:"YOUR MOMENTS"})}</span><h2>{localize("ui.63ec34ec60f992a0",{defaultValue:"My celebrations"})}</h2></div><button type="button" onClick={()=>void load()} disabled={loading}><RefreshCw size={16}/> {localize("ui.7ce64141b163cd0d",{defaultValue:" Refresh"})}</button></div>
 {loading?<div role="status" className="dashEmpty">{localize("ui.361b2cc13538cf9e",{defaultValue:"Loading your celebrations…"})}</div>:events.length===0?<div className="dashEmpty"><Sparkles size={28}/><h3>{localize("ui.7c552fd5985d64a4",{defaultValue:"Start with a special moment"})}</h3><p>{localize("ui.e004375ae5fe3925",{defaultValue:"Create an event, add gifts, and design an invitation your guests will love."})}</p><Link className="dashCreate" href="/events">{localize("ui.f996f33a3c8d8aa7",{defaultValue:"Create your first event"})}</Link></div>:<div className="dashEventGrid">{events.map(event=><article className="dashEventCard" key={event.id}><div className="dashEventTop"><span className="dashOccasion">{occasionIcons[event.occasionType]||'✨'}</span><span className={`dashPill dashPill--${event.status?.toLowerCase()||'unknown'}`}>{event.status||localize("ui.b764cdc0eab71374",{defaultValue:"Unknown"})}</span></div><div className="dashEventBody"><span className="dashOccasionText">{localizeText((event.occasionType||'OTHER').replaceAll('_',' '))}</span><h3>{event.title}</h3><p><CalendarDays size={15}/>{safeEventDate(event.eventDate)}</p></div><div className="dashEventLinks">{eventActions(event).filter(action=>typeof action.href==='string' && action.href.length>0).map(action=><Link key={action.href} href={action.href!}>{action.label}<ArrowUpRight size={14}/></Link>)}</div></article>)}</div>}
 </section><section className="dashQuick"><div><QrCode size={23}/><h2>{localize("ui.67ef1bb627d36c91",{defaultValue:"Invitation studio"})}</h2><p>{localize("ui.52a0e58f9fb1aab3",{defaultValue:"Customize your QR invitation with backgrounds, text, and decorations."})}</p><Link href="/studio">{localize("ui.d11dbc6bf1e0383a",{defaultValue:"Open designer "})}<ArrowUpRight size={15}/></Link></div><div><Gift size={23}/><h2>{localize("ui.fc4f92cc5dbe8236",{defaultValue:"Gift wishlist"})}</h2><p>{localize("ui.0ca9d7ff2080f498",{defaultValue:"Choose an event to manage gifts and their visibility for guests."})}</p><Link href="/events">{localize("ui.4d7798f4794c902c",{defaultValue:"Choose event "})}<ArrowUpRight size={15}/></Link></div></section>
 </div></div></main>;
}

export default function Dashboard(){useLocalizationRender();const {mode}=useExperience();return mode==='FOCUSED'?<FocusedDashboard/>:<ClassicDashboard/>;}
