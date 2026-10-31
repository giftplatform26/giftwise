// batch-3d4d2d-compact
'use client';
// batch-3d4d2c-layout: compact galleries and reorganized controls
import {useEffect,useRef,useState} from 'react';
import AiInvitationDesigner from './AiInvitationDesigner';
import {useExperience} from './ExperienceProvider';
import {hostApi} from '../lib/host-api.mjs';
import {ArrowLeft,ArrowRight,Download,Gift,ImagePlus,Plus,RotateCcw,Save,Trash2,Type,Upload,Move,ShieldCheck,Sparkles,MoreHorizontal} from 'lucide-react';
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
type Design={version:number;width:number;height:number;background:string;accent:string;backgroundImage:string;backgroundTransform?:{x:number;y:number;zoom:number};elements:Item[]};
export default function SharedVisualInvitationEditor({eventId:linkedEventId = "",children}:{eventId?:string;children?:import("react").ReactNode}){
 const {mode}=useExperience();const focused=mode==='FOCUSED';const [toolPanel,setToolPanel]=useState('preview'),[fit,setFit]=useState(0.5),[eventChoices,setEventChoices]=useState<Array<{id:string;title:string}>>([]);
 const [aiOpen,setAiOpen]=useState(false);const aiTrigger=useRef<HTMLButtonElement>(null);const moreMenu=useRef<HTMLDetailsElement>(null);const importInput=useRef<HTMLInputElement>(null);
 function closeAi(){setAiOpen(false);requestAnimationFrame(()=>aiTrigger.current?.focus())}
 const initialDraft=useRef(false),editedDraft=useRef(false);
 const [editBackground,setEditBackground]=useState(false);
 const [backgroundSize,setBackgroundSize]=useState<{w:number;h:number}|null>(null);
 const backgroundDrag=useRef<{sx:number;sy:number;before:Design;start:{x:number;y:number;zoom:number}}|null>(null);
 const [platformAssets,setPlatformAssets]=useState<{id:string;category:string;occasion:string;title:string;tier?:string;accessible?:boolean}[]>([]);
 const [platformError,setPlatformError]=useState('');
 const [galleryCategory,setGalleryCategory]=useState('All');const [design,setDesign]=useState<Design>(initialDesign as Design);const [occasion,setOccasion]=useState('OTHER');const [eventTitle,setEventTitle]=useState('');const [selected,setSelected]=useState('title');const [history,setHistory]=useState<Design[]>([]);const [future,setFuture]=useState<Design[]>([]);const [ready,setReady]=useState(false);const [status,setStatus]=useState('Draft saved locally');const [scale,setScale]=useState(0.7);const [eventId,setEventId]=useState(linkedEventId);const [qrUrl,setQrUrl]=useState('');const drag=useRef<{id:string;sx:number;sy:number;ox:number;oy:number;ow:number;oh:number;mode:'move'|'resize';before:Design}|null>(null);
 useEffect(()=>{try{const saved=localStorage.getItem(linkedEventId ? `gift-invitation-studio-v1-${linkedEventId}` : 'gift-invitation-studio-v1');if(saved){const parsed=JSON.parse(saved);if(validateDesign(parsed)&&validateSafeLayout(parsed)){initialDraft.current=JSON.stringify(parsed)!==JSON.stringify(initialDesign);setDesign(parsed)}}}catch{}setReady(true)},[]);
 useEffect(()=>{if(ready){try{localStorage.setItem(linkedEventId ? `gift-invitation-studio-v1-${linkedEventId}` : 'gift-invitation-studio-v1',JSON.stringify(design));setStatus('Draft saved locally')}catch{setStatus('Your browser could not save this draft. Export a copy to keep your work.')}}},[design,ready]);
 useEffect(()=>{if(!focused)return;const resize=()=>{const next=Math.min(.7,Math.max(.2,(window.innerWidth-80)/design.width));setFit(next);if(window.innerWidth<=760)setScale(next)};resize();window.addEventListener('resize',resize);return()=>window.removeEventListener('resize',resize)},[focused,design.width]);
 useEffect(()=>{if(!eventId)return;let active=true;hostApi.getEvent(fetch,eventId).then(event=>{if(!active)return;setOccasion(event.occasionType||'OTHER');setEventTitle(event.title||'');if(!initialDraft.current&&!editedDraft.current)setDesign(createOccasionDesign(event.occasionType,{title:event.title}) as Design)}).catch(()=>{});return()=>{active=false}},[eventId]);
 useEffect(()=>{if(focused&&!linkedEventId)hostApi.events(fetch).then(setEventChoices).catch(()=>{})},[focused,linkedEventId]);
 useEffect(()=>{
  let active=true;
  (linkedEventId?fetch('/backend/api/v1/commerce/templates',{credentials:'include',cache:'no-store'}).then(async r=>{if(!r.ok)throw Error();return r.json()}):listPublishedAssets(fetch))
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
 function backgroundPointerUp(){
   const drag=backgroundDrag.current;
   if(!drag)return;
   backgroundDrag.current=null;
   setDesign(current=>{
     if(JSON.stringify(current.backgroundTransform)!==
        JSON.stringify(drag.before.backgroundTransform)){
       setHistory(h=>[...h.slice(-49),drag.before]);
       setFuture([]);
     }
     return current;
   });
 }
 function commit(next:Design){if(next===design)return;editedDraft.current=true;if(!validateSafeLayout(next)){setStatus('Change blocked: QR protected area');return}setHistory(h=>[...h.slice(-49),design]);setFuture([]);setDesign(next)}
 function patch(id:string,p:Partial<Item>){commit(safeUpdateElement(design,id,p))}
 function insertSticker(id:string,authenticated=false){const next=addSticker(design,id,authenticated) as Design;if(next===design){setStatus('No QR-safe space for this decoration');return}commit(next);setSelected(next.elements[next.elements.length-1].id)}
 function finishDrag(){const d=drag.current;drag.current=null;if(!d)return;setDesign(current=>{if(JSON.stringify(current)!==JSON.stringify(d.before)){setHistory(h=>[...h.slice(-49),d.before]);setFuture([])}return current})}
 function undo(){if(!history.length)return;setFuture(f=>[design,...f]);setDesign(history[history.length-1]);setHistory(h=>h.slice(0,-1))}
 function redo(){if(!future.length)return;setHistory(h=>[...h,design]);setDesign(future[0]);setFuture(f=>f.slice(1))}
 function upload(file?:File){if(!file)return;if(!['image/jpeg','image/png','image/webp'].includes(file.type)||file.size>3*1024*1024){alert('Choose a PNG, JPG or WebP under 3 MB.');return}const reader=new FileReader();reader.onload=()=>{const img=new Image();img.onload=()=>{if(img.width>4096||img.height>4096){alert('Image dimensions must be at most 4096 pixels.');return}const canvas=document.createElement('canvas');canvas.width=Math.min(img.width,1600);canvas.height=Math.round(img.height*canvas.width/img.width);canvas.getContext('2d')?.drawImage(img,0,0,canvas.width,canvas.height);commit({...design,backgroundImage:canvas.toDataURL('image/jpeg',0.82)})};img.src=String(reader.result)};reader.readAsDataURL(file)}
 function resetDefault(){if(!window.confirm('Reset the invitation design to its default template? This will discard your current visual changes, but will not change event details, gifts, or the event QR.'))return;commit(resetInvitationToDefault(occasion,{title:eventTitle}) as Design);setSelected('title')}
 function applyTemplate(nextOccasion:string){setOccasion(nextOccasion);setGalleryCategory('All');commit(createOccasionDesign(nextOccasion,{title:eventTitle}) as Design);setSelected('title')}
 function exportDraft(){const blob=new Blob([JSON.stringify(design,null,2)],{type:'application/json'});const url=URL.createObjectURL(blob);const a=document.createElement('a');a.href=url;a.download='gift-invitation-draft.json';a.click();URL.revokeObjectURL(url)}
 function importDraft(file?:File){if(!file||file.size>2*1024*1024)return;file.text().then(s=>{try{const parsed=JSON.parse(s);if(!validateDesign(parsed)||!validateSafeLayout(parsed))throw Error('Invalid or overlapping design');commit(parsed)}catch{alert('Invalid invitation draft')}})}
 async function loadQr(id=eventId){if(!/^[0-9a-f]{8}-[0-9a-f-]{27,}$/i.test(id)){alert('Enter a valid event UUID');return}try{const r=await fetch(`/backend/api/v1/events/${encodeURIComponent(id)}/qr/png`,{credentials:'include'});if(!r.ok)throw Error(`HTTP ${r.status}: login to backend may be required`);const b=await r.blob();if(b.type!=='image/png')throw Error('Unexpected QR response');const url=URL.createObjectURL(b);setQrUrl(old=>{if(old)URL.revokeObjectURL(old);return url});setStatus('Real event QR loaded')}catch(e){alert(`Could not load QR: ${String(e)}. Backend authentication is required.`)}}
 
useEffect(()=>{
  if(!linkedEventId)return;
  let active=true;
  let objectUrl='';
  fetch(
    `/backend/api/v1/events/${encodeURIComponent(linkedEventId)}/qr/png`,
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
},[linkedEventId]);
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
 return <main className="app invitationEditor" data-editor-panel={toolPanel}>
 <header className="studioHeader">
  <div className="studioHeading"><div className="brand"><span className="brandIcon"><Gift size={22}/></span><div><strong>Gift Invitation Studio</strong><small>Create and personalise your invitation</small></div></div><span className="studioSaveStatus"><Save size={14}/>{status}</span></div>
  <div className="studioToolbar" role="toolbar" aria-label="Invitation design actions">
   <div className="studioMainActions">
    <button type="button" ref={aiTrigger} aria-haspopup="dialog" className="studioAiButton" onClick={()=>setAiOpen(true)}><Sparkles size={17}/> Design with AI</button>
    <button type="button" onClick={()=>void downloadArtwork()} disabled={!hasRealQr} className="studioDownloadButton" title={hasRealQr?'Download full invitation as PNG':'Load the real event QR first'}><Download size={17}/> Download PNG</button>
   </div>
   <div className="studioEditActions"><div className="studioHistory" role="group" aria-label="Design history"><button type="button" onClick={undo} disabled={!history.length} title="Undo" aria-label="Undo"><ArrowLeft size={18}/></button><button type="button" onClick={redo} disabled={!future.length} title="Redo" aria-label="Redo"><ArrowRight size={18}/></button></div>
    <details ref={moreMenu} className="studioMore" onKeyDown={e=>{if(e.key==='Escape'){e.currentTarget.open=false;e.currentTarget.querySelector('summary')?.focus()}}}>
     <summary><MoreHorizontal size={18}/> More</summary>
     <div className="studioMoreMenu">
      <button type="button" className="studioImportAction" onClick={()=>importInput.current?.click()}><Upload size={16}/> Import draft</button><input ref={importInput} type="file" accept="application/json,.json" hidden onChange={e=>{importDraft(e.target.files?.[0]);e.target.value='';if(moreMenu.current)moreMenu.current.open=false}}/>
      <button type="button" onClick={()=>{exportDraft();if(moreMenu.current)moreMenu.current.open=false}}><Download size={16}/> Export draft</button>
      <button type="button" onClick={()=>{if(moreMenu.current)moreMenu.current.open=false;resetDefault()}}><RotateCcw size={16}/> Reset invitation to default</button>
     </div>
    </details>
   </div>
  </div>
 </header>{focused&&<nav className="focusEditorTabs" aria-label="Invitation tools">{[['preview','Preview'],['templates','Design tools'],['properties','Selected element']].map(([value,label])=><button key={value} type="button" aria-pressed={toolPanel===value} onClick={()=>setToolPanel(value)}>{label}</button>)}</nav>}{aiOpen&&<AiInvitationDesigner eventId={linkedEventId||eventId} onClose={closeAi} onApply={(next,suggestion)=>{commit(next as Design);setOccasion(suggestion.occasionType);setEventTitle(suggestion.eventTitle);setSelected('title');setToolPanel('preview');if(!linkedEventId&&eventId!==suggestion.eventId){setEventId(suggestion.eventId);setQrUrl(old=>{if(old)URL.revokeObjectURL(old);return ''});void loadQr(suggestion.eventId)}}}/>}<div className="workspace"><aside className="sidebar"><h2>Customize</h2><p>Every detail, your way.</p><div className="section"><h3>Event template</h3><label>Occasion<select value={occasion} onChange={e=>applyTemplate(e.target.value)}>{Object.entries(occasionTemplates).map(([key,t])=><option key={key} value={key}>{t.label}</option>)}</select></label><label>Event name<input value={eventTitle} maxLength={200} placeholder="Your event name" onChange={e=>{setEventTitle(e.target.value);commit(safeUpdateElement(design,'title',{text:e.target.value||occasionTemplates[occasion as keyof typeof occasionTemplates]?.title||'Celebrate With Us'}))}}/></label></div><div className="section"><h3>Elements</h3><button className="wide" onClick={()=>{const next=addText(design);commit(next);setSelected(next.elements.at(-1)!.id)}}><Plus size={17}/> Add text or tagline</button><div className="studioElementScroll" role="region" aria-label="Invitation elements">{design.elements.map(e=><button key={e.id} className={`layer ${selected===e.id?'chosen':''}`} onClick={()=>setSelected(e.id)}>{e.type==='qr'?<Gift size={16}/>:<Type size={16}/>}<span>{e.type==='qr'?'Event QR':e.type==='sticker'?(STICKERS.find(s=>s.id===e.stickerId)?.name||'Decoration'):e.text.slice(0,26)}</span></button>)}</div></div>
<div className="section">
  <h3>Free decorations</h3>
  <p>Add a decoration, then drag or resize it. QR overlap is blocked.</p>
  <div style={{display:'grid',gridAutoFlow:'column',gridAutoColumns:'104px',gridTemplateRows:'1fr',overflowX:'auto',overflowY:'hidden',paddingBottom:10,gap:8}}>
    {STICKERS.map(sticker=><button type="button" key={sticker.id} title={`Add ${sticker.name}`} onClick={()=>insertSticker(sticker.id)} style={{padding:6,border:'1px solid #ddd',borderRadius:8,background:'#fff',cursor:'pointer'}}>
      <img src={sticker.src} alt="" style={{width:'100%',height:58,objectFit:'contain'}}/>
      <span style={{display:'block',fontSize:11}}>{sticker.name}</span>
    </button>)}
  </div>
</div>

<div className="section">
 <h3>Platform Decorations</h3>
  <p>Published decorations for this occasion.</p>
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
      const src=linkedEventId?`/backend/api/v1/commerce/templates/${a.id}/image`:publicAssetImageUrl(a.id);
    if((a.tier==='PREMIUM'&&!linkedEventId)||a.accessible===false)return <a key={a.id} href="/host/monetization" style={{padding:12,border:'1px solid #ddd',borderRadius:8}}>{a.title} · Premium · Unlock →</a>;
      return <button
        key={a.id}
        type="button"
        title={`Add ${a.title}`}
        onClick={()=>insertSticker(platformDecorationId(a.id),!!linkedEventId)}
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

 <div className="section">
  <h3>Platform Backgrounds</h3>
 <p>Published backgrounds from the administrator gallery. <a href="/host/monetization">Browse premium invitations and plans →</a></p>
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
    const src=linkedEventId?`/backend/api/v1/commerce/templates/${a.id}/image`:publicAssetImageUrl(a.id);
    if((a.tier==='PREMIUM'&&!linkedEventId)||a.accessible===false)return <a key={a.id} href="/host/monetization" style={{padding:12,border:'1px solid #ddd',borderRadius:8}}>{a.title} · Premium · Unlock →</a>;
    return <button
     key={a.id}
     type="button"
     title={`Apply ${a.title}`}
     onClick={()=>commit({...design,backgroundImage:src})}
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

<div className="section">
  <h3>Background Gallery</h3>
  <p>Select a Free background without changing your text or QR.</p>

  <label>Collection
    <select
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
        title={`Apply ${b.name}`}
        onClick={()=>commit({...design,backgroundImage:b.src})}
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
          alt={`${b.name} invitation background`}
          style={{
            width:'100%',
            aspectRatio:'3 / 4',
            objectFit:'cover',
            borderRadius:4
          }}
        />
        <span style={{display:'block',fontSize:12}}>
          {b.name} · Free
        </span>
      </button>
    )}
  </div>

  <small>All 36 Free backgrounds are available; matching occasions appear first.</small>
</div>
</aside><section className="stage"><div className="stageHeader"><span>Live invitation preview</span><div><label>Zoom <select value={scale} onChange={e=>setScale(Number(e.target.value))}>{focused&&<option value={fit}>Fit to screen</option>}<option value="0.5">50%</option><option value="0.7">70%</option><option value="1">100%</option></select></label></div></div><div className="canvasViewport"><div className="canvasFrame" style={{width:design.width*scale,height:design.height*scale}}><div className="canvas" style={{width:design.width,height:design.height,transform:`scale(${scale})`,backgroundColor:design.background,backgroundImage:design.backgroundImage?`linear-gradient(#ffffff88,#ffffff88),url("${design.backgroundImage}")`:undefined,
    backgroundSize:design.backgroundImage&&backgroundSize
      ?(()=>{const r=backgroundImageRect(backgroundSize.w,backgroundSize.h,design.width,design.height,design.backgroundTransform);return `100% 100%, ${r.width}px ${r.height}px`})()
      :undefined,
    backgroundPosition:design.backgroundImage&&backgroundSize
      ?(()=>{const r=backgroundImageRect(backgroundSize.w,backgroundSize.h,design.width,design.height,design.backgroundTransform);return `center, ${r.x}px ${r.y}px`})()
      :undefined,
    backgroundRepeat:'no-repeat',
    cursor:editBackground?'grab':undefined,
    touchAction:editBackground?'none':undefined}} onPointerMove={e=>{if(backgroundDrag.current){backgroundPointerMove(e);return}if(!drag.current)return;const d=drag.current;const dx=Math.round((e.clientX-d.sx)/scale);const dy=Math.round((e.clientY-d.sy)/scale);const patch=d.mode==='resize'?{w:Math.max(d.id==='qr'?MIN_QR_SIZE:MIN_TEXT_WIDTH,d.ow+dx),h:Math.max(d.id==='qr'?MIN_QR_SIZE:MIN_TEXT_HEIGHT,d.oh+dy)}:{x:d.ox+dx,y:d.oy+dy};setDesign(old=>safeUpdateElement(old,d.id,patch))}} onPointerDown={backgroundPointerDown} onPointerUp={()=>{backgroundPointerUp();finishDrag()}} onPointerCancel={()=>{backgroundPointerUp();finishDrag()}}>{design.elements.map(el=><div key={el.id} onPointerDown={e=>{if(editBackground)return;if(el.type==='qr'||el.type==='text'||el.type==='sticker'){setSelected(el.id);drag.current={id:el.id,sx:e.clientX,sy:e.clientY,ox:el.x,oy:el.y,ow:el.w,oh:el.h,mode:'move',before:design};e.currentTarget.setPointerCapture(e.pointerId)}}} className={`canvasElement ${selected===el.id?'active':''}`} style={{left:el.x,top:el.y,width:el.w,height:el.h,fontFamily:el.fontFamily||"Georgia, serif",fontSize:el.fontSize,color:el.color,fontWeight:el.fontWeight,textAlign:el.align as 'left'|'center'|'right',touchAction:'none'}}>{el.type==='sticker'?<img src={el.src} alt={STICKERS.find(s=>s.id===el.stickerId)?.name||'Decoration'} draggable={false} style={{width:'100%',height:'100%',objectFit:'contain',pointerEvents:'none'}}/>:el.type==='qr'?<div className="qrSlot">{hasRealQr?<img src={qrUrl} alt="Event QR"/>:<><Gift size={44}/><small>QR PREVIEW</small><small>Not scannable</small></>}</div>:el.text}{selected===el.id&&<div title="Drag to resize" aria-label={`Resize ${el.id}`} onPointerDown={e=>{e.stopPropagation();drag.current={id:el.id,sx:e.clientX,sy:e.clientY,ox:el.x,oy:el.y,ow:el.w,oh:el.h,mode:'resize',before:design};e.currentTarget.setPointerCapture(e.pointerId)}} onPointerMove={e=>{if(!drag.current||drag.current.mode!=='resize')return;e.stopPropagation();const d=drag.current;const dx=Math.round((e.clientX-d.sx)/scale);const dy=Math.round((e.clientY-d.sy)/scale);setDesign(old=>safeUpdateElement(old,d.id,{w:Math.max(el.type==='qr'?MIN_QR_SIZE:MIN_TEXT_WIDTH,d.ow+dx),h:Math.max(el.type==='qr'?MIN_QR_SIZE:MIN_TEXT_HEIGHT,d.oh+dy)}))}} onPointerUp={e=>{e.stopPropagation();finishDrag()}} onPointerCancel={e=>{e.stopPropagation();finishDrag()}} style={{position:'absolute',right:-7,bottom:-7,width:16,height:16,background:'#80538e',border:'2px solid white',borderRadius:4,cursor:'nwse-resize',touchAction:'none',zIndex:10}}/>}</div>)}</div></div></div><div className="hint"><Move size={15}/> Drag elements on the invitation. Use the right panel for precise sizing and text.</div>{children && <div className="qrBelowPreview">{children}</div>}</section><aside className="inspector"><h2>Properties</h2>{active?<div className="section"><h3>{active.type==='qr'?'QR element':active.type==='sticker'?'Decoration':'Text element'}</h3>{active.type==='text'&&<><label>Message<textarea rows={4} value={active.text} onChange={e=>patch(active.id,{text:e.target.value})}/></label><label>Text color<input type="color" value={active.color} onChange={e=>patch(active.id,{color:e.target.value})}/></label><label>Font style
      <select
        value={active.fontFamily||"Georgia, serif"}
        onChange={e=>patch(active.id,{fontFamily:e.target.value})}
      >
        {FONT_CHOICES.map(f=>
          <option key={f.value} value={f.value}>{f.label}</option>
        )}
      </select>
    </label>
    <label>Font size<input type="range" min="12" max="70" value={active.fontSize} onChange={e=>patch(active.id,{fontSize:Number(e.target.value)})}/><span>{active.fontSize}px</span></label><label>Alignment<select value={active.align} onChange={e=>patch(active.id,{align:e.target.value})}><option value="left">Left</option><option value="center">Center</option><option value="right">Right</option></select></label></>}<div className="studioCoordinates">{['x','y','w','h'].map(k=><label key={k}>{({x:'Left',y:'Top',w:'Width',h:'Height'} as Record<string,string>)[k]}<input type="number" value={active[k as 'x'|'y'|'w'|'h']} onChange={e=>patch(active.id,{[k]:Number(e.target.value)})}/></label>)}</div>{active.type!=='qr'&&<button className="danger" onClick={()=>{commit(deleteElement(design,active.id));setSelected('qr')}}><Trash2 size={16}/> Delete element</button>}</div>:<p>Select an element.</p>}<div className="section"><h3>Background position</h3>
      <button type="button" className="wide" onClick={()=>setEditBackground(v=>!v)}>
        {editBackground?'Finish positioning':'Edit background — drag to position'}
      </button>
      <label>Background zoom: {Math.round(normalizeBackgroundTransform(design.backgroundTransform).zoom*100)}%
        <input type="range" min="100" max="300" step="5"
          value={Math.round(normalizeBackgroundTransform(design.backgroundTransform).zoom*100)}
          onChange={e=>backgroundChange({
            ...normalizeBackgroundTransform(design.backgroundTransform),
            zoom:Number(e.target.value)/100
          })}/>
      </label>
      <button type="button" className="wide"
        onClick={()=>backgroundChange(resetBackgroundTransform())}>
        Reset background position
      </button>
      <small>Enable Edit background, then drag the invitation image. Text and QR remain fixed.</small>
    </div><div className="section"><h3>Background</h3><label className="wide upload"><ImagePlus size={17}/> Upload photo<input type="file" accept="image/png,image/jpeg,image/webp" hidden onChange={e=>upload(e.target.files?.[0])}/></label><label>Background color<input type="color" value={design.background} onChange={e=>commit({...design,background:e.target.value})}/></label><button className="wide" onClick={()=>commit({...design,backgroundImage:''})}><RotateCcw size={16}/> Remove background photo</button></div><div className="section"><h3>Real event QR</h3>{!linkedEventId&&(focused?<label>Celebration<select value={eventId} onChange={e=>setEventId(e.target.value)}><option value="">Choose a celebration</option>{eventChoices.map(e=><option key={e.id} value={e.id}>{e.title}</option>)}</select></label>:<input placeholder="Event UUID" value={eventId} onChange={e=>setEventId(e.target.value)}/>)}<button className="wide" onClick={()=>void loadQr()}><ShieldCheck size={16}/> Load authenticated QR</button><small>The preview placeholder is NOT scannable. A real QR is required for final artwork.</small></div><div className="notice"><ShieldCheck size={18}/><p>{focused?'Your design stays in this browser. Download the finished invitation to share it.':'Drafts are stored in this browser only. Server-side publishing and scan validation are planned for the next integration batch.'}</p></div></aside></div></main>
}
