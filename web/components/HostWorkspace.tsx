'use client';
import {BrandLogo} from './BrandingProvider';
/* localization:instrumented */
import {localize,localizeText} from '../lib/localization.mjs';
import {HostLanguageSwitcher,useLocalizationRender} from './LocalizationProvider';

import {createContext,useContext,useEffect,useId,useState,type ReactNode} from 'react';
import {usePathname,useRouter,useSearchParams} from 'next/navigation';
import Link from 'next/link';
import {ShieldCheck,Activity,CalendarDays,CheckSquare,Eye,Pencil,Gift,Heart,Images,Landmark,LayoutDashboard,LogOut,Mail,PanelLeftClose,PanelLeftOpen,Plus,QrCode,ReceiptText,Sparkles,BookOpen,NotebookPen,MonitorPlay,CalendarRange,UsersRound,HandHeart,MessageCircle,CalendarHeart,Gift as GiftIcon2,Store,BookHeart,ListChecks,Wallet,Clock,DoorOpen,Megaphone,Smartphone,Briefcase,Users2,Award,Armchair,Ticket,ChevronDown,UserRound,Users,type LucideIcon} from 'lucide-react';
import {hostApi,hostRequest} from '../lib/host-api.mjs';
import {useGrowth} from './GrowthProvider';
import {pendingReferral} from '../lib/reminders.mjs';
import {pendingOffer} from '../lib/acquisition.mjs';
import {saveFlash} from '../lib/flash.mjs';
import {eventIdForRoute} from '../lib/focused.mjs';
import './host-sidebar.css';
type EventInfo={title:string;eventDate:string|null;status:string};
const WorkspaceContext=createContext<{eventId:string|null;event:EventInfo|null}>({eventId:null,event:null});
export const useHostWorkspace=()=>useContext(WorkspaceContext);
type Item={href:string;label:string;Icon:LucideIcon;active?:boolean};
export default function HostWorkspace({children}:{children:ReactNode}){useLocalizationRender();
 const growth=useGrowth().features as Record<string,boolean>;
 const path=usePathname(),query=useSearchParams(),router=useRouter(),eventId=eventIdForRoute(path,query.get('eventId'));
 const [name,setName]=useState(''),[collapsed,setCollapsed]=useState(false),[ready,setReady]=useState(false),[busy,setBusy]=useState(false),[error,setError]=useState('');
 const [hostFeatures,setHostFeatures]=useState<Record<string,boolean>>({}),[helping,setHelping]=useState(0);
 // A first-celebration-free offer remembered from a guest page is claimed once after sign-in.
 useEffect(()=>{let token:string|null=null;try{token=pendingOffer(localStorage)}catch{}if(!token)return;void hostRequest(fetch,'/v1/me/offers/first-free',{method:'POST',payload:{eventToken:token}}).then((r:{granted?:boolean;message?:string})=>{try{localStorage.removeItem('gift.offer');if(r?.message)saveFlash(sessionStorage,r.granted?'success':'info',r.message)}catch{}}).catch(()=>{try{localStorage.removeItem('gift.offer')}catch{}})},[]);
 // A friend's referral link remembered at sign-up is claimed once, then forgotten.
 useEffect(()=>{let code:string|null=null;try{code=pendingReferral(localStorage)}catch{}if(!code)return;void hostRequest(fetch,'/v1/me/referrals/claim',{method:'POST',payload:{code}}).then(()=>{try{localStorage.removeItem('gift.ref')}catch{}}).catch(()=>{})},[]);
 useEffect(()=>{let active=true;void fetch('/backend/api/v1/me/co-hosting',{credentials:'include',cache:'no-store'}).then(r=>r.ok?r.json():[]).then(list=>{if(active&&Array.isArray(list))setHelping(list.length)}).catch(()=>{});return()=>{active=false}},[]);
 useEffect(()=>{let active=true;void fetch('/backend/api/v1/me/features',{credentials:'include',cache:'no-store'}).then(r=>r.ok?r.json():{}).then(f=>{if(active)setHostFeatures(f)}).catch(()=>{});return()=>{active=false}},[]);
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
 const account:Item[]=[{href:'/dashboard',label:'Overview',Icon:LayoutDashboard},{href:'/events',label:'Celebrations',Icon:CalendarDays},{href:'/events/new',label:'Create celebration',Icon:Plus},...(hostFeatures.GROUP_COLLECTIONS?[{href:'/collections',label:'Group gifts',Icon:Users2}]:[]),...(hostFeatures.PARTNER_ACCOUNTS?[{href:'/partner',label:'Partner workspace',Icon:Briefcase}]:[]),...(hostFeatures.GIFT_LEDGER?[{href:'/ledger',label:'Gift ledger',Icon:BookOpen}]:[]),...(helping>0?[{href:'/helping',label:'Celebrations I help with',Icon:HandHeart}]:[]),...(hostFeatures.OCCASION_REMINDERS?[{href:'/reminders',label:'Dates to remember',Icon:CalendarHeart}]:[]),...(hostFeatures.VENDOR_DIRECTORY?[{href:'/vendors',label:'Vendors',Icon:Store}]:[]),...(hostFeatures.HOST_REFERRALS?[{href:'/referrals',label:'Invite friends',Icon:GiftIcon2}]:[]),{href:'/host/payment-settings',label:'Receiving accounts',Icon:Landmark},{href:'/host/monetization',label:'Plans & partnerships',Icon:Sparkles},{href:'/host/account',label:'Account',Icon:UserRound}];
 const base=eventId?`/events/${eventId}`:'';
 const eventLinks:Item[]=eventId?[
  {href:base,label:'Celebration overview',Icon:CheckSquare},
  {href:base+'/edit',label:'Event details',Icon:Pencil},
  ...(features.EVENT_FUNCTIONS?[{href:base+'/functions',label:'Schedule & functions',Icon:CalendarRange}]:[]),
  ...(features.PLANNING_CHECKLIST?[{href:base+'/checklist',label:'Planning checklist',Icon:ListChecks}]:[]),
  ...(features.SEATING_PLAN?[{href:base+'/seating',label:'Seating plan',Icon:Armchair}]:[]),
  ...(features.EVENT_BUDGET?[{href:base+'/budget',label:'Budget',Icon:Wallet}]:[]),
  ...(features.RUN_SHEET?[{href:base+'/run-sheet',label:'Run sheet',Icon:Clock}]:[]),
  ...(features.CO_HOSTS?[{href:base+'/co-hosts',label:'Co-hosts & helpers',Icon:UsersRound}]:[]),
  ...(features.WHATSAPP_MESSAGING?[{href:base+'/whatsapp',label:'WhatsApp messages',Icon:MessageCircle}]:[]),
  ...((features.CELEBRATION_PLANNING||features.EVENT_REUSE)?[{href:base+'/planning',label:'Celebration settings',Icon:Sparkles}]:[]),
  {href:base+'/receiving-accounts',label:'Receiving accounts',Icon:Landmark},
  {href:`/gifts?eventId=${eventId}`,label:'Gift wishlist',Icon:Gift,active:path==='/gifts'},
  ...((features.RSVP||features.GUESTBOOK)?[{href:base+'/celebration',label:'Guests & responses',Icon:Users}]:[]),
  ...(features.MEMORY_ALBUM?[{href:base+'/album',label:'Memory album',Icon:Images}]:[]),
  ...(features.LIVE_WALL?[{href:base+'/live-wall',label:'Live venue wall',Icon:MonitorPlay}]:[]),
  ...(features.GUEST_CHECKIN?[{href:base+'/checkin',label:'Guest check-in',Icon:DoorOpen}]:[]),
  ...(features.GUEST_PASSES?[{href:base+'/passes',label:'Guest passes',Icon:Ticket}]:[]),
  ...(features.LIVE_UPDATES?[{href:base+'/updates',label:'Live updates',Icon:Megaphone}]:[]),
  {href:base+'/contributions',label:'Contribution report',Icon:Activity},
  ...(features.SHAGUN_REGISTER?[{href:base+'/register',label:'Gift register',Icon:NotebookPen}]:[]),
  {href:base+'/transactions',label:'Payment verification',Icon:ShieldCheck},
  ...(growth.CUSTOM_RECEIPTS?[{href:base+'/receipts',label:'Guest receipts',Icon:ReceiptText}]:[]),
  {href:base+'/qr',label:'Invitation design & QR',Icon:QrCode},
  ...(features.STORY_CARDS?[{href:base+'/story-cards',label:'Status & Story cards',Icon:Smartphone}]:[]),
  ...(features.CELEBRATION_SHOWCASE?[{href:base+'/showcase',label:'Share in showcase',Icon:Award}]:[]),
  {href:base+'/preview',label:'Preview invitation',Icon:Eye},
  {href:base+'/notifications',label:'Notification history',Icon:Mail},
  {href:base+'/thank-you',label:'Thank-you cards',Icon:Heart},
  ...(features.MEMORY_BOOK?[{href:base+'/memory-book',label:'Memory book',Icon:BookHeart}]:[]),
 ]:[{href:'/studio',label:'Invitation studio',Icon:Sparkles}];
 const visibleEventLinks=eventLinks.filter(item=>features.GIFTING_ENABLED!==false||!['Receiving accounts','Gift wishlist','Contribution report','Payment verification','Guest receipts'].includes(item.label));
 // The celebration menu, grouped by stage so 30+ tools stay easy to scan. Collapsed (icon-only) mode stays a flat list.
 const GROUPS:[string,string[]][]=[
  ['Plan',['Event details','Schedule & functions','Planning checklist','Seating plan','Budget','Run sheet','Co-hosts & helpers','Celebration settings']],
  ['Invites & guests',['Invitation design & QR','Preview invitation','Status & Story cards','WhatsApp messages','Guests & responses','Guest passes','Notification history']],
  ['Gifts & money',['Gift wishlist','Receiving accounts','Payment verification','Contribution report','Gift register','Guest receipts']],
  ['On the day',['Live venue wall','Guest check-in','Live updates']],
  ['After the event',['Memory album','Memory book','Thank-you cards','Share in showcase']],
 ];
 const [openGroups,setOpenGroups]=useState<Record<string,boolean>>({});
 useEffect(()=>{try{setOpenGroups(JSON.parse(localStorage.getItem('gift.host.nav.groups')||'{}'))}catch{}},[]);
 function toggleGroup(name:string,open:boolean){setOpenGroups(g=>{const next={...g,[name]:open};try{localStorage.setItem('gift.host.nav.groups',JSON.stringify(next))}catch{}return next})}
 function renderEventNav(){
  if(!eventId||collapsed)return visibleEventLinks.map(navItem);
  const isActive=(i:Item)=>i.active??(path===i.href);
  const grouped=new Set(GROUPS.flatMap(([,labels])=>labels));
  const top=visibleEventLinks.filter(i=>!grouped.has(i.label));
  return <>{top.map(navItem)}{GROUPS.map(([name,labels])=>{const items=visibleEventLinks.filter(i=>labels.includes(i.label));if(!items.length)return null;
   const open=items.some(isActive)||(openGroups[name]??name==='Plan');
   return <details key={name} className="hostNavGroup" open={open} onToggle={e=>{const o=(e.currentTarget as HTMLDetailsElement).open;if(o!==open)toggleGroup(name,o)}}>
    <summary className="hostNavGroupTitle">{localizeText(name)}<span className="hostNavGroupCount">{items.length}</span><ChevronDown size={14} aria-hidden="true"/></summary>{items.map(navItem)}</details>})}</>;
 }
 function navItem({href,label,Icon,active}:Item){return <Link key={href} href={href} className="hostNavItem" aria-label={label} aria-current={(active??(path===href))?'page':undefined} aria-describedby={tooltip?.label===label?tooltipId:undefined} title={collapsed?label:undefined} onClick={navigate} onMouseEnter={e=>showTooltip(e.currentTarget,label)} onMouseLeave={()=>setTooltip(null)} onFocus={e=>showTooltip(e.currentTarget,label)} onBlur={()=>setTooltip(null)}><Icon size={21} aria-hidden="true"/><span className="hostNavLabel">{localizeText(label)}</span></Link>}
 return <WorkspaceContext.Provider value={{eventId,event}}><div className={`hostWorkspace${collapsed?' hostWorkspaceCollapsed':''}${ready?' hostWorkspaceReady':''}`}>
 <header className="hostIdentity"><button className="hostSidebarToggle" type="button" onClick={toggle} aria-expanded={!collapsed} aria-controls="hostSidebarNav" aria-label={collapsed?localize("ui.37a5d6485e109bf6",{defaultValue:"Expand sidebar"}):localize("ui.aab31cde23ba9783",{defaultValue:"Collapse sidebar"})} title={collapsed?localize("ui.37a5d6485e109bf6",{defaultValue:"Expand sidebar"}):localize("ui.aab31cde23ba9783",{defaultValue:"Collapse sidebar"})}>{collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>}</button><div className="hostIdentityMain"><Link href="/events" className="hostBrand" aria-label="Celebrations home"><BrandLogo size={32}/></Link><div className="hostWelcome"><HostLanguageSwitcher/><p className="hostGreeting">{name?localize("ui.53271b7f71daf571",{defaultValue:"Welcome back, {{value0}}!",value0:(name)}):localize("ui.38533a49bd19232c",{defaultValue:"Welcome to your workspace"})}</p></div></div></header>
 <aside className="hostSidebar" aria-label={localize("ui.ad47638d3660c9b1",{defaultValue:"Host navigation"})}>
 <div className="hostSidebarBody" id="hostSidebarNav"><nav aria-label={localize("ui.87bb59ba2f92f2a5",{defaultValue:"Workspace"})}><p className="hostNavSection">{localize("ui.87bb59ba2f92f2a5",{defaultValue:"Workspace"})}</p>{account.map(navItem)}<div className="hostNavDivider"/><p className="hostNavSection" title={event?.title||localize("ui.af4ea02852eb08ea",{defaultValue:"Invitation"})}>{eventId?event?.title||localize("ui.af38142a10020703",{defaultValue:"Your celebration"}):localize("ui.af4ea02852eb08ea",{defaultValue:"Invitation"})}</p><nav aria-label={eventId?localize("ui.4a8b403035a2b673",{defaultValue:"Celebration"}):localize("ui.af4ea02852eb08ea",{defaultValue:"Invitation"})}>{renderEventNav()}</nav></nav></div>
 <div className="hostSidebarFooter">{error&&<p className="hostSidebarError" role="alert">{localizeText(error)}</p>}<button type="button" className="hostNavItem" disabled={busy} onClick={()=>void signOut()} aria-label={busy?localize("ui.08c6bd357b7d7b81",{defaultValue:"Signing out…"}):localize("ui.48f0d3d397d49f13",{defaultValue:"Sign out"})} title={localize("ui.48f0d3d397d49f13",{defaultValue:"Sign out"})} onMouseEnter={e=>showTooltip(e.currentTarget,'Sign out')} onMouseLeave={()=>setTooltip(null)} onFocus={e=>showTooltip(e.currentTarget,'Sign out')} onBlur={()=>setTooltip(null)}><LogOut size={21} aria-hidden="true"/><span className="hostNavLabel">{busy?localize("ui.08c6bd357b7d7b81",{defaultValue:"Signing out…"}):localize("ui.48f0d3d397d49f13",{defaultValue:"Sign out"})}</span></button></div></aside>
 {!collapsed&&<button className="hostSidebarBackdrop" aria-label={localize("ui.17e28e2302175d33",{defaultValue:"Close sidebar"})} onClick={()=>setCollapsed(true)}/>}
 <div className="hostWorkspaceContent" id="host-workspace-content">{children}</div>{tooltip&&<div className="hostNavTooltip" id={tooltipId} role="tooltip" style={{left:tooltip.x,top:tooltip.y}}>{tooltip.label}</div>}
 </div></WorkspaceContext.Provider>;
}
