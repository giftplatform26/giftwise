// batch-3d4d2d-compact
'use client';
/* localization:instrumented */
import {confirmAction,notify} from '../lib/confirm-dialog.mjs';
import {localize,localizeText} from '../lib/localization.mjs';
import {useLocalizationRender} from './LocalizationProvider';

// batch-3d4d2c-layout: compact galleries and reorganized controls
import {useEffect,useRef,useState,useReducer} from 'react';
import {beginAction,completeAction} from '../lib/interaction-analytics.mjs';
import './invitation-studio.css';
import {invitationHistoryReducer} from '../lib/invitation-history.mjs';
import HostInvitationUploads from './HostInvitationUploads';
import dynamic from 'next/dynamic';
const InvitationTemplateGallery=dynamic(()=>import('./InvitationTemplateGallery'),{loading:()=> <p role="status">Loading invitation templates…</p>});
const AiInvitationDesigner=dynamic(()=>import('./AiInvitationDesigner'),{loading:()=> <p role="status">Loading design assistant…</p>});
import {useExperience} from './ExperienceProvider';
import {hostApi,fetchEventQrPng} from '../lib/host-api.mjs';
import {QrCode,LayoutGrid,Shapes,Palette,Layers,ArrowLeft,ArrowRight,Download,Gift,ImagePlus,Plus,RotateCcw,Save,Trash2,Type,Upload,Move,ShieldCheck,Sparkles,MoreHorizontal} from 'lucide-react';
import {downloadInvitationPng, FONT_CHOICES} from '../lib/invitation-export.mjs';
import {BACKGROUND_CATEGORIES,filterBackgrounds} from '../lib/background-gallery.mjs';
import {listPublishedAssets,publicAssetImageUrl} from '../lib/public-gallery.mjs';
import {backgroundImageRect,normalizeBackgroundTransform,resetBackgroundTransform} from '../lib/background-position.mjs';
import {STICKERS,addSticker} from '../lib/invitation-stickers.mjs';
import {
  platformDecorationId,
  eligiblePlatformDecorations
} from '../lib/platform-decorations.mjs';
import {safeUpdateElement,validateSafeLayout,MIN_QR_SIZE,MIN_TEXT_WIDTH,MIN_TEXT_HEIGHT} from '../lib/invitation-geometry.mjs';
import {initialDesign,updateElement,addText,deleteElement,validateDesign,occasionTemplates,createOccasionDesign,resetInvitationToDefault} from '../lib/design.mjs';
type Item={id:string;type:string;text:string;x:number;y:number;w:number;h:number;fontSize:number;color:string;fontWeight:number;align:string;fontFamily?:string;stickerId?:string;src?:string};
type Design={version:number;width:number;height:number;background:string;accent:string;backgroundImage:string;backgroundOverlay?:number;occasionType?:string;backgroundTransform?:{x:number;y:number;zoom:number};elements:Item[]};
type EditorHistory={present:Design;past:Design[];future:Design[]};
export default function SharedVisualInvitationEditor({eventId:linkedEventId = "",children}:{eventId?:string;children?:import("react").ReactNode}){useLocalizationRender();
 const {mode}=useExperience();const focused=mode==='FOCUSED';const [toolPanel,setToolPanel]=useState('preview'),[fit,setFit]=useState(0.5),[eventChoices,setEventChoices]=useState<Array<{id:string;title:string}>>([]);
 const [aiOpen,setAiOpen]=useState(false),[aiAllowed,setAiAllowed]=useState(false);const aiTrigger=useRef<HTMLButtonElement>(null);const moreMenu=useRef<HTMLDetailsElement>(null);const importInput=useRef<HTMLInputElement>(null);
 function closeAi(){setAiOpen(false);requestAnimationFrame(()=>aiTrigger.current?.focus())}
 const initialDraft=useRef(false),editedDraft=useRef(false);const [eventDetails,setEventDetails]=useState<Record<string,any>>({});
 const stageRef=useRef<HTMLElement>(null);const [libraryTab,setLibraryTab]=useState('templates'),[decorationCategory,setDecorationCategory]=useState('All'),[qrDownloading,setQrDownloading]=useState(false);
 const [editBackground,setEditBackground]=useState(false);
 const [backgroundSize,setBackgroundSize]=useState<{w:number;h:number}|null>(null);
 const backgroundDrag=useRef<{sx:number;sy:number;before:Design;start:{x:number;y:number;zoom:number}}|null>(null);
 const [platformAssets,setPlatformAssets]=useState<{id:string;category:string;occasion:string;title:string;tier?:string;accessible?:boolean}[]>([]);
 const [platformError,setPlatformError]=useState('');
 const [galleryCategory,setGalleryCategory]=useState('All');const [editorState,dispatchDesign]=useReducer((state:EditorHistory,action:any)=>invitationHistoryReducer(state,action) as EditorHistory,{present:createOccasionDesign('OTHER') as Design,past:[] as Design[],future:[] as Design[]});const {present:design,past:history,future}=editorState;const [occasion,setOccasion]=useState('OTHER');const [eventTitle,setEventTitle]=useState('');const [selected,setSelected]=useState('title');const [ready,setReady]=useState(false);const [status,setStatus]=useState('Draft saved locally');const [scale,setScale]=useState(0.7);const [eventId,setEventId]=useState(linkedEventId);const [qrUrl,setQrUrl]=useState('');const drag=useRef<{id:string;sx:number;sy:number;ox:number;oy:number;ow:number;oh:number;mode:'move'|'resize';before:Design}|null>(null);
 useEffect(()=>{
  let active=true;let controller:AbortController|null=null;setAiAllowed(false);setAiOpen(false);
  async function refreshAiAccess(){controller?.abort();const current=new AbortController();controller=current;
   const id=linkedEventId||eventId;
   const path=id?`/backend/api/v1/events/${encodeURIComponent(id)}/qr/ai-design`:'/backend/api/v1/events/ai-design-options';
   try{const response=await fetch(path,{credentials:'include',cache:'no-store',signal:current.signal});if(!response.ok)throw Error();const result=await response.json();if(active&&!current.signal.aborted){const enabled=id?result.enabled===true:Array.isArray(result)&&result.length>0;setAiAllowed(enabled);if(!enabled)setAiOpen(false)}}catch{if(active&&!current.signal.aborted){setAiAllowed(false);setAiOpen(false)}}
  }
  void refreshAiAccess();const visible=()=>{if(!document.hidden)void refreshAiAccess()};window.addEventListener('focus',visible);document.addEventListener('visibilitychange',visible);
  return()=>{active=false;controller?.abort();window.removeEventListener('focus',visible);document.removeEventListener('visibilitychange',visible)};
 },[linkedEventId,eventId]);
 useEffect(()=>{try{const saved=localStorage.getItem(linkedEventId ? `gift-invitation-studio-v1-${linkedEventId}` : 'gift-invitation-studio-v1');if(saved){const parsed=JSON.parse(saved);if(validateDesign(parsed)&&validateSafeLayout(parsed)){initialDraft.current=JSON.stringify(parsed)!==JSON.stringify(initialDesign);setDesign(parsed)}}}catch{}setReady(true)},[]);
 useEffect(()=>{if(ready){try{localStorage.setItem(linkedEventId ? `gift-invitation-studio-v1-${linkedEventId}` : 'gift-invitation-studio-v1',JSON.stringify(design));setStatus('Draft saved locally')}catch{setStatus('Your browser could not save this draft. Export a copy to keep your work.')}}},[design,ready]);
 useEffect(()=>{const node=stageRef.current;if(!node)return;const resize=()=>{if(!node.clientWidth)return;const next=Math.min(.85,Math.max(.2,(node.clientWidth-64)/design.width));setFit(next);setScale(next)};const observer=new ResizeObserver(resize);observer.observe(node);resize();return()=>observer.disconnect()},[design.width,toolPanel]);
 useEffect(()=>{if(!eventId)return;let active=true;hostApi.getEvent(fetch,eventId).then(event=>{if(!active)return;setOccasion(event.occasionType||'OTHER');setEventTitle(event.title||'');setEventDetails(event);if(!initialDraft.current&&!editedDraft.current)setDesign(createOccasionDesign(event.occasionType,event) as Design)}).catch(()=>{});return()=>{active=false}},[eventId]);
 useEffect(()=>{if(!linkedEventId)hostApi.events(fetch).then(setEventChoices).catch(()=>{})},[focused,linkedEventId]);
 useEffect(()=>{
  let active=true;
  (fetch('/backend/api/v1/commerce/templates',{credentials:'include',cache:'no-store'}).then(async r=>{if(!r.ok)throw Error();return r.json()}))
   .then(items=>{if(active)setPlatformAssets(items);})
   .catch(()=>{if(active)setPlatformError('Unable to load published gallery assets');});
  return ()=>{active=false;};
 },[]);
 useEffect(()=>{
   let active=true;
   setBackgroundSize(null);
   if(!design.backgroundImage)return ()=>{active=false};
   const img=new Image();
   img.onload=()=>{if(active)setBackgroundSize({w:img.naturalWidth,h:img.naturalHeight})};
   img.onerror=()=>{if(active)setBackgroundSize(null)};
   img.src=design.backgroundImage;
   return ()=>{active=false};
 },[design.backgroundImage]);
 function backgroundChange(transform:{x:number;y:number;zoom:number}){
   commit({...design,backgroundTransform:normalizeBackgroundTransform(transform)});
 }
 function backgroundPointerDown(e:import('react').PointerEvent<HTMLDivElement>){
   if(!editBackground||!design.backgroundImage)return;
   e.preventDefault();
   const start=normalizeBackgroundTransform(design.backgroundTransform);
   backgroundDrag.current={sx:e.clientX,sy:e.clientY,before:design,start};
   e.currentTarget.setPointerCapture(e.pointerId);
 }
 function backgroundPointerMove(e:import('react').PointerEvent<HTMLDivElement>){
   const drag=backgroundDrag.current;
   if(!drag)return;
   const dx=(e.clientX-drag.sx)/scale;
   const dy=(e.clientY-drag.sy)/scale;
   let x=drag.start.x+dx;
   let y=drag.start.y+dy;
   if(backgroundSize){
     const rect=backgroundImageRect(
       backgroundSize.w,backgroundSize.h,
       design.width,design.height,
       {...drag.start,x,y}
     );
     x=rect.offsetX;
     y=rect.offsetY;
   }
   setDesign(old=>({
     ...old,
     backgroundTransform:{...drag.start,x,y}
   }));
 }
 function backgroundPointerUp(){const movement=backgroundDrag.current;if(!movement)return;backgroundDrag.current=null;editedDraft.current=true;dispatchDesign({type:'finish',before:movement.before});}
 function setDesign(next:Design|((current:Design)=>Design)){dispatchDesign({type:'preview',next})}
 function commit(next:Design){if(next===design)return;editedDraft.current=true;if(!validateSafeLayout(next)){setStatus('Change blocked: QR protected area');return}dispatchDesign({type:'commit',next})}
 function patch(id:string,p:Partial<Item>){commit(safeUpdateElement(design,id,p))}
 function insertSticker(id:string,authenticated=false){const next=addSticker(design,id,authenticated) as Design;if(next===design){setStatus('No QR-safe space for this decoration');return}commit(next);setSelected(next.elements[next.elements.length-1].id)}
 function finishDrag(){const movement=drag.current;drag.current=null;if(!movement)return;editedDraft.current=true;dispatchDesign({type:'finish',before:movement.before})}
 function undo(){editedDraft.current=true;dispatchDesign({type:'undo'})}
 function redo(){editedDraft.current=true;dispatchDesign({type:'redo'})}
 useEffect(()=>{function shortcut(event:KeyboardEvent){const target=event.target as HTMLElement|null;if(target?.closest('input,textarea,[contenteditable=true]')||!(event.metaKey||event.ctrlKey)||event.altKey)return;const key=event.key.toLowerCase();if(key==='z'){if(event.shiftKey?future.length:history.length){event.preventDefault();editedDraft.current=true;dispatchDesign({type:event.shiftKey?'redo':'undo'})}}else if(key==='y'&&future.length){event.preventDefault();editedDraft.current=true;dispatchDesign({type:'redo'})}}window.addEventListener('keydown',shortcut);return()=>window.removeEventListener('keydown',shortcut)},[history.length,future.length]);
 useEffect(()=>{if(!design.elements.some(element=>element.id===selected))setSelected('title');const title=design.elements.find(element=>element.id==='title')?.text||'';setEventTitle(title);setOccasion(design.occasionType||eventDetails.occasionType||'OTHER')},[design,selected,eventDetails.occasionType]);
 async function resetDefault(){if(!await confirmAction('Reset the invitation design to its default template? This will discard your current visual changes, but will not change event details, gifts, or the event QR.',{confirmLabel:'Reset design',danger:true}))return;commit(resetInvitationToDefault(occasion,{...eventDetails,title:eventTitle}) as Design);setSelected('title')}
 function applyTemplate(nextOccasion:string){setOccasion(nextOccasion);setGalleryCategory('All');commit(createOccasionDesign(nextOccasion,{...eventDetails,title:eventTitle}) as Design);setSelected('title')}
 function exportDraft(){const blob=new Blob([JSON.stringify(design,null,2)],{type:'application/json'});const url=URL.createObjectURL(blob);const a=document.createElement('a');a.href=url;a.download='gift-invitation-draft.json';a.click();URL.revokeObjectURL(url)}
 function importDraft(file?:File){if(!file||file.size>2*1024*1024)return;file.text().then(s=>{try{const parsed=JSON.parse(s);if(!validateDesign(parsed)||!validateSafeLayout(parsed))throw Error('Invalid or overlapping design');commit(parsed)}catch{void notify('Invalid invitation draft')}})}
 async function loadQr(id=eventId){if(!/^[0-9a-f]{8}-[0-9a-f-]{27,}$/i.test(id)){void notify('Enter a valid event UUID');return}try{const r=await fetch(`/backend/api/v1/events/${encodeURIComponent(id)}/qr/png`,{credentials:'include'});if(!r.ok)throw Error(`HTTP ${r.status}: login to backend may be required`);const b=await r.blob();if(b.type!=='image/png')throw Error('Unexpected QR response');const url=URL.createObjectURL(b);setQrUrl(old=>{if(old)URL.revokeObjectURL(old);return url});setStatus('Real event QR loaded')}catch{void notify('Could not load the event QR. Please sign in again and retry.')}}
 
useEffect(()=>{
  setQrUrl('');
  if(!eventId)return;
  let active=true;
  let objectUrl='';
  fetch(
    `/backend/api/v1/events/${encodeURIComponent(eventId)}/qr/png`,
    {
      credentials:'include',
      cache:'no-store',
      headers:{Accept:'image/png'}
    }
  )
  .then(async response=>{
    if(!response.ok)throw Error(`HTTP ${response.status}`);
    const blob=await response.blob();
    if(blob.type!=='image/png')throw Error('Invalid QR image');
    if(!active)return;
    objectUrl=URL.createObjectURL(blob);
    setQrUrl(objectUrl);
    setStatus('Authenticated event QR loaded');
  })
  .catch(()=>{
    if(active)setStatus('QR unavailable — sign in and retry');
  });
  return ()=>{
    active=false;
    if(objectUrl)URL.revokeObjectURL(objectUrl);
  };
},[eventId]);
async function downloadStandaloneQr(){
 if(!eventId||qrDownloading)return;const interaction=beginAction('QR_DOWNLOAD',{eventId});setQrDownloading(true);
 try{const blob=await fetchEventQrPng(fetch,eventId);const url=URL.createObjectURL(blob);try{const a=document.createElement('a');a.href=url;a.download=`giftwise-qr-${eventId}.png`;document.body.appendChild(a);a.click();a.remove();completeAction(interaction,'BROWSER_READY','PNG_HANDED_TO_BROWSER');setStatus('QR image downloaded')}finally{setTimeout(()=>URL.revokeObjectURL(url),1500)}}catch{completeAction(interaction,'FAILURE','QR_DOWNLOAD_FAILED');setStatus('Could not download the QR image. Please retry.')}finally{setQrDownloading(false)}
}
async function downloadArtwork(){
      if(!qrUrl){
        setStatus('Load the authenticated QR before downloading');
        return;
      }
      try{
        setStatus('Rendering invitation PNG...');
        await downloadInvitationPng(
          design,
          qrUrl,
          `gift-invitation-${linkedEventId||'draft'}.png`
        );
        setStatus('Invitation PNG downloaded');
      }catch(err){
        setStatus(
          err instanceof Error ? err.message : 'Invitation download failed'
        );
      }
    }
    const active=design.elements.find(e=>e.id===selected);const hasRealQr=Boolean(qrUrl);
 return <main className="app invitationEditor" data-library-tab={libraryTab} data-event-id={eventId||undefined} data-editor-panel={toolPanel}>
 <header className="studioHeader">
  <div className="studioHeading"><div className="brand"><span className="brandIcon"><Gift size={22}/></span><div><strong>{localizeText('GiftWise Invitation Studio')}</strong><small>{localizeText('Beautiful moments deserve beautiful invitations')}</small></div></div><span className="studioSaveStatus"><Save size={14}/>{status}</span></div>
  <div className="studioToolbar" role="toolbar" aria-label={localize("ui.1a2f75626ab3a51f",{defaultValue:"Invitation design actions"})}>
   <div className="studioMainActions"><button type="button" className="studioQrDownload" data-track-action="QR_DOWNLOAD" disabled={!eventId||qrDownloading} onClick={()=>void downloadStandaloneQr()}><QrCode size={17}/>{localizeText(qrDownloading?'Preparing QR…':'Download QR')}</button>
    {aiAllowed&&<button type="button" ref={aiTrigger} aria-haspopup="dialog" className="studioAiButton" onClick={()=>setAiOpen(true)}><Sparkles size={17}/> {localize("ui.920bf770754f42cf",{defaultValue:" Design with AI"})}</button>}
    <button type="button" onClick={()=>void downloadArtwork()} disabled={!hasRealQr} className="studioDownloadButton" title={hasRealQr?localize("ui.5fa0853872e2238f",{defaultValue:"Download full invitation as PNG"}):localize("ui.63f71a3b35db60b8",{defaultValue:"Load the real event QR first"})}><Download size={17}/> {localizeText('Download invitation')}</button>
   </div>
   <div className="studioEditActions"><div className="studioHistory" role="group" aria-label={localize("ui.114df1c0a533756c",{defaultValue:"Design history"})}><button type="button" onClick={undo} disabled={!history.length} title={localize("ui.a8283ade31856f71",{defaultValue:"Undo"})} aria-label={localize("ui.a8283ade31856f71",{defaultValue:"Undo"})}><ArrowLeft size={18}/></button><button type="button" onClick={redo} disabled={!future.length} title={localize("ui.74273989b0966e23",{defaultValue:"Redo"})} aria-label={localize("ui.74273989b0966e23",{defaultValue:"Redo"})}><ArrowRight size={18}/></button></div>
    <details ref={moreMenu} className="studioMore" onKeyDown={e=>{if(e.key==='Escape'){e.currentTarget.open=false;e.currentTarget.querySelector('summary')?.focus()}}}>
     <summary><MoreHorizontal size={18}/> {localize("ui.c50e340ff78a786c",{defaultValue:" More"})}</summary>
     <div className="studioMoreMenu">
      <button type="button" className="studioImportAction" onClick={()=>importInput.current?.click()}><Upload size={16}/> {localize("ui.bfe3fd0809ff3230",{defaultValue:" Import draft"})}</button><input ref={importInput} type="file" accept="application/json,.json" hidden onChange={e=>{importDraft(e.target.files?.[0]);e.target.value='';if(moreMenu.current)moreMenu.current.open=false}}/>
      <button type="button" onClick={()=>{exportDraft();if(moreMenu.current)moreMenu.current.open=false}}><Download size={16}/> {localize("ui.791a355f853121f3",{defaultValue:" Export draft"})}</button>
      <button type="button" onClick={()=>{if(moreMenu.current)moreMenu.current.open=false;resetDefault()}}><RotateCcw size={16}/> {localize("ui.d1f59ae5b10e2c87",{defaultValue:" Reset invitation to default"})}</button>
     </div>
    </details>
   </div>
  </div>
 </header>{!linkedEventId&&<section className="studioEventPicker"><label>{localizeText('Choose your celebration for an instant invitation')}<select value={eventId} onChange={async e=>{const value=e.target.value;if(editedDraft.current&&!await confirmAction('Replace the current design with an invitation for this celebration?',{confirmLabel:'Replace design'}))return;initialDraft.current=false;editedDraft.current=false;dispatchDesign({type:'clear'});setEventId(value)}}><option value="">{localizeText('Choose a celebration')}</option>{eventChoices.map(event=><option key={event.id} value={event.id}>{event.title}</option>)}</select></label></section>}<nav className="studioMobileTabs" aria-label={localize("ui.ad7e4b54fbe13d29",{defaultValue:"Invitation tools"})}>{[['preview','Preview'],['templates','Templates & design'],['properties','Selected element']].map(([value,label])=><button key={value} type="button" aria-pressed={toolPanel===value} onClick={()=>setToolPanel(value)}>{localizeText(label)}</button>)}</nav>{aiOpen&&aiAllowed&&<AiInvitationDesigner eventId={linkedEventId||eventId} onClose={closeAi} onApply={(next,suggestion)=>{commit(next as Design);setOccasion(suggestion.occasionType);setEventTitle(suggestion.eventTitle);setSelected('title');setToolPanel('preview');if(!linkedEventId&&eventId!==suggestion.eventId){setEventId(suggestion.eventId);setQrUrl(old=>{if(old)URL.revokeObjectURL(old);return ''})}void loadQr(suggestion.eventId)}}/>}<div className="workspace"><aside className="sidebar"><div className="studioLibraryIntro"><span className="studioEyebrow">{localizeText('YOUR CREATIVE CORNER')}</span><h2>{localizeText('Make it yours')}</h2><p>{localize("ui.d056977a3b09b634",{defaultValue:"Every detail, your way."})}</p></div><nav className="studioLibraryNav" aria-label={localizeText('Design library')}>{[{id:'templates',label:'Templates',Icon:LayoutGrid},{id:'decorations',label:'Decorate',Icon:Shapes},{id:'backgrounds',label:'Backdrop',Icon:Palette},{id:'details',label:'Details',Icon:Type},{id:'layers',label:'Layers',Icon:Layers}].map(({id,label,Icon})=><button key={id} type="button" aria-pressed={libraryTab===id} onClick={()=>setLibraryTab(id)}><Icon size={19}/><span>{localizeText(label)}</span></button>)}</nav><InvitationTemplateGallery occasion={occasion} details={{...eventDetails,title:eventTitle}} qrUrl={qrUrl} onApply={next=>{commit(next as Design);setSelected('title');setToolPanel('preview')}}/><div className="section studioToolGroup" data-studio-tool="details"><h3>{localize("ui.abc2a1a18863d803",{defaultValue:"Event template"})}</h3><label>{localize("ui.f68e0b1ef1269bae",{defaultValue:"Occasion"})}<select value={occasion} onChange={e=>applyTemplate(e.target.value)}>{Object.entries(occasionTemplates).map(([key,t])=><option key={key} value={key}>{t.label}</option>)}</select></label><label>{localize("ui.25bc9efedfb71cbf",{defaultValue:"Event name"})}<input value={eventTitle} maxLength={200} placeholder={localize("ui.115ee2f858ce066a",{defaultValue:"Your event name"})} onChange={e=>{setEventTitle(e.target.value);commit(safeUpdateElement(design,'title',{text:e.target.value||occasionTemplates[occasion as keyof typeof occasionTemplates]?.title||'Celebrate With Us'}))}}/></label></div><div className="section studioToolGroup" data-studio-tool="layers"><h3>{localize("ui.e1ff7a9061031102",{defaultValue:"Elements"})}</h3><button className="wide" onClick={()=>{const next=addText(design);commit(next);setSelected(next.elements.at(-1)!.id)}}><Plus size={17}/> {localize("ui.3931578de88dbe3f",{defaultValue:" Add text or tagline"})}</button><div className="studioElementScroll" role="region" aria-label={localize("ui.8ba28db800ef5155",{defaultValue:"Invitation elements"})}>{design.elements.map(e=><button key={e.id} className={`layer ${selected===e.id?'chosen':''}`} onClick={()=>setSelected(e.id)}>{e.type==='qr'?<Gift size={16}/>:<Type size={16}/>}<span>{e.type==='qr'?localize("ui.2bf360710086819b",{defaultValue:"Event QR"}):e.type==='sticker'?(STICKERS.find(s=>s.id===e.stickerId)?.name||localize("ui.7b39a9fd07f83d61",{defaultValue:"Decoration"})):e.text.slice(0,26)}</span></button>)}</div></div>
<div className="section studioToolGroup" data-studio-tool="decorations">
  <h3>{localize("ui.6ecd8a6b759c252e",{defaultValue:"Free decorations"})}</h3>
  <p>{localize("ui.1220d9c4780b8249",{defaultValue:"Add a decoration, then drag or resize it. QR overlap is blocked."})}</p>
  <HostInvitationUploads category="DECORATION" onUse={asset=>insertSticker(`upload:${asset.id}`)}/><div className="studioCategoryChips">{['All','Floral','Celebration','Love','Botanical','Frames'].map(category=><button key={category} type="button" aria-pressed={decorationCategory===category} onClick={()=>setDecorationCategory(category)}>{localizeText(category)}</button>)}</div><div className="studioDecorationGrid">
    {STICKERS.filter(sticker=>decorationCategory==='All'||sticker.category===decorationCategory).map(sticker=><button type="button" key={sticker.id} title={localize("ui.ddcadea790b77482",{defaultValue:"Add {{value0}}",value0:(sticker.name)})} onClick={()=>insertSticker(sticker.id)} style={{padding:6,border:'1px solid #ddd',borderRadius:8,background:'#fff',cursor:'pointer'}}>
      <img src={sticker.src} alt="" style={{width:'100%',height:58,objectFit:'contain'}}/>
      <span style={{display:'block',fontSize:11}}>{localizeText(sticker.name)}</span>
    </button>)}
  </div>
</div>

<div className="section studioToolGroup" data-studio-tool="decorations">
 <h3>{localize("ui.baf8e5b26157bfc2",{defaultValue:"Platform Decorations"})}</h3>
  <p>{localize("ui.df01eb051b5d7949",{defaultValue:"Published decorations for this occasion."})}</p>
  <div style={{
    display:'grid',
    gridAutoFlow:'column',
    gridAutoColumns:'110px',
    gap:8,
    overflowX:'auto',
    paddingBottom:10
  }}>
    {eligiblePlatformDecorations(
      platformAssets.map(a=>({...a,published:true})),
      occasion
    ).map(a=>{
      const src=`/backend/api/v1/commerce/templates/${a.id}/image`;
    if(a.accessible===false)return <a key={a.id} href="/host/monetization" style={{padding:12,border:'1px solid #ddd',borderRadius:8}}>{a.title} {localize("ui.bdacc894d3dc6dff",{defaultValue:" · Premium · Unlock →"})}</a>;
      return <button
        key={a.id}
        type="button"
        title={localize("ui.ddcadea790b77482",{defaultValue:"Add {{value0}}",value0:(a.title)})}
        onClick={()=>insertSticker(platformDecorationId(a.id),true)}
        style={{
          padding:6,
          border:'1px solid #ccc',
          borderRadius:8,
          background:'#fff',
          cursor:'pointer'
        }}>
        <img src={src} alt="" style={{
          width:'100%',
          height:70,
          objectFit:'contain'
        }}/>
        <span style={{display:'block',fontSize:11}}>
          {a.title}
        </span>
      </button>;
    })}
  </div>
 </div>

 <div className="section studioToolGroup" data-studio-tool="backgrounds">
  <h3>{localize("ui.9dfb219725f2ae8f",{defaultValue:"Platform Backgrounds"})}</h3>
 <p>{localize("ui.456e5ab5ef656360",{defaultValue:"Published backgrounds from the administrator gallery. "})}<a href="/host/monetization">{localize("ui.a267268d8ab20301",{defaultValue:"Browse premium invitations and plans →"})}</a></p>
 {platformError&&<p role="alert">{platformError}</p>}
 <div style={{
  display:'grid',
  gridAutoFlow:'column',
  gridAutoColumns:'132px',
  gap:8,
  overflowX:'auto',
  paddingBottom:10
 }}>
  {platformAssets
   .filter(a=>a.category==='BACKGROUND'&&
     (a.occasion===occasion||a.occasion==='OTHER'))
   .map(a=>{
    const src=`/backend/api/v1/commerce/templates/${a.id}/image`;
    if(a.accessible===false)return <a key={a.id} href="/host/monetization" style={{padding:12,border:'1px solid #ddd',borderRadius:8}}>{a.title} {localize("ui.bdacc894d3dc6dff",{defaultValue:" · Premium · Unlock →"})}</a>;
    return <button
     key={a.id}
     type="button"
     title={localize("ui.139165035ef11646",{defaultValue:"Apply {{value0}}",value0:(a.title)})}
     onClick={()=>commit({...design,backgroundImage:src,backgroundOverlay:0})}
     style={{
      padding:6,
      border:design.backgroundImage===src
       ?'2px solid #80538e'
       :'1px solid #ccc',
      borderRadius:8,
      background:'#fff',
      cursor:'pointer'
     }}>
      <img
       src={src}
       alt={a.title}
       style={{
        width:'100%',
        aspectRatio:'3 / 4',
        objectFit:'cover',
        borderRadius:4
       }}
      />
      <span style={{display:'block',fontSize:12}}>{a.title}</span>
    </button>;
   })}
 </div>
</div>

<div className="section studioToolGroup" data-studio-tool="backgrounds">
  <h3>{localize("ui.a7c68f3ad95fd65f",{defaultValue:"Background Gallery"})}</h3>
  <p>{localize("ui.8d7730ae01d7835a",{defaultValue:"Select a Free background without changing your text or QR."})}</p>

  <label>{localize("ui.7b790708ff1b1713",{defaultValue:"Collection"})}<select
      value={galleryCategory}
      onChange={ev=>setGalleryCategory(ev.target.value)}
    >
      {BACKGROUND_CATEGORIES.map(c=>
        <option key={c} value={c}>{c}</option>
      )}
    </select>
  </label>

  <div style={{
    display:'grid',
    gridAutoFlow:'column',
    gridAutoColumns:'132px',
    gridTemplateRows:'1fr',
    gap:8,
    overflowX:'auto',
    overflowY:'hidden',
    paddingBottom:10
  }}>
    {filterBackgrounds(occasion,galleryCategory).map(b=>
      <button
        key={b.id}
        type="button"
        title={localize("ui.139165035ef11646",{defaultValue:"Apply {{value0}}",value0:(b.name)})}
        onClick={()=>commit({...design,backgroundImage:b.src,backgroundOverlay:0})}
        style={{
          padding:6,
          border:design.backgroundImage===b.src
            ?'2px solid #80538e'
            :'1px solid #ccc',
          borderRadius:8,
          background:'#fff',
          cursor:'pointer'
        }}
      >
        <img
          src={b.src}
          alt={localize("ui.ade99fac7237fbf9",{defaultValue:"{{value0}} invitation background",value0:(b.name)})}
          style={{
            width:'100%',
            aspectRatio:'3 / 4',
            objectFit:'cover',
            borderRadius:4
          }}
        />
        <span style={{display:'block',fontSize:12}}>
          {b.name} {localize("ui.1841ac188debd8c2",{defaultValue:" · Free"})}</span>
      </button>
    )}
  </div>

  <small>{localizeText('Explore the full background collection. Designs for your occasion appear first.')}</small>
</div>
<p className="studioLibraryFoot">{localizeText('A few thoughtful details. A celebration that feels like you.')}</p></aside><section className="stage" ref={stageRef}><div className="stageHeader"><span>{localize("ui.e3020b3a43d8de27",{defaultValue:"Live invitation preview"})}</span><div><label>{localize("ui.e3ce6241ad144b88",{defaultValue:"Zoom "})}<select value={scale} onChange={e=>setScale(Number(e.target.value))}>{<option value={fit}>{localize("ui.32bb0d298ca1545c",{defaultValue:"Fit to screen"})}</option>}<option value="0.5">50%</option><option value="0.7">70%</option><option value="1">100%</option></select></label></div></div><div className="canvasViewport"><div className="canvasFrame" style={{width:design.width*scale,height:design.height*scale}}><div className="canvas" style={{width:design.width,height:design.height,transform:`scale(${scale})`,backgroundColor:design.background,backgroundImage:design.backgroundImage?`linear-gradient(rgba(255,255,255,${design.backgroundOverlay??.53}),rgba(255,255,255,${design.backgroundOverlay??.53})),url("${design.backgroundImage}")`:undefined,
    backgroundSize:design.backgroundImage&&backgroundSize
      ?(()=>{const r=backgroundImageRect(backgroundSize.w,backgroundSize.h,design.width,design.height,design.backgroundTransform);return `100% 100%, ${r.width}px ${r.height}px`})()
      :undefined,
    backgroundPosition:design.backgroundImage&&backgroundSize
      ?(()=>{const r=backgroundImageRect(backgroundSize.w,backgroundSize.h,design.width,design.height,design.backgroundTransform);return `center, ${r.x}px ${r.y}px`})()
      :undefined,
    backgroundRepeat:'no-repeat',
    cursor:editBackground?'grab':undefined,
    touchAction:editBackground?'none':undefined}} onPointerMove={e=>{if(backgroundDrag.current){backgroundPointerMove(e);return}if(!drag.current)return;const d=drag.current;const dx=Math.round((e.clientX-d.sx)/scale);const dy=Math.round((e.clientY-d.sy)/scale);const patch=d.mode==='resize'?{w:Math.max(d.id==='qr'?MIN_QR_SIZE:MIN_TEXT_WIDTH,d.ow+dx),h:Math.max(d.id==='qr'?MIN_QR_SIZE:MIN_TEXT_HEIGHT,d.oh+dy)}:{x:d.ox+dx,y:d.oy+dy};setDesign(old=>safeUpdateElement(old,d.id,patch))}} onPointerDown={backgroundPointerDown} onPointerUp={()=>{backgroundPointerUp();finishDrag()}} onPointerCancel={()=>{backgroundPointerUp();finishDrag()}}>{design.elements.map(el=><div key={el.id} onPointerDown={e=>{if(editBackground)return;if(el.type==='qr'||el.type==='text'||el.type==='sticker'){setSelected(el.id);drag.current={id:el.id,sx:e.clientX,sy:e.clientY,ox:el.x,oy:el.y,ow:el.w,oh:el.h,mode:'move',before:design};e.currentTarget.setPointerCapture(e.pointerId)}}} className={`canvasElement ${selected===el.id?'active':''}`} style={{left:el.x,top:el.y,width:el.w,height:el.h,fontFamily:el.fontFamily||"Georgia, serif",fontSize:el.fontSize,color:el.color,fontWeight:el.fontWeight,textAlign:el.align as 'left'|'center'|'right',touchAction:'none'}}>{el.type==='sticker'?<img src={el.src} alt={STICKERS.find(s=>s.id===el.stickerId)?.name||localize("ui.7b39a9fd07f83d61",{defaultValue:"Decoration"})} draggable={false} style={{width:'100%',height:'100%',objectFit:'contain',pointerEvents:'none'}}/>:el.type==='qr'?<div className="qrSlot">{hasRealQr?<img src={qrUrl} alt={localize("ui.2bf360710086819b",{defaultValue:"Event QR"})}/>:<><Gift size={44}/><small>{localize("ui.dced4f1bec4c2463",{defaultValue:"QR PREVIEW"})}</small><small>{localize("ui.bb9ea43767f15123",{defaultValue:"Not scannable"})}</small></>}</div>:el.text}{selected===el.id&&<div title={localize("ui.31b2f1cdb2e93ebb",{defaultValue:"Drag to resize"})} aria-label={localize("ui.d9ec9da693f451ea",{defaultValue:"Resize {{value0}}",value0:(el.id)})} onPointerDown={e=>{e.stopPropagation();drag.current={id:el.id,sx:e.clientX,sy:e.clientY,ox:el.x,oy:el.y,ow:el.w,oh:el.h,mode:'resize',before:design};e.currentTarget.setPointerCapture(e.pointerId)}} onPointerMove={e=>{if(!drag.current||drag.current.mode!=='resize')return;e.stopPropagation();const d=drag.current;const dx=Math.round((e.clientX-d.sx)/scale);const dy=Math.round((e.clientY-d.sy)/scale);setDesign(old=>safeUpdateElement(old,d.id,{w:Math.max(el.type==='qr'?MIN_QR_SIZE:MIN_TEXT_WIDTH,d.ow+dx),h:Math.max(el.type==='qr'?MIN_QR_SIZE:MIN_TEXT_HEIGHT,d.oh+dy)}))}} onPointerUp={e=>{e.stopPropagation();finishDrag()}} onPointerCancel={e=>{e.stopPropagation();finishDrag()}} style={{position:'absolute',right:-7,bottom:-7,width:16,height:16,background:'#80538e',border:'2px solid white',borderRadius:4,cursor:'nwse-resize',touchAction:'none',zIndex:10}}/>}</div>)}</div></div></div><div className="hint"><Move size={15}/> {localize("ui.0b35e931c2270cdc",{defaultValue:" Drag elements on the invitation. Use the right panel for precise sizing and text."})}</div>{children && <div className="qrBelowPreview">{children}</div>}</section><aside className="inspector"><h2>{localize("ui.ae43692b2a310b8e",{defaultValue:"Properties"})}</h2>{active?<div className="section"><h3>{active.type==='qr'?localize("ui.eeac798826ab5606",{defaultValue:"QR element"}):active.type==='sticker'?localize("ui.7b39a9fd07f83d61",{defaultValue:"Decoration"}):localize("ui.0e5a638475def659",{defaultValue:"Text element"})}</h3>{active.type==='text'&&<><label>{localize("ui.2f77668a9dfbf8d5",{defaultValue:"Message"})}<textarea rows={4} value={active.text} onChange={e=>patch(active.id,{text:e.target.value})}/></label><label>{localize("ui.4a69d01660468781",{defaultValue:"Text color"})}<input type="color" value={active.color} onChange={e=>patch(active.id,{color:e.target.value})}/></label><label>{localize("ui.bf9a32d3cac90bed",{defaultValue:"Font style"})}<select
        value={active.fontFamily||"Georgia, serif"}
        onChange={e=>patch(active.id,{fontFamily:e.target.value})}
      >
        {FONT_CHOICES.map(f=>
          <option key={f.value} value={f.value}>{f.label}</option>
        )}
      </select>
    </label>
    <label>{localize("ui.6d784d95c63833e6",{defaultValue:"Font size"})}<input type="range" min="12" max="70" value={active.fontSize} onChange={e=>patch(active.id,{fontSize:Number(e.target.value)})}/><span>{active.fontSize}{localize("ui.6ee2cc105a677431",{defaultValue:"px"})}</span></label><label>{localize("ui.9d597870c4b3f76d",{defaultValue:"Alignment"})}<select value={active.align} onChange={e=>patch(active.id,{align:e.target.value})}><option value="left">{localize("ui.58eb9032e3bb83f0",{defaultValue:"Left"})}</option><option value="center">{localize("ui.d946067427e930a1",{defaultValue:"Center"})}</option><option value="right">{localize("ui.883361d5d682a157",{defaultValue:"Right"})}</option></select></label></>}<div className="studioCoordinates">{['x','y','w','h'].map(k=><label key={k}>{localizeText(({x:'Left',y:'Top',w:'Width',h:'Height'} as Record<string,string>)[k])}<input type="number" value={active[k as 'x'|'y'|'w'|'h']} onChange={e=>patch(active.id,{[k]:Number(e.target.value)})}/></label>)}</div>{active.type!=='qr'&&<button className="danger" onClick={()=>{commit(deleteElement(design,active.id));setSelected('qr')}}><Trash2 size={16}/> {localize("ui.246a4e073a502e68",{defaultValue:" Delete element"})}</button>}</div>:<p>{localize("ui.a1b9f46af2fe9cf2",{defaultValue:"Select an element."})}</p>}<div className="section"><h3>{localize("ui.4358be6ef6bdcbcd",{defaultValue:"Background position"})}</h3>
      <button type="button" className="wide" onClick={()=>setEditBackground(v=>!v)}>
        {editBackground?localize("ui.3f318e4e1654bf8d",{defaultValue:"Finish positioning"}):localize("ui.e16337fe95686a20",{defaultValue:"Edit background — drag to position"})}
      </button>
      <label>{localize("ui.e7d1ff81125efbcb",{defaultValue:"Background zoom: "})}{Math.round(normalizeBackgroundTransform(design.backgroundTransform).zoom*100)}%
        <input type="range" min="100" max="300" step="5"
          value={Math.round(normalizeBackgroundTransform(design.backgroundTransform).zoom*100)}
          onChange={e=>backgroundChange({
            ...normalizeBackgroundTransform(design.backgroundTransform),
            zoom:Number(e.target.value)/100
          })}/>
      </label>
      <button type="button" className="wide"
        onClick={()=>backgroundChange(resetBackgroundTransform())}>
        {localize("ui.11385dfe21d5ef61",{defaultValue:"Reset background position"})}</button>
      <small>{localize("ui.f0b5d4763eddf94a",{defaultValue:"Enable Edit background, then drag the invitation image. Text and QR remain fixed."})}</small>
    </div><div className="section"><label>{localizeText('Soften the background')}<input type="range" min="0" max="100" value={Math.round((design.backgroundOverlay??.53)*100)} onChange={e=>commit({...design,backgroundOverlay:Number(e.target.value)/100})}/></label><h3>{localize("ui.ea2b8a878841df59",{defaultValue:"Background"})}</h3><HostInvitationUploads category="BACKGROUND" onUse={asset=>commit({...design,backgroundImage:asset.src,backgroundOverlay:0})}/><label>{localize("ui.3e314dafdf7c6c51",{defaultValue:"Background color"})}<input type="color" value={design.background} onChange={e=>commit({...design,background:e.target.value})}/></label><button className="wide" onClick={()=>commit({...design,backgroundImage:''})}><RotateCcw size={16}/> {localize("ui.e0134396e5e148fb",{defaultValue:" Remove background photo"})}</button></div><div className="section"><h3>{localize("ui.2d6b6233a643297c",{defaultValue:"Real event QR"})}</h3>{!linkedEventId&&(focused?<label>{localize("ui.4a8b403035a2b673",{defaultValue:"Celebration"})}<select value={eventId} onChange={e=>setEventId(e.target.value)}><option value="">{localize("ui.fc832ad901abec7d",{defaultValue:"Choose a celebration"})}</option>{eventChoices.map(e=><option key={e.id} value={e.id}>{e.title}</option>)}</select></label>:<input placeholder={localize("ui.f9f384308278b4e6",{defaultValue:"Event UUID"})} value={eventId} onChange={e=>setEventId(e.target.value)}/>)}<button className="wide" onClick={()=>void loadQr()}><ShieldCheck size={16}/> {localize("ui.c3f50438f104955b",{defaultValue:" Load authenticated QR"})}</button><small>{localize("ui.31b26e52566030cc",{defaultValue:"The preview placeholder is NOT scannable. A real QR is required for final artwork."})}</small></div><div className="notice"><ShieldCheck size={18}/><p>{focused?localize("ui.477293b219312aee",{defaultValue:"Your design stays in this browser. Download the finished invitation to share it."}):localize("ui.de36de437f87f363",{defaultValue:"Drafts are stored in this browser only. Server-side publishing and scan validation are planned for the next integration batch."})}</p></div></aside></div></main>
}
