'use client';
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
function ClassicDashboard(){
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

 <div className="dashContent"><header className="dashHeader"><div><span className="dashEyebrow">HOST DASHBOARD</span><h1>{username?`Welcome back, ${username}`:'Your celebration space'}</h1><p>Everything you need to create memorable moments.</p></div><HostNotificationBell/> <Link className="dashCreate" href="/events"><Plus size={18}/> Create celebration</Link></header>
 {error&&<div role="alert" className="dashError">{error} <Link href="/login">Sign in</Link> or <button type="button" onClick={()=>void load()}>Retry</button></div>}
 <section className="dashMetrics" aria-label="Event overview">{([{name:'Total events',value:summary.total},{name:'Published',value:summary.published},{name:'In draft',value:summary.draft},{name:'Completed',value:summary.completed}] as const).map(item=><div className="dashMetric" key={item.name}><span>{item.name}</span><strong>{loading?'—':item.value}</strong></div>)}</section>
 <section className="dashSection"><div className="dashSectionHead"><div><span className="dashEyebrow">YOUR MOMENTS</span><h2>My celebrations</h2></div><button type="button" onClick={()=>void load()} disabled={loading}><RefreshCw size={16}/> Refresh</button></div>
 {loading?<div role="status" className="dashEmpty">Loading your celebrations…</div>:events.length===0?<div className="dashEmpty"><Sparkles size={28}/><h3>Start with a special moment</h3><p>Create an event, add gifts, and design an invitation your guests will love.</p><Link className="dashCreate" href="/events">Create your first event</Link></div>:<div className="dashEventGrid">{events.map(event=><article className="dashEventCard" key={event.id}><div className="dashEventTop"><span className="dashOccasion">{occasionIcons[event.occasionType]||'✨'}</span><span className={`dashPill dashPill--${event.status?.toLowerCase()||'unknown'}`}>{event.status||'Unknown'}</span></div><div className="dashEventBody"><span className="dashOccasionText">{(event.occasionType||'OTHER').replaceAll('_',' ')}</span><h3>{event.title}</h3><p><CalendarDays size={15}/>{safeEventDate(event.eventDate)}</p></div><div className="dashEventLinks">{eventActions(event).filter(action=>typeof action.href==='string' && action.href.length>0).map(action=><Link key={action.href} href={action.href!}>{action.label}<ArrowUpRight size={14}/></Link>)}</div></article>)}</div>}
 </section><section className="dashQuick"><div><QrCode size={23}/><h2>Invitation studio</h2><p>Customize your QR invitation with backgrounds, text, and decorations.</p><Link href="/">Open designer <ArrowUpRight size={15}/></Link></div><div><Gift size={23}/><h2>Gift wishlist</h2><p>Choose an event to manage gifts and their visibility for guests.</p><Link href="/events">Choose event <ArrowUpRight size={15}/></Link></div></section>
 </div></div></main>;
}

export default function Dashboard(){const {mode}=useExperience();return mode==='FOCUSED'?<FocusedDashboard/>:<ClassicDashboard/>;}
