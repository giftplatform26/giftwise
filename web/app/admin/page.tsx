'use client';
import GrowthControls from '../../components/GrowthControls';
import GrowthFunnel from '../../components/GrowthFunnel';
import ProfilePreferences from '../../components/ProfilePreferences';
import BusinessAudit from '../../components/BusinessAudit';
import EmailDeliveryStatus from '../../components/EmailDeliveryStatus';
import BrandingAdmin from '../../components/BrandingAdmin';
import {activeLocale} from '../../lib/localization.mjs';

/* localization:instrumented */
import {localize,localizeText} from '../../lib/localization.mjs';
import {useLocalizationRender} from '../../components/LocalizationProvider';

import LocalizationAdmin,{LanguageScopeControl} from '../../components/LocalizationAdmin';
import {PreferredLanguage} from '../../components/LocalizationProvider';
import ProductionReadiness from '../../components/ProductionReadiness';
import AdminEventModules from '../../components/AdminEventModules';
import {AdminVendors,AdminReferrals,AdminShowcase,AdminPresets,AdminGrowthResults} from '../../components/AdminGrowthPanels';
import AdminAiDesignDetails from '../../components/AdminAiDesignDetails';
import {useCallback,useEffect,useMemo,useRef,useState} from 'react';
import {useExperience} from '../../components/ExperienceProvider';
import PlatformVerificationControls from '../../components/PlatformVerificationControls';
import MobileVerification from '../../components/MobileVerification';
import AccountEmailVerification from '../../components/AccountEmailVerification';
import GroupMemberEmailPolicy from '../../components/GroupMemberEmailPolicy';
import AccountPolicies from './AccountPolicies';
import AdminAudit from './AdminAudit';
import {AdminInvitationUploadAccess} from '../../components/HostInvitationUploads';
import AdminInteractions from './AdminInteractions';
import MonetizationControls from '../../components/MonetizationControls';
import CommercialAdminControls from '../../components/CommercialAdminControls';
import PaymentControls from '../../components/PaymentControls';
import AdminPaymentMethods from '../../components/admin/AdminPaymentMethods';
import AdminShell from '../../components/admin/AdminShell';
import {ConfirmDialog,StatusPill,EmptyState,Callout} from '../../components/admin/AdminUi';
import AdminUsers from '../../components/admin/AdminUsers';
import AdminVerificationInbox from '../../components/admin/AdminVerificationInbox';
import AdminGroupPayments from '../../components/admin/AdminGroupPayments';
import {ADMIN_GROUPS,ADMIN_SECTIONS,groupModules,parseAdminHash,sectionHref} from '../../lib/admin-sections.mjs';
import {GROWTH_FEATURES} from '../../lib/growth.mjs';
type Summary={hosts:number;events:number;publishedEvents:number;groups:number};
type GroupTransferReport={reportedUnverified:number;reportedUnverifiedPaise:number;verifiedRecords:number;rejectedRecords:number};
type GuestbookReport={pending:number;approved:number;hidden:number};
type NotificationReport={manualClaimOutbox:number};
type Module={module_code:string;enabled:boolean};
type Host={id:string;username:string;email:string};
type HostModule={module_code:string;platform_enabled:boolean;host_enabled:boolean;effective_enabled:boolean};
type EventRow={id:string;title:string;status:string;occasion_type:string;host_name:string;album_enabled:boolean;album_available:boolean;ai_enabled:boolean;ai_available:boolean};
type Audit={action:string;module_code:string|null;created_at:string};
type Snapshot={razorpay?:{platformEnabled:boolean;mode:string}|null;monetization?:{enabled:boolean}|null;verification?:{settings:{mobileOtpEnabled:boolean;emailVerificationEnabled:boolean}}|null;inbox?:number|null};
type Pending={code:string;enabled:boolean};
const modules:Record<string,string>={BUDGET_SHARING:'Budget sharing with family',VENDOR_BOOKINGS:'Vendor quotes & bookings',GUEST_FEEDBACK:'Guest feedback after the event',SHARED_ALBUM:'Shared photo album via QR',GUEST_TRAVEL:'Travel & stay for outstation guests',GUEST_HOUSEHOLDS:'Guest list & households',SEATING_PLAN:'Seating plan',GUEST_PASSES:'Guest passes (QR check-in)',HOST_NOTIFICATIONS:'Host notifications & daily digest',TRY_WITHOUT_ACCOUNT:'Try without an account (platform only — visitors have no host or event yet)',STORY_CARDS:'Status & Story cards',PARTNER_ACCOUNTS:'Planner & venue partners',GROUP_COLLECTIONS:'Office & group gifts (no event needed)',GUEST_FIRST_FREE:'First celebration free for guests',CELEBRATION_SHOWCASE:'Celebration showcase',PLANNING_CHECKLIST:'Planning checklist',EVENT_BUDGET:'Budget & vendor payments',RUN_SHEET:'Day-of run sheet',CATERING_HEADCOUNT:'Catering headcount (meal choices)',GUEST_CHECKIN:'Entry check-in',LIVE_UPDATES:'Live updates & stream link',RETURN_GIFTS:'Return gifts tracker',OCCASION_REMINDERS:'Dates to remember',HOST_REFERRALS:'Invite-a-friend referrals',YEARLY_REHOST:'Yearly re-hosting suggestions',MEMORY_BOOK:'Printable memory book',VENDOR_DIRECTORY:'Vendor directory',EXPERIENCE_GIFTS:'Experience & charity wishes',WHATSAPP_MESSAGING:'WhatsApp invitations & replies',EVENT_FUNCTIONS:'Multi-function schedule',CO_HOSTS:'Co-hosts & gift-desk helpers',LIVE_WALL:'Live venue wall',SHAGUN_REGISTER:'Shagun / gift register',GIFT_LEDGER:'Personal gift ledger',CELEBRATION_PLANNING:'Simple celebrations & optional gifting',EVENT_ESSENTIALS:'Venue details & calendar',EVENT_REUSE:'Repeat celebrations',GROUP_GIFTING:'Group gifting',GUEST_GIFTS:'Guest-added gifts',RSVP:'RSVP',GUEST_ATTENDANCE:'Guest attendance',WHATSAPP_SHARING:'WhatsApp sharing',GUESTBOOK:'Digital guestbook',MEMORY_ALBUM:'Event memory albums',AI_INVITATION:'Design with AI',SOCIAL_SHARING:'Social invitation sharing'};
const HOST_LIST_LIMIT=100;
const UUID_PATTERN=/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
function adminCsrfToken():string {
 const entry=document.cookie.split(';').map(v=>v.trim()).find(v=>v.startsWith('XSRF-TOKEN='));
 return entry?decodeURIComponent(entry.substring('XSRF-TOKEN='.length)):'';
}
async function api<T>(path:string,options?:RequestInit):Promise<T>{const modifying=!!options?.method && options.method.toUpperCase()!=='GET';
 if(modifying){const init=await fetch('/backend/api/auth/csrf',{credentials:'include',cache:'no-store'});if(!init.ok)throw Error(`Secure session initialization failed (${init.status})`);}
 const token=modifying?adminCsrfToken():'';
 if(modifying&&!token)throw Error('CSRF token missing; reload and sign in again');
 const response=await fetch('/backend/api/admin/console'+path,{credentials:'include',cache:'no-store',...options,headers:{...options?.headers,...(modifying?{'X-XSRF-TOKEN':token}:{})}});if(!response.ok)throw Error(response.status===403?'Platform administrator access required':`Request failed (${response.status})`);return response.json();}
async function readJson<T>(url:string):Promise<T>{const r=await fetch(url,{credentials:'include',cache:'no-store'});if(!r.ok)throw Error(String(r.status));return r.json();}
const label=(code:string)=>modules[code]||code.replaceAll('_',' ').toLowerCase();

/** On/off filter shared by the feature lists. */
function StateFilter({value,onChange,name}:{value:string;onChange:(v:string)=>void;name:string}){
 return <fieldset className="admSegmented"><legend className="admVisuallyHidden">{localizeText('Show')}</legend>
  {[['ALL','All'],['ON','On'],['OFF','Off']].map(([v,text])=><label key={v}><input type="radio" name={name} value={v} checked={value===v} onChange={()=>onChange(v)}/><span>{localizeText(text)}</span></label>)}
 </fieldset>;
}

export default function AdminConsole(){useLocalizationRender();
 const {mode}=useExperience();const focused=mode==='FOCUSED';const [paymentsVersion,setPaymentsVersion]=useState(0);
 const [summary,setSummary]=useState<Summary|null>(null),[list,setList]=useState<Module[]|null>(null),[hosts,setHosts]=useState<Host[]|null>(null),[events,setEvents]=useState<EventRow[]|null>(null),[history,setHistory]=useState<Audit[]|null>(null),[snapshot,setSnapshot]=useState<Snapshot>({});
 const [groupReport,setGroupReport]=useState<GroupTransferReport|null>(null),[guestbookReport,setGuestbookReport]=useState<GuestbookReport|null>(null),[notificationReport,setNotificationReport]=useState<NotificationReport|null>(null);
 const [selected,setSelected]=useState(''),[hostModules,setHostModules]=useState<HostModule[]>([]),[search,setSearch]=useState(''),[error,setError]=useState(''),[notice,setNotice]=useState(''),[busy,setBusy]=useState(false),[tab,setTab]=useState('overview'),[ready,setReady]=useState(false);
 const [featureQuery,setFeatureQuery]=useState(''),[featureState,setFeatureState]=useState('ALL'),[hostModuleQuery,setHostModuleQuery]=useState(''),[hostModuleState,setHostModuleState]=useState('ALL');
 const [eventQuery,setEventQuery]=useState(''),[eventStatus,setEventStatus]=useState('ALL'),[pending,setPending]=useState<Pending|null>(null),[reloadKey,setReloadKey]=useState(0);
 const [auditRevision,setAuditRevision]=useState(0);
 const selectedRef=useRef(selected);selectedRef.current=selected;
 const tabRef=useRef(tab);tabRef.current=tab;
 // Each section is addressable as /admin#<section>[/<search>] so links, bookmarks and Back work.
 useEffect(()=>{const sync=()=>{const next=parseAdminHash(window.location.hash);setTab(next.section);if(next.section==='features'&&next.query){setFeatureQuery(next.query);setFeatureState('ALL');}if(next.section==='hosts'&&UUID_PATTERN.test(next.query))setSelected(next.query);setNotice('');setReady(true);};sync();window.addEventListener('hashchange',sync);return()=>window.removeEventListener('hashchange',sync);},[]);
 const refresh=useCallback(async()=>{try{
   const currentTab=tabRef.current;const selected=selectedRef.current;
   if(currentTab==='overview'){
     const [s,a]=await Promise.all([api<Summary>('/summary'),api<Audit[]>('/audit')]);setSummary(s);setHistory(a);
     const [m,razorpay,monetization,verification,inbox]=await Promise.allSettled([api<Module[]>('/modules'),readJson<{platformEnabled:boolean;mode:string}>('/backend/api/admin/console/payments/razorpay-status'),readJson<{enabled:boolean}>('/backend/api/admin/console/monetization'),readJson<{settings:{mobileOtpEnabled:boolean;emailVerificationEnabled:boolean}}>('/backend/api/admin/verification-settings'),readJson<{queues:{count:number}[]}>('/backend/api/admin/console/verification-inbox')]);
     if(m.status==='fulfilled')setList(m.value);
     setSnapshot({razorpay:razorpay.status==='fulfilled'?razorpay.value:null,monetization:monetization.status==='fulfilled'?monetization.value:null,verification:verification.status==='fulfilled'?verification.value:null,inbox:inbox.status==='fulfilled'?inbox.value.queues.reduce((sum,q)=>sum+Number(q.count||0),0):null});
   }else if(currentTab==='features'){setList(await api<Module[]>('/modules'));}
   else if(currentTab==='hosts'){
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
 useEffect(()=>{if(ready)void refresh();},[tab,ready,refresh]);
 useEffect(()=>{let active=true;setHostModules([]);if(selected)void api<HostModule[]>('/hosts/'+selected+'/modules').then(x=>{if(active)setHostModules(x)}).catch(e=>{if(active)setError(String(e))});return()=>{active=false};},[selected]);
 async function toggle(path:string,enabled:boolean):Promise<boolean>{if(busy)return false;setBusy(true);setNotice('');try{await api(path,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({enabled})});await refresh();return true;}catch(e){setError(String(e));return false;}finally{setBusy(false);}}
 async function confirmPlatformToggle(){if(!pending)return;const {code,enabled}=pending;const ok=await toggle('/modules/'+code,enabled);setPending(null);if(ok)setNotice(localize('admin.feature.saved',{defaultValue:'{{name}} is now {{state}} for the platform. The change is recorded in the audit log.',name:localizeText(label(code)),state:localizeText(enabled?'on':'off')}));}
 function reloadSection(){setNotice('');setReloadKey(k=>k+1);void refresh();}
 const filtered=(hosts||[]).filter(h=>(h.username+' '+h.email).toLowerCase().includes(search.toLowerCase()));
 const selectedHost=(hosts||[]).find(h=>h.id===selected);
 const searchExtra=useMemo(()=>[...Object.entries(modules).map(([code,name])=>({label:name,section:'features',query:name,keywords:'feature module switch '+code.replaceAll('_',' ').toLowerCase()})),...Object.entries(GROWTH_FEATURES as Record<string,string[]>).map(([,[name,help]])=>({label:name,section:'experiences',keywords:help.toLowerCase()}))],[]);
 const enabledCount=list?list.filter(m=>m.enabled).length:0;
 const featureGroups=list?groupModules(list,modules,{query:featureQuery,state:featureState}):[];
 const hostGroups=groupModules(hostModules,modules,{query:hostModuleQuery,state:hostModuleState,isOn:(r:HostModule)=>r.effective_enabled});
 const eventStatuses=[...new Set((events||[]).map(e=>e.status))].sort();
 const visibleEvents=(events||[]).filter(e=>(eventStatus==='ALL'||e.status===eventStatus)&&(e.title+' '+(e.host_name||'')+' '+e.occasion_type).toLowerCase().includes(eventQuery.trim().toLowerCase()));
 const isForbidden=error==='Platform administrator access required';
 const actions=<button type="button" className="admButton" onClick={reloadSection}>{localize("ui.0e91610117029a62",{defaultValue:"Refresh"})}</button>;
 const pendingName=pending?localizeText(label(pending.code)):'';
 return <AdminShell current={tab} actions={actions} searchExtra={searchExtra}>
  {error&&<div role="alert" className="acError">{localizeText(error)} {isForbidden?<a href="/login">{localize("ui.9d6322c1f4d9d3f3",{defaultValue:"Login"})}</a>:<button type="button" className="admLinkButton" onClick={reloadSection}>{localizeText('Retry')}</button>}</div>}
  {notice&&<div role="status" className="admNotice">{notice}</div>}
  <div key={`${tab}-${reloadKey}`} className="admSection">

  {tab==='overview'&&<>
   <section className="admGlance" aria-labelledby="glance-title"><h2 id="glance-title" className="admSectionTitle">{localizeText('At a glance')}</h2>
    <div className="admGlanceGrid">
     <a className="admGlanceCard" href={sectionHref('inbox')}><span>{localizeText('Verification inbox')}</span><strong>{snapshot.inbox===undefined?'—':snapshot.inbox===null?localizeText('Unavailable'):localize('admin.inbox.total',{defaultValue:'{{count}} items waiting',count:snapshot.inbox})}</strong><small>{localizeText('Open inbox →')}</small></a>
     <a className="admGlanceCard" href={sectionHref('features')}><span>{localizeText('Platform features')}</span><strong>{list?localize('admin.glance.features',{defaultValue:'{{on}} of {{total}} on',on:enabledCount,total:list.length}):'—'}</strong><small>{localizeText('Manage features →')}</small></a>
     <a className="admGlanceCard" href={sectionHref('payments')}><span>{localizeText('Razorpay checkout')}</span>{snapshot.razorpay===undefined?<strong>—</strong>:<StatusPill on={!!snapshot.razorpay?.platformEnabled} unknown={snapshot.razorpay===null}/>}<small>{snapshot.razorpay?localizeText(snapshot.razorpay.mode==='LIVE'?'Live keys':snapshot.razorpay.mode==='TEST'?'Test keys':'No live keys'):localizeText('Payment methods →')}</small></a>
     <a className="admGlanceCard" href={sectionHref('monetization')}><span>{localizeText('Platform fees')}</span>{snapshot.monetization===undefined?<strong>—</strong>:<StatusPill on={!!snapshot.monetization?.enabled} unknown={snapshot.monetization===null}/>}<small>{localizeText('Fees & partners →')}</small></a>
     <a className="admGlanceCard" href={sectionHref('policies')}><span>{localizeText('Mobile OTP')}</span>{snapshot.verification===undefined?<strong>—</strong>:<StatusPill on={!!snapshot.verification?.settings.mobileOtpEnabled} unknown={snapshot.verification===null}/>}<small>{snapshot.verification?localizeText(snapshot.verification.settings.emailVerificationEnabled?'Email verification required':'Email verification bypassed'):localizeText('Sign-in & verification →')}</small></a>
    </div>
   </section>
   <section className="acMetrics" aria-label={localizeText('Platform totals')}>{[['Total hosts',summary?.hosts],['Total events',summary?.events],['Published events',summary?.publishedEvents],['Group gifts',summary?.groups]].map(([text,value])=><article key={String(text)}><span>{localizeText(String(text))}</span><strong>{value??'—'}</strong></article>)}</section>
   <section className="acPanel" aria-labelledby="directory-title"><h2 id="directory-title">{localizeText('All settings')}</h2><p>{localizeText('Every control in one place. Use “Find a setting” in the menu if you know what you are looking for.')}</p>
    <div className="admDirectory">{ADMIN_GROUPS.filter(g=>g.label).map(group=><div key={group.id}><h3>{localizeText(group.label)}</h3><ul>{ADMIN_SECTIONS.filter(s=>s.group===group.id).map(s=><li key={s.id}><a href={sectionHref(s.id)}><strong>{localizeText(s.label)}{s.href?' ↗':''}</strong><small>{localizeText(s.description)}</small></a></li>)}</ul></div>)}</div>
   </section>
   <section className="acPanel" aria-labelledby="recent-title"><div className="admPanelHead"><h2 id="recent-title">{localize("ui.013e793c9c18e4ee",{defaultValue:"Recent administration activity"})}</h2><a href={sectionHref('activity')}>{localizeText('Open audit log →')}</a></div>
    {history===null?<p role="status">{localizeText('Loading recent activity…')}</p>:history.length===0?<EmptyState title={localizeText('No administrator changes yet.')}/>:<ul className="admActivity">{history.slice(0,8).map((x,i)=><li key={i}><strong>{x.action.replaceAll('_',' ').toLowerCase()}</strong><span>{x.module_code?localizeText(label(x.module_code)):localize("ui.c78ffe19571018fb",{defaultValue:"Platform"})}</span><time dateTime={x.created_at}>{new Date(x.created_at).toLocaleString()}</time></li>)}</ul>}
   </section>
  </>}

  {tab==='features'&&<>
   <section className="acPanel" aria-labelledby="features-title">
    <div className="admPanelHead"><h2 id="features-title">{localize("ui.741b0b4fbb436fdd",{defaultValue:"Platform-wide modules"})}</h2>{list&&<span className="admCount">{localize('admin.glance.features',{defaultValue:'{{on}} of {{total}} on',on:enabledCount,total:list.length})}</span>}</div>
    <Callout><p>{localize("ui.50885304776655c8",{defaultValue:"Disable a module platform-wide to remove its effective eligibility for all hosts. Guest-added gifts controls invitation guests adding, selecting and publishing their own individual or group gifts. It also requires Group gifting access. Published gifts remain in host reports when disabled."})}</p><p>{localizeText('Access works in three layers: platform → host → event. A feature reaches a host only when it is on here and granted under Hosts & access (and for an event, under Events).')}</p></Callout>
    <div className="admToolbar"><label className="admField">{localizeText('Find a feature')}<input type="search" value={featureQuery} placeholder={localizeText('Name or code')} onChange={e=>setFeatureQuery(e.target.value)}/></label><StateFilter name="feature-state" value={featureState} onChange={setFeatureState}/></div>
    {list===null?<p role="status">{localizeText('Loading features…')}</p>:featureGroups.length===0?<EmptyState title={localizeText('No features match.')}><button type="button" className="admLinkButton" onClick={()=>{setFeatureQuery('');setFeatureState('ALL');}}>{localizeText('Clear search and filters')}</button></EmptyState>:
     featureGroups.map(group=><div className="admCategory" key={group.id}><h3>{localizeText(group.label)} <small>{localize('admin.category.count',{defaultValue:'{{on}}/{{total}} on',on:group.rows.filter((r:Module)=>r.enabled).length,total:group.rows.length})}</small></h3>
      <div className="acRows">{group.rows.map((m:Module)=><label key={m.module_code} className="admSwitchRow"><span><strong>{localizeText(label(m.module_code))}</strong><small>{m.enabled?localizeText('On for the platform'):localizeText('Off for every host')}</small></span><input type="checkbox" role="switch" disabled={busy} checked={m.enabled} onChange={e=>setPending({code:m.module_code,enabled:e.target.checked})}/></label>)}</div>
     </div>)}
   </section>
   <AdminPresets/>
  </>}

  {tab==='experiences'&&<GrowthControls/>}
  {tab==='users'&&<AdminUsers/>}
  {tab==='inbox'&&<AdminVerificationInbox/>}
  {tab==='groups'&&<AdminGroupPayments/>}

  {tab==='hosts'&&<div className="admSplit">
   <section className="acPanel admHostList" aria-labelledby="hosts-title"><h2 id="hosts-title">{localizeText('Hosts')}</h2>
    <label className="admField">{localize("ui.542e34e1dd6aaae9",{defaultValue:"Search hosts"})}<input type="search" placeholder={localize("ui.6ed4ffa1412a4ef2",{defaultValue:"Search by host name or email"})} value={search} onChange={e=>setSearch(e.target.value)}/></label>
    {hosts===null?<p role="status">{localizeText('Loading hosts…')}</p>:filtered.length===0?<EmptyState title={localizeText(hosts.length?'No hosts match this search.':'No hosts have registered yet.')}/>:<>
     <p className="admCount" role="status">{localize('admin.hosts.count',{defaultValue:'{{shown}} of {{total}} hosts',shown:Math.min(filtered.length,HOST_LIST_LIMIT),total:hosts.length})}</p>
     <ul className="admPickList" aria-label={localize("ui.f05e43a498a59f81",{defaultValue:"Select host"})}>{filtered.slice(0,HOST_LIST_LIMIT).map(h=><li key={h.id}><button type="button" aria-pressed={selected===h.id} onClick={()=>setSelected(h.id)}><strong>{h.username}</strong><small>{h.email}</small></button></li>)}</ul>
     {filtered.length>HOST_LIST_LIMIT&&<p className="admHint">{localizeText('Showing the first 100 matches. Refine your search to find others.')}</p>}
    </>}
   </section>
   <div className="admHostDetail">
    {!selected?<section className="acPanel"><EmptyState title={localizeText('Choose a host')}>{localizeText('Select a host to review and change their feature access, uploads, language and verification settings.')}</EmptyState></section>:<>
     <section className="acPanel" aria-labelledby="host-access-title">
      <div className="admPanelHead"><div><h2 id="host-access-title">{localize("ui.c2f74d51dbdd989b",{defaultValue:"Host-specific privileges"})}</h2>{selectedHost&&<p className="admHostName"><strong>{selectedHost.username}</strong> · {selectedHost.email}</p>}</div><button type="button" className="admButton" onClick={()=>setSelected('')}>{localizeText('Change host')}</button></div>
      <p>{localizeText('Turn on a feature for this host. Features that are off platform-wide cannot be granted until they are turned on under Platform features.')}</p>
      <div className="admToolbar"><label className="admField">{localizeText('Find a feature')}<input type="search" value={hostModuleQuery} onChange={e=>setHostModuleQuery(e.target.value)}/></label><StateFilter name="host-feature-state" value={hostModuleState} onChange={setHostModuleState}/></div>
      {hostModules.length===0?<p role="status">{localizeText('Loading host access…')}</p>:hostGroups.length===0?<EmptyState title={localizeText('No features match.')}/>:hostGroups.map(group=><div className="admCategory" key={group.id}><h3>{localizeText(group.label)}</h3>
       <div className="acRows">{group.rows.map((m:HostModule)=><label key={m.module_code} className="admSwitchRow"><span><strong>{localizeText(label(m.module_code))}</strong> <small>{m.platform_enabled?localize("ui.0b228816b9b9bfa8",{defaultValue:"Platform available"}):localize("ui.f4312c3a02359965",{defaultValue:"Platform disabled"})}</small>{focused&&<small className="focusOptionStatus">{m.effective_enabled?localize("ui.9ba49cf2ee1ccea2",{defaultValue:"Available to this host"}):!m.platform_enabled?localize("ui.9dc5ea03259fa03c",{defaultValue:"Unavailable · disabled platform-wide"}):localize("ui.ec1a4a748cab1def",{defaultValue:"Unavailable · host access not granted"})}</small>}</span><input type="checkbox" role="switch" disabled={busy||!m.platform_enabled} checked={m.host_enabled} onChange={e=>void toggle('/hosts/'+selected+'/modules/'+m.module_code,e.target.checked)}/></label>)}</div>
      </div>)}
     </section>
     <AdminPaymentMethods key={`payments-${selected}`} scope="HOST" scopeId={selected} name={selectedHost?.username}/>
     <section className="acPanel" aria-labelledby="host-more-title"><h2 id="host-more-title">{localizeText('Uploads, language & verification')}</h2>
      <div className="admStack"><AdminInvitationUploadAccess key={`uploads-${selected}`} hostId={selected}/><LanguageScopeControl scope="HOST" id={selected}/><GroupMemberEmailPolicy key={selected} hostId={selected}/><AdminAiDesignDetails key={`ai-${selected}`} hostId={selected}/></div>
     </section>
    </>}
   </div>
  </div>}

  {tab==='events'&&<section className="acPanel" aria-labelledby="events-title">
   <h2 id="events-title">{localize("ui.8ecce94dc301d033",{defaultValue:"Recent events"})}</h2>
   <Callout><p>{localizeText('Memory albums and Design with AI each require their platform feature and host access under Hosts & access, plus the event switch below. Use AI design details to review access and event information. Existing memories are preserved when access is disabled.')}</p></Callout>
   <div className="admToolbar"><label className="admField">{localizeText('Find an event')}<input type="search" value={eventQuery} placeholder={localizeText('Title, host or occasion')} onChange={e=>setEventQuery(e.target.value)}/></label>
    <label className="admField">{localize("ui.920e413c7d411b61",{defaultValue:"Status"})}<select value={eventStatus} onChange={e=>setEventStatus(e.target.value)}><option value="ALL">{localizeText('All statuses')}</option>{eventStatuses.map(s=><option key={s} value={s}>{localizeText(s)}</option>)}</select></label></div>
   {events===null?<p role="status">{localizeText('Loading events…')}</p>:visibleEvents.length===0?<EmptyState title={localizeText(events.length?'No events match.':'No events yet.')}/>:<ul className="admEventList">{visibleEvents.map(e=><li key={e.id} className="admEventCard">
    <div className="admEventHead"><div><h3>{e.title}</h3><p>{e.host_name||'—'} · {e.occasion_type}</p></div><span className="admPill is-neutral">{localizeText(e.status)}</span></div>
    <div className="admEventSwitches">
     <label className="admSwitchRow"><span><strong>{localize("ui.3a5d4a5193a9ff6b",{defaultValue:"Memory album"})}</strong><small>{e.album_available?localize("ui.bc27b53cdfe52f79",{defaultValue:"Available to the host"}):e.album_enabled?localize("ui.b4419b8f4ccd5266",{defaultValue:"Also enable the platform module and host grant"}):localize("ui.80b7b0d4756b110f",{defaultValue:"Disabled for this event"})}</small></span><input type="checkbox" role="switch" aria-label={localize("ui.e6fa666a4ae99e06",{defaultValue:"Enable memory album for {{value0}}",value0:(e.title)})} disabled={busy} checked={e.album_enabled} onChange={change=>void toggle('/events/'+e.id+'/album-settings',change.target.checked)}/></label>
     <label className="admSwitchRow"><span><strong>{localize("ui.f66b148c4052d5b7",{defaultValue:"Design with AI"})}</strong><small>{e.ai_available?localize("ui.bc27b53cdfe52f79",{defaultValue:"Available to the host"}):e.ai_enabled?localize("ui.b4419b8f4ccd5266",{defaultValue:"Also enable the platform module and host grant"}):localize("ui.80b7b0d4756b110f",{defaultValue:"Disabled for this event"})}</small></span><input type="checkbox" role="switch" aria-label={localize("ui.a1a8ac106eb2578c",{defaultValue:"Enable Design with AI for {{value0}}",value0:(e.title)})} disabled={busy} checked={e.ai_enabled} onChange={change=>void toggle('/events/'+e.id+'/ai-design-settings',change.target.checked)}/></label>
    </div>
    <div className="admEventMore"><AdminEventModules eventId={e.id}/><LanguageScopeControl scope="EVENT" id={e.id}/><AdminAiDesignDetails eventId={e.id}/></div>
   </li>)}</ul>}
  </section>}

  {tab==='reports'&&<><section className="acPanel"><h2>{localize("ui.6870fddcabdb85e8",{defaultValue:"Group transfer reports"})}</h2><p>{localize("ui.f80d8b100fb4049b",{defaultValue:"These are reported transactions, not independently confirmed financial balances. A VERIFIED record count is shown separately and is not proof of ledger settlement."})}</p><div className="acReportGrid"><div>{localize("ui.7fa758e5c565e82e",{defaultValue:"Awaiting independent verification"})}<strong>{groupReport?.reportedUnverified??'—'}</strong></div><div>{localize("ui.a16cf8d0bfe56aca",{defaultValue:"Guest-reported amount (not received balance)"})}<strong>{groupReport?localize("ui.b3da1e4c465e3370",{defaultValue:"₹{{value0}}",value0:((groupReport.reportedUnverifiedPaise/100).toLocaleString(activeLocale("en-IN"),{minimumFractionDigits:2,maximumFractionDigits:2}))}):'—'}</strong></div><div>{localize("ui.d302ba79d424fac8",{defaultValue:"Records marked verified"})}<strong>{groupReport?.verifiedRecords??'—'}</strong></div><div>{localize("ui.267d0867b48d1fa7",{defaultValue:"Rejected reports"})}<strong>{groupReport?.rejectedRecords??'—'}</strong></div></div></section><section className="acPanel"><h2>{localize("ui.2c112b8042c6a9eb",{defaultValue:"Guestbook moderation overview"})}</h2><p>{localize("ui.6855f89a924db91c",{defaultValue:"Event hosts moderate messages through their own celebration pages; administrators can review platform-wide status here."})}</p><div className="acReportGrid"><div>{localize("ui.bb33a7f41817b38d",{defaultValue:"Pending approval"})}<strong>{guestbookReport?.pending??'—'}</strong></div><div>{localize("ui.87b42e40c2a290e0",{defaultValue:"Approved"})}<strong>{guestbookReport?.approved??'—'}</strong></div><div>{localize("ui.7e6fefff0f0aeb6c",{defaultValue:"Hidden"})}<strong>{guestbookReport?.hidden??'—'}</strong></div></div></section><section className="acPanel"><h2>{localize("ui.d26eb50069a23524",{defaultValue:"Notification activity"})}</h2><p>{localize("ui.2a08de0c31c613ce",{defaultValue:"Outbox records do not mean an email or SMS was delivered."})}</p><div className="acReportGrid"><div>{localize("ui.482dafaf227b419f",{defaultValue:"Manual-claim notification records"})}<strong>{notificationReport?.manualClaimOutbox??'—'}</strong></div></div></section></>}

  {tab==='payments'&&<><AdminPaymentMethods scope="PLATFORM" onChanged={()=>setPaymentsVersion(v=>v+1)}/><PaymentControls key={paymentsVersion}/></>}
  {tab==='monetization'&&<><MonetizationControls/><CommercialAdminControls/></>}
  {tab==='programs'&&<><AdminReferrals/><AdminVendors/><AdminShowcase/></>}
  {tab==='analytics'&&<><AdminGrowthResults/><GrowthFunnel/><AdminInteractions/></>}
  {tab==='policies'&&<><PlatformVerificationControls/><AccountPolicies hosts={hosts||[]} events={events||[]}/></>}
  {tab==='activity'&&<><AdminAudit revision={auditRevision} modules={modules}/><BusinessAudit/></>}
  {tab==='system'&&<><ProductionReadiness/><EmailDeliveryStatus/></>}
  {tab==='appearance'&&<BrandingAdmin/>}
  {tab==='languages'&&<LocalizationAdmin/>}
  {tab==='account'&&<><ProfilePreferences/><PreferredLanguage/><section className="acPanel"><h2>{localize("ui.b53181a4d853c390",{defaultValue:"My account"})}</h2><AccountEmailVerification/><MobileVerification/></section></>}
  </div>

  <ConfirmDialog open={!!pending} busy={busy} tone={pending?.enabled?'primary':'danger'}
   title={pending?(pending.enabled?localize('admin.confirm.onTitle',{defaultValue:'Turn on {{name}} for the platform?',name:pendingName}):localize('admin.confirm.offTitle',{defaultValue:'Turn off {{name}} for every host?',name:pendingName})):''}
   confirmLabel={localizeText(pending?.enabled?'Turn on':'Turn off')}
   onCancel={()=>setPending(null)} onConfirm={()=>void confirmPlatformToggle()}>
   <p>{pending?.enabled?localizeText('Hosts who already have host access can use it straight away. Other hosts still need access granted under Hosts & access.'):localizeText('Hosts and events lose this feature straight away. Host and event switches are kept, so turning it back on restores the same access.')}</p>
   <p>{localizeText('The change is recorded in the audit log.')}</p>
  </ConfirmDialog>
 </AdminShell>;
}
