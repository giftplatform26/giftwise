'use client';
import AdminAiDesignDetails from '../../components/AdminAiDesignDetails';
import {useCallback,useEffect,useRef,useState} from 'react';
import Link from 'next/link';
import {hostApi} from '../../lib/host-api.mjs';
import ExperienceControl from '../../components/focused/ExperienceControl';
import {useExperience} from '../../components/ExperienceProvider';
import PlatformVerificationControls from '../../components/PlatformVerificationControls';
import MobileVerification from '../../components/MobileVerification';
import AccountEmailVerification from '../../components/AccountEmailVerification';
import GroupMemberEmailPolicy from '../../components/GroupMemberEmailPolicy';
import AccountPolicies from './AccountPolicies';
import AdminAudit from './AdminAudit';
import MonetizationControls from '../../components/MonetizationControls';
import CommercialAdminControls from '../../components/CommercialAdminControls';
import PaymentControls from '../../components/PaymentControls';
import './admin-console.css';
type Summary={hosts:number;events:number;publishedEvents:number;groups:number};
type GroupTransferReport={reportedUnverified:number;reportedUnverifiedPaise:number;verifiedRecords:number;rejectedRecords:number};
type GuestbookReport={pending:number;approved:number;hidden:number};
type NotificationReport={manualClaimOutbox:number};
type Module={module_code:string;enabled:boolean};
type Host={id:string;username:string;email:string};
type HostModule={module_code:string;platform_enabled:boolean;host_enabled:boolean;effective_enabled:boolean};
type EventRow={id:string;title:string;status:string;occasion_type:string;host_name:string;album_enabled:boolean;album_available:boolean;ai_enabled:boolean;ai_available:boolean};
type Audit={action:string;module_code:string|null;created_at:string};
const modules:Record<string,string>={GROUP_GIFTING:'Group gifting',GUEST_GIFTS:'Guest-added gifts',RSVP:'RSVP',GUEST_ATTENDANCE:'Guest attendance',WHATSAPP_SHARING:'WhatsApp sharing',GUESTBOOK:'Digital guestbook',MEMORY_ALBUM:'Event memory albums',AI_INVITATION:'Design with AI',SOCIAL_SHARING:'Social invitation sharing'};
function adminCsrfToken():string {
 const entry=document.cookie.split(';').map(v=>v.trim()).find(v=>v.startsWith('XSRF-TOKEN='));
 return entry?decodeURIComponent(entry.substring('XSRF-TOKEN='.length)):'';
}
async function api<T>(path:string,options?:RequestInit):Promise<T>{const modifying=!!options?.method && options.method.toUpperCase()!=='GET';
 if(modifying){const init=await fetch('/backend/api/auth/csrf',{credentials:'include',cache:'no-store'});if(!init.ok)throw Error(`Secure session initialization failed (${init.status})`);}
 const token=modifying?adminCsrfToken():'';
 if(modifying&&!token)throw Error('CSRF token missing; reload and sign in again');
 const response=await fetch('/backend/api/admin/console'+path,{credentials:'include',cache:'no-store',...options,headers:{...options?.headers,...(modifying?{'X-XSRF-TOKEN':token}:{})}});if(!response.ok)throw Error(response.status===403?'Platform administrator access required':`Request failed (${response.status})`);return response.json();}
export default function AdminConsole(){
 const {mode}=useExperience();const focused=mode==='FOCUSED';
 const [summary,setSummary]=useState<Summary|null>(null),[list,setList]=useState<Module[]>([]),[hosts,setHosts]=useState<Host[]>([]),[events,setEvents]=useState<EventRow[]>([]),[history,setHistory]=useState<Audit[]>([]);
 const [groupReport,setGroupReport]=useState<GroupTransferReport|null>(null),[guestbookReport,setGuestbookReport]=useState<GuestbookReport|null>(null),[notificationReport,setNotificationReport]=useState<NotificationReport|null>(null);
  const [selected,setSelected]=useState(''),[hostModules,setHostModules]=useState<HostModule[]>([]),[search,setSearch]=useState(''),[error,setError]=useState(''),[busy,setBusy]=useState(false),[tab,setTab]=useState('overview');
 const [auditRevision,setAuditRevision]=useState(0);
 const selectedRef=useRef(selected);selectedRef.current=selected;
 const tabRef=useRef(tab);tabRef.current=tab;
 const refresh=useCallback(async()=>{try{
   const currentTab=tabRef.current;const selected=selectedRef.current;
   if(currentTab==='overview'){
     const [s,a]=await Promise.all([api<Summary>('/summary'),api<Audit[]>('/audit')]);setSummary(s);setHistory(a);
   }else if(currentTab==='hosts'){
     const [m,h,selectedHostModules]=await Promise.all([api<Module[]>('/modules'),api<Host[]>('/hosts'),selected?api<HostModule[]>('/hosts/'+selected+'/modules'):Promise.resolve([] as HostModule[])]);setList(m);setHosts(h);if(selectedRef.current===selected)setHostModules(selectedHostModules);
   }else if(currentTab==='events'){setEvents(await api<EventRow[]>('/events'));}
   else if(currentTab==='activity'){setAuditRevision(value=>value+1);}
   else if(currentTab==='policies'){const [h,e]=await Promise.all([api<Host[]>('/hosts'),api<EventRow[]>('/events')]);setHosts(h);setEvents(e);}
   else if(currentTab==='reports'){
     const reports=await Promise.allSettled([api<GroupTransferReport>('/reports/group-transfers'),api<GuestbookReport>('/reports/guestbook'),api<NotificationReport>('/reports/notifications')]);
     if(reports[0].status==='fulfilled')setGroupReport(reports[0].value);
     if(reports[1].status==='fulfilled')setGuestbookReport(reports[1].value);
     if(reports[2].status==='fulfilled')setNotificationReport(reports[2].value);
     if(reports.some(r=>r.status==='rejected'))throw Error('Some reporting data could not be loaded. Retry to refresh reports.');
   }
   setError('');
 }catch(e){setError(e instanceof Error?e.message:'Unable to load console');}},[]);
 useEffect(()=>{void refresh();},[tab,refresh]);
 useEffect(()=>{let active=true;setHostModules([]);if(selected)void api<HostModule[]>('/hosts/'+selected+'/modules').then(x=>{if(active)setHostModules(x)}).catch(e=>{if(active)setError(String(e))});return()=>{active=false};},[selected]);
 async function toggle(path:string,enabled:boolean){if(busy)return;setBusy(true);try{await api(path,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({enabled})});await refresh();}catch(e){setError(String(e));}finally{setBusy(false);}}
 async function signout(){try{await hostApi.logout(fetch);window.location.assign('/login');}catch(e){setError(String(e));}}
 const filtered=hosts.filter(h=>(h.username+' '+h.email).toLowerCase().includes(search.toLowerCase()));
 return <main className="ac"><aside className="acSide"><Link className="acBrand" href="/admin">✦ Gift Platform <small>Administration</small></Link><nav aria-label="Administration">{[['overview','Dashboard'],['hosts','Hosts & privileges'],['events','Events'],['reports','Reports'],['monetization','Monetization'],['payments','Payment controls'],['activity','Activity & audit'],['policies','Authentication & policies'],['appearance','Application view'],['account','My account']].map(([id,label])=><button key={id} aria-current={tab===id?'page':undefined} onClick={()=>setTab(id)}>{label}</button>)}<Link href="/admin/invitation-assets">Invitation gallery ↗</Link></nav><p>Secure platform management</p></aside><div className="acMain"><header className="acHeader"><div><span>PLATFORM ADMINISTRATION</span><h1>{({overview:'Dashboard',hosts:'Host privileges',events:'Events',reports:'Reports',monetization:'Monetization',payments:'Payment controls',activity:'Activity & audit',policies:'Authentication & policies',appearance:'Application view',account:'My account'} as Record<string,string>)[tab]}</h1></div><div><button onClick={()=>void refresh()}>Refresh</button><button onClick={()=>void signout()}>Sign out</button></div></header>{error&&<div role="alert" className="acError">{error} <Link href="/login">Login</Link></div>}
 {tab==='overview'&&<><section className="acMetrics">{[['Total hosts',summary?.hosts],['Total events',summary?.events],['Published events',summary?.publishedEvents],['Group gifts',summary?.groups]].map(([label,value])=><article key={label}><span>{label}</span><strong>{value??'—'}</strong></article>)}</section><section className="acPanel"><h2>Quick access</h2><div className="acLinks"><button onClick={()=>setTab('hosts')}>Manage privileges →</button><button onClick={()=>setTab('reports')}>View reports →</button><Link href="/admin/invitation-assets">Invitation backgrounds →</Link></div></section><section className="acPanel"><h2>Recent administration activity</h2>{history.slice(0,8).map((x,i)=><p key={i}>{x.action} · {x.module_code||'Platform'} · {new Date(x.created_at).toLocaleString()}</p>)}</section></>}
 {tab==='hosts'&&<><section className="acPanel"><h2>Platform-wide modules</h2><p>Disable a module platform-wide to remove its effective eligibility for all hosts. Guest-added gifts controls invitation guests adding, selecting and publishing their own individual or group gifts. It also requires Group gifting access. Published gifts remain in host reports when disabled.</p><div className="acRows">{list.map(m=><label key={m.module_code}><span>{modules[m.module_code]||m.module_code}</span><input type="checkbox" disabled={busy} checked={m.enabled} onChange={e=>void toggle('/modules/'+m.module_code,e.target.checked)}/></label>)}</div></section><section className="acPanel"><h2>Host-specific privileges</h2><input aria-label="Search hosts" placeholder="Search by host name or email" value={search} onChange={e=>setSearch(e.target.value)}/><select aria-label="Select host" value={selected} onChange={e=>setSelected(e.target.value)}><option value="">Select host</option>{filtered.map(h=><option value={h.id} key={h.id}>{h.username} · {h.email}</option>)}</select>{selected&&<GroupMemberEmailPolicy key={selected} hostId={selected}/>} {selected&&<AdminAiDesignDetails key={`ai-${selected}`} hostId={selected}/>} {selected&&<div className="acRows">{hostModules.map(m=><label key={m.module_code}><span>{modules[m.module_code]} <small>{m.platform_enabled?'Platform available':'Platform disabled'}</small>{focused&&<small className="focusOptionStatus">{m.effective_enabled?'Available to this host':!m.platform_enabled?'Unavailable · disabled platform-wide':'Unavailable · host access not granted'}</small>}</span><input type="checkbox" disabled={busy||!m.platform_enabled} checked={m.host_enabled} onChange={e=>void toggle('/hosts/'+selected+'/modules/'+m.module_code,e.target.checked)}/></label>)}</div>}</section></>}
 {tab==='events'&&<section className="acPanel"><h2>Recent events</h2><p>Memory albums and Design with AI each require their platform module and host privilege under Hosts &amp; privileges, plus the event switch below. Use AI design details to review access and event information. Existing memories are preserved when access is disabled.</p><div className="acTable"><table><thead><tr><th>Event</th><th>Host</th><th>Occasion</th><th>Status</th><th>Memory album</th><th>Design with AI</th></tr></thead><tbody>{events.map(e=><tr key={e.id}><td>{e.title}</td><td>{e.host_name||'—'}</td><td>{e.occasion_type}</td><td>{e.status}</td><td><label style={{display:'flex',gap:8,alignItems:'center'}}><input type="checkbox" aria-label={`Enable memory album for ${e.title}`} disabled={busy} checked={e.album_enabled} onChange={change=>void toggle('/events/'+e.id+'/album-settings',change.target.checked)}/>Enable album</label><small>{e.album_available?'Available to the host':e.album_enabled?'Also enable the platform module and host grant':'Disabled for this event'}</small></td><td><label style={{display:'flex',gap:8,alignItems:'center'}}><input type="checkbox" aria-label={`Enable Design with AI for ${e.title}`} disabled={busy} checked={e.ai_enabled} onChange={change=>void toggle('/events/'+e.id+'/ai-design-settings',change.target.checked)}/>Enable AI design</label><small>{e.ai_available?'Available to the host':e.ai_enabled?'Also enable the platform module and host grant':'Disabled for this event'}</small><AdminAiDesignDetails eventId={e.id}/></td></tr>)}</tbody></table></div></section>}
 {tab==='reports'&&<><section className="acPanel"><h2>Group transfer reports</h2><p>These are reported transactions, not independently confirmed financial balances. A VERIFIED record count is shown separately and is not proof of ledger settlement.</p><div className="acReportGrid"><div>Awaiting independent verification<strong>{groupReport?.reportedUnverified??'—'}</strong></div><div>Guest-reported amount (not received balance)<strong>{groupReport?`₹${(groupReport.reportedUnverifiedPaise/100).toLocaleString('en-IN',{minimumFractionDigits:2,maximumFractionDigits:2})}`:'—'}</strong></div><div>Records marked verified<strong>{groupReport?.verifiedRecords??'—'}</strong></div><div>Rejected reports<strong>{groupReport?.rejectedRecords??'—'}</strong></div></div></section><section className="acPanel"><h2>Guestbook moderation overview</h2><p>Event hosts moderate messages through their own celebration pages; administrators can review platform-wide status here.</p><div className="acReportGrid"><div>Pending approval<strong>{guestbookReport?.pending??'—'}</strong></div><div>Approved<strong>{guestbookReport?.approved??'—'}</strong></div><div>Hidden<strong>{guestbookReport?.hidden??'—'}</strong></div></div></section><section className="acPanel"><h2>Notification activity</h2><p>Outbox records do not mean an email or SMS was delivered.</p><div className="acReportGrid"><div>Manual-claim notification records<strong>{notificationReport?.manualClaimOutbox??'—'}</strong></div></div></section></>}
 {tab==='payments'&&<PaymentControls/>}
 {tab==='monetization'&&<><MonetizationControls/><CommercialAdminControls/></>}
 {tab==='appearance'&&<ExperienceControl/>}
 {tab==='account'&&<section className="acPanel"><h2>My account</h2><AccountEmailVerification/><MobileVerification/></section>}
 {tab==='policies'&&<><PlatformVerificationControls/><AccountPolicies hosts={hosts} events={events}/></>}
 {tab==='activity'&&<AdminAudit revision={auditRevision} modules={modules}/>}
 <footer>Gift Platform · Administrator workspace · Payment confirmation remains independently verified</footer></div></main>;
}
