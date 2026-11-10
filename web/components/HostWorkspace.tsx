'use client';
import {BrandLogo} from './BrandingProvider';
/* localization:instrumented */
import {localize,localizeText} from '../lib/localization.mjs';
import {HostLanguageSwitcher,useLocalizationRender} from './LocalizationProvider';

import {createContext,useContext,useEffect,useId,useState,type ReactNode} from 'react';
import {usePathname,useRouter,useSearchParams} from 'next/navigation';
import Link from 'next/link';
import {ShieldCheck,Activity,CalendarDays,CheckSquare,Eye,Gift,Heart,Images,Landmark,LayoutDashboard,LogOut,Mail,PanelLeftClose,PanelLeftOpen,Plus,QrCode,Sparkles,UserRound,Users,type LucideIcon} from 'lucide-react';
import {hostApi} from '../lib/host-api.mjs';
import {eventIdForRoute} from '../lib/focused.mjs';
import './host-sidebar.css';
type EventInfo={title:string;eventDate:string|null;status:string};
const WorkspaceContext=createContext<{eventId:string|null;event:EventInfo|null}>({eventId:null,event:null});
export const useHostWorkspace=()=>useContext(WorkspaceContext);
type Item={href:string;label:string;Icon:LucideIcon;active?:boolean};
export default function HostWorkspace({children}:{children:ReactNode}){useLocalizationRender();
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
 const account:Item[]=[{href:'/dashboard',label:'Overview',Icon:LayoutDashboard},{href:'/events',label:'Celebrations',Icon:CalendarDays},{href:'/events/new',label:'Create celebration',Icon:Plus},{href:'/host/payment-settings',label:'Receiving accounts',Icon:Landmark},{href:'/host/monetization',label:'Plans & partnerships',Icon:Sparkles},{href:'/host/account',label:'Account',Icon:UserRound}];
 const base=eventId?`/events/${eventId}`:'';
 const eventLinks:Item[]=eventId?[
  {href:base,label:'Celebration overview',Icon:CheckSquare},
  ...((features.CELEBRATION_PLANNING||features.EVENT_REUSE)?[{href:base+'/planning',label:'Celebration settings',Icon:Sparkles}]:[]),
  {href:base+'/receiving-accounts',label:'Receiving accounts',Icon:Landmark},
  {href:`/gifts?eventId=${eventId}`,label:'Gift wishlist',Icon:Gift,active:path==='/gifts'},
  ...((features.RSVP||features.GUESTBOOK)?[{href:base+'/celebration',label:'Guests & responses',Icon:Users}]:[]),
  ...(features.MEMORY_ALBUM?[{href:base+'/album',label:'Memory album',Icon:Images}]:[]),
  {href:base+'/contributions',label:'Contribution report',Icon:Activity},
  {href:base+'/transactions',label:'Payment verification',Icon:ShieldCheck},
  {href:base+'/qr',label:'Invitation design & QR',Icon:QrCode},
  {href:base+'/preview',label:'Preview invitation',Icon:Eye},
  {href:base+'/notifications',label:'Notification history',Icon:Mail},
  {href:base+'/thank-you',label:'Thank-you cards',Icon:Heart},
 ]:[{href:'/studio',label:'Invitation studio',Icon:Sparkles}];
 const visibleEventLinks=eventLinks.filter(item=>features.GIFTING_ENABLED!==false||!['Receiving accounts','Gift wishlist','Contribution report','Payment verification'].includes(item.label));
 function navItem({href,label,Icon,active}:Item){return <Link key={href} href={href} className="hostNavItem" aria-label={label} aria-current={(active??(path===href))?'page':undefined} aria-describedby={tooltip?.label===label?tooltipId:undefined} title={collapsed?label:undefined} onClick={navigate} onMouseEnter={e=>showTooltip(e.currentTarget,label)} onMouseLeave={()=>setTooltip(null)} onFocus={e=>showTooltip(e.currentTarget,label)} onBlur={()=>setTooltip(null)}><Icon size={21} aria-hidden="true"/><span className="hostNavLabel">{localizeText(label)}</span></Link>}
 return <WorkspaceContext.Provider value={{eventId,event}}><div className={`hostWorkspace${collapsed?' hostWorkspaceCollapsed':''}${ready?' hostWorkspaceReady':''}`}>
 <header className="hostIdentity"><button className="hostSidebarToggle" type="button" onClick={toggle} aria-expanded={!collapsed} aria-controls="hostSidebarNav" aria-label={collapsed?localize("ui.37a5d6485e109bf6",{defaultValue:"Expand sidebar"}):localize("ui.aab31cde23ba9783",{defaultValue:"Collapse sidebar"})} title={collapsed?localize("ui.37a5d6485e109bf6",{defaultValue:"Expand sidebar"}):localize("ui.aab31cde23ba9783",{defaultValue:"Collapse sidebar"})}>{collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>}</button><div className="hostIdentityMain"><Link href="/events" className="hostBrand" aria-label="Celebrations home"><BrandLogo size={32}/></Link><div className="hostWelcome"><HostLanguageSwitcher/><p className="hostGreeting">{name?localize("ui.53271b7f71daf571",{defaultValue:"Welcome back, {{value0}}!",value0:(name)}):localize("ui.38533a49bd19232c",{defaultValue:"Welcome to your workspace"})}</p></div></div></header>
 <aside className="hostSidebar" aria-label={localize("ui.ad47638d3660c9b1",{defaultValue:"Host navigation"})}>
 <div className="hostSidebarBody" id="hostSidebarNav"><nav aria-label={localize("ui.87bb59ba2f92f2a5",{defaultValue:"Workspace"})}><p className="hostNavSection">{localize("ui.87bb59ba2f92f2a5",{defaultValue:"Workspace"})}</p>{account.map(navItem)}<div className="hostNavDivider"/><p className="hostNavSection" title={event?.title||localize("ui.af4ea02852eb08ea",{defaultValue:"Invitation"})}>{eventId?event?.title||localize("ui.af38142a10020703",{defaultValue:"Your celebration"}):localize("ui.af4ea02852eb08ea",{defaultValue:"Invitation"})}</p><nav aria-label={eventId?localize("ui.4a8b403035a2b673",{defaultValue:"Celebration"}):localize("ui.af4ea02852eb08ea",{defaultValue:"Invitation"})}>{visibleEventLinks.map(navItem)}</nav></nav></div>
 <div className="hostSidebarFooter">{error&&<p className="hostSidebarError" role="alert">{localizeText(error)}</p>}<button type="button" className="hostNavItem" disabled={busy} onClick={()=>void signOut()} aria-label={busy?localize("ui.08c6bd357b7d7b81",{defaultValue:"Signing out…"}):localize("ui.48f0d3d397d49f13",{defaultValue:"Sign out"})} title={localize("ui.48f0d3d397d49f13",{defaultValue:"Sign out"})} onMouseEnter={e=>showTooltip(e.currentTarget,'Sign out')} onMouseLeave={()=>setTooltip(null)} onFocus={e=>showTooltip(e.currentTarget,'Sign out')} onBlur={()=>setTooltip(null)}><LogOut size={21} aria-hidden="true"/><span className="hostNavLabel">{busy?localize("ui.08c6bd357b7d7b81",{defaultValue:"Signing out…"}):localize("ui.48f0d3d397d49f13",{defaultValue:"Sign out"})}</span></button></div></aside>
 {!collapsed&&<button className="hostSidebarBackdrop" aria-label={localize("ui.17e28e2302175d33",{defaultValue:"Close sidebar"})} onClick={()=>setCollapsed(true)}/>}
 <div className="hostWorkspaceContent" id="host-workspace-content">{children}</div>{tooltip&&<div className="hostNavTooltip" id={tooltipId} role="tooltip" style={{left:tooltip.x,top:tooltip.y}}>{tooltip.label}</div>}
 </div></WorkspaceContext.Provider>;
}
