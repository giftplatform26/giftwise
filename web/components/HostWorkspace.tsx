'use client';
import {createContext,useContext,useEffect,useId,useState,type ReactNode} from 'react';
import {usePathname,useRouter,useSearchParams} from 'next/navigation';
import Link from 'next/link';
import {ShieldCheck,Activity,CalendarDays,CheckSquare,Eye,Gift,Heart,Landmark,LayoutDashboard,LogOut,Mail,PanelLeftClose,PanelLeftOpen,Plus,QrCode,Sparkles,UserRound,Users,type LucideIcon} from 'lucide-react';
import {hostApi} from '../lib/host-api.mjs';
import {eventIdForRoute} from '../lib/focused.mjs';
import './host-sidebar.css';
type EventInfo={title:string;eventDate:string|null;status:string};
const WorkspaceContext=createContext<{eventId:string|null;event:EventInfo|null}>({eventId:null,event:null});
export const useHostWorkspace=()=>useContext(WorkspaceContext);
type Item={href:string;label:string;Icon:LucideIcon;active?:boolean};
export default function HostWorkspace({children}:{children:ReactNode}){
 const path=usePathname(),query=useSearchParams(),router=useRouter(),eventId=eventIdForRoute(path,query.get('eventId'));
 const [name,setName]=useState(''),[collapsed,setCollapsed]=useState(false),[ready,setReady]=useState(false),[busy,setBusy]=useState(false),[error,setError]=useState('');
 const [event,setEvent]=useState<EventInfo|null>(null),[features,setFeatures]=useState<Record<string,boolean>>({}),[tooltip,setTooltip]=useState<{label:string;x:number;y:number}|null>(null);const tooltipId=useId();
 useEffect(()=>{try{setCollapsed(window.matchMedia('(max-width:700px)').matches||localStorage.getItem('gift.host.sidebar.collapsed')==='true')}catch{}setReady(true)},[]);
 useEffect(()=>{let active=true;void hostApi.me(fetch).then(me=>{if(active)setName(me.username||'');return hostApi.profileName(fetch)}).then(profile=>{if(active&&profile.fullName)setName(profile.fullName)}).catch(()=>{});return()=>{active=false}},[]);
 useEffect(()=>{let active=true;setEvent(null);setFeatures({});if(eventId)void Promise.all([hostApi.getEvent(fetch,eventId),fetch(`/backend/api/v1/events/${eventId}/features`,{credentials:'include',cache:'no-store'}).then(r=>r.ok?r.json():{})]).then(([e,f])=>{if(active){setEvent(e);setFeatures(f)}}).catch(()=>{});return()=>{active=false}},[eventId]);
 useEffect(()=>{setTooltip(null);if(window.matchMedia('(max-width:700px)').matches)setCollapsed(true)},[path]);
 useEffect(()=>{function escape(e:KeyboardEvent){if(e.key==='Escape'){setTooltip(null);if(window.matchMedia('(max-width:700px)').matches)setCollapsed(true)}}window.addEventListener('keydown',escape);return()=>window.removeEventListener('keydown',escape)},[]);
 function toggle(){setTooltip(null);setCollapsed(current=>{const next=!current;try{if(!window.matchMedia('(max-width:700px)').matches)localStorage.setItem('gift.host.sidebar.collapsed',String(next))}catch{}return next})}
 function showTooltip(element:HTMLElement,label:string){if(!collapsed)return;const rect=element.getBoundingClientRect();setTooltip({label,x:rect.right+10,y:rect.top+rect.height/2})}
 function navigate(){setTooltip(null);if(window.matchMedia('(max-width:700px)').matches)setCollapsed(true)}
 async function signOut(){if(busy)return;setBusy(true);setError('');try{await hostApi.logout(fetch);router.replace('/login');router.refresh()}catch(e){setError(e instanceof Error?e.message:'Sign out failed');setBusy(false)}}
 const account:Item[]=[{href:'/dashboard',label:'Overview',Icon:LayoutDashboard},{href:'/events',label:'Celebrations',Icon:CalendarDays},{href:'/events/new',label:'Create celebration',Icon:Plus},{href:'/host/payment-settings',label:'Receiving accounts',Icon:Landmark},{href:'/host/account',label:'Account',Icon:UserRound}];
 const base=eventId?`/events/${eventId}`:'';
 const eventLinks:Item[]=eventId?[
  {href:base,label:'Celebration overview',Icon:CheckSquare},
  {href:base+'/receiving-accounts',label:'Receiving accounts',Icon:Landmark},
  {href:`/gifts?eventId=${eventId}`,label:'Gift wishlist',Icon:Gift,active:path==='/gifts'},
  ...((features.RSVP||features.GUESTBOOK)?[{href:base+'/celebration',label:'Guests & responses',Icon:Users}]:[]),
  {href:base+'/contributions',label:'Contribution report',Icon:Activity},
  {href:base+'/transactions',label:'Payment verification',Icon:ShieldCheck},
  {href:base+'/qr',label:'Invitation design & QR',Icon:QrCode},
  {href:base+'/preview',label:'Preview invitation',Icon:Eye},
  {href:base+'/notifications',label:'Notification history',Icon:Mail},
  {href:base+'/thank-you',label:'Thank-you cards',Icon:Heart},
 ]:[{href:'/',label:'Invitation studio',Icon:Sparkles}];
 function navItem({href,label,Icon,active}:Item){return <Link key={href} href={href} className="hostNavItem" aria-label={label} aria-current={(active??(path===href))?'page':undefined} aria-describedby={tooltip?.label===label?tooltipId:undefined} title={collapsed?label:undefined} onClick={navigate} onMouseEnter={e=>showTooltip(e.currentTarget,label)} onMouseLeave={()=>setTooltip(null)} onFocus={e=>showTooltip(e.currentTarget,label)} onBlur={()=>setTooltip(null)}><Icon size={21} aria-hidden="true"/><span className="hostNavLabel">{label}</span></Link>}
 return <WorkspaceContext.Provider value={{eventId,event}}><div className={`hostWorkspace${collapsed?' hostWorkspaceCollapsed':''}${ready?' hostWorkspaceReady':''}`}>
 <header className="hostIdentity"><button className="hostSidebarToggle" type="button" onClick={toggle} aria-expanded={!collapsed} aria-controls="hostSidebarNav" aria-label={collapsed?'Expand sidebar':'Collapse sidebar'} title={collapsed?'Expand sidebar':'Collapse sidebar'}>{collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>}</button><div className="hostIdentityMain"><Link href="/events" className="hostBrand" aria-label="Gift Platform celebrations"><Gift size={24} aria-hidden="true"/><span>Gift Platform</span></Link><p className="hostGreeting">{name?`Welcome back, ${name}!`:'Welcome to your workspace'}</p></div></header>
 <aside className="hostSidebar" aria-label="Host navigation">
 <div className="hostSidebarBody" id="hostSidebarNav"><nav aria-label="Workspace"><p className="hostNavSection">Workspace</p>{account.map(navItem)}<div className="hostNavDivider"/><p className="hostNavSection" title={event?.title||'Invitation'}>{eventId?event?.title||'Your celebration':'Invitation'}</p><nav aria-label={eventId?'Celebration':'Invitation'}>{eventLinks.map(navItem)}</nav></nav></div>
 <div className="hostSidebarFooter">{error&&<p className="hostSidebarError" role="alert">{error}</p>}<button type="button" className="hostNavItem" disabled={busy} onClick={()=>void signOut()} aria-label={busy?'Signing out…':'Sign out'} title="Sign out" onMouseEnter={e=>showTooltip(e.currentTarget,'Sign out')} onMouseLeave={()=>setTooltip(null)} onFocus={e=>showTooltip(e.currentTarget,'Sign out')} onBlur={()=>setTooltip(null)}><LogOut size={21} aria-hidden="true"/><span className="hostNavLabel">{busy?'Signing out…':'Sign out'}</span></button></div></aside>
 {!collapsed&&<button className="hostSidebarBackdrop" aria-label="Close sidebar" onClick={()=>setCollapsed(true)}/>}
 <div className="hostWorkspaceContent" id="host-workspace-content">{children}</div>{tooltip&&<div className="hostNavTooltip" id={tooltipId} role="tooltip" style={{left:tooltip.x,top:tooltip.y}}>{tooltip.label}</div>}
 </div></WorkspaceContext.Provider>;
}
