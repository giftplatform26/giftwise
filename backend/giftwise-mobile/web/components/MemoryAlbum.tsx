'use client';
import {activeLocale} from '../lib/localization.mjs';

/* localization:instrumented */
import {localize,localizeText} from '../lib/localization.mjs';
import {useLocalizationRender} from './LocalizationProvider';

import {useCallback,useEffect,useRef,useState,type CSSProperties} from 'react';
import Link from 'next/link';
import {useParams} from 'next/navigation';
import {ArrowLeft,ArrowRight,Heart,Images,Maximize,Minimize,Pause,Play,RefreshCw,Sparkles} from 'lucide-react';
import {memoryIndex,memoryDuration,mergeMemories} from '../lib/album.mjs';
import './memory-album.css';
type Memory={id:string;guestName:string;message:string;photoAvailable:boolean;createdAt:string;status:string};
type Album={title:string;eventDate:string|null;memories:Memory[];totalCount:number;photoCount:number;hasMore:boolean;nextOffset:number};
function date(value:string|null){return value?new Intl.DateTimeFormat(activeLocale("en-IN"),{day:'numeric',month:'long',year:'numeric'}).format(new Date(value.length===10?value+'T12:00:00':value)):'';}
function MemoryPhoto({src,name,className}:{src:string;name:string;className?:string}){useLocalizationRender();
 const [failed,setFailed]=useState(false);
 return failed?<div className="ma-photo-missing"><Heart aria-hidden="true"/><p>{localize("ui.6818d89aad457107",{defaultValue:"A moment shared with love by "})}{name}</p></div>:<img className={className} src={src} alt={localize("ui.4a367967f0d70a93",{defaultValue:"A memory shared by {{value0}}",value0:(name)})} onError={()=>setFailed(true)}/>;
}
export default function MemoryAlbum(){useLocalizationRender();
 const params=useParams(),eventId=String(params.id||'');
 const [album,setAlbum]=useState<Album|null>(null),[index,setIndex]=useState(0),[playing,setPlaying]=useState(false),[seconds,setSeconds]=useState(10),[hidden,setHidden]=useState(false);
 const [loading,setLoading]=useState(true),[more,setMore]=useState(false),[error,setError]=useState(''),[full,setFull]=useState(false);
 const stage=useRef<HTMLDivElement>(null),film=useRef<HTMLDivElement>(null),request=useRef<AbortController|null>(null),generation=useRef(0),moreBusy=useRef(false);
 const photo=(memory:Memory)=>`/backend/api/v1/events/${encodeURIComponent(eventId)}/album/${encodeURIComponent(memory.id)}/photo`;
 const fetchPage=useCallback(async(offset:number,signal?:AbortSignal):Promise<Album>=>{
  const response=await fetch(`/backend/api/v1/events/${encodeURIComponent(eventId)}/album?limit=60&offset=${offset}&includeHidden=${hidden}`,{credentials:'include',cache:'no-store',signal});
  if(!response.ok)throw Error(response.status===404?'This album is not available. Ask your administrator to enable Memory album for your host account and this event.':response.status===401?'Please sign in as the event host to open this album.':'Could not open your memories. Please try again.');
  return response.json();
 },[eventId,hidden]);
 const refresh=useCallback(async()=>{
  request.current?.abort();const control=new AbortController();request.current=control;generation.current++;moreBusy.current=false;setMore(false);setLoading(true);setPlaying(false);setError('');setAlbum(null);setIndex(0);
  try{const value=await fetchPage(0,control.signal);if(!control.signal.aborted)setAlbum(value)}catch(e){if(!control.signal.aborted)setError(e instanceof Error?e.message:'Could not load your album.')}finally{if(!control.signal.aborted)setLoading(false)}
 },[fetchPage]);
 useEffect(()=>{void refresh();return()=>{request.current?.abort();generation.current++}},[refresh]);
 async function loadMore(){
  if(!album?.hasMore||moreBusy.current)return false;const version=generation.current;moreBusy.current=true;setMore(true);setError('');
  try{const next=await fetchPage(album.nextOffset,request.current?.signal);if(version!==generation.current)return false;setAlbum(current=>current?{...next,memories:mergeMemories(current.memories,next.memories)}:next);return next.memories.length>0}
  catch(e){if(version===generation.current){setPlaying(false);setError(e instanceof Error?e.message:'Could not load more memories.')}return false}finally{if(version===generation.current){moreBusy.current=false;setMore(false)}}
 }
 const memories=album?.memories||[],current=memories[index];
 async function advance(direction:number){
  if(direction>0&&index===memories.length-1&&album?.hasMore){if(await loadMore())setIndex(value=>value+1);return;}
  setIndex(value=>memoryIndex(value,direction,memories.length));
 }
 useEffect(()=>{
  if(!playing||!current||more)return;
  const timer=setTimeout(()=>void advance(1),memoryDuration(current.message,seconds));return()=>clearTimeout(timer);
 },[playing,index,current?.id,seconds,more,album?.nextOffset]);
 useEffect(()=>{const visible=()=>{if(document.hidden)setPlaying(false)};document.addEventListener('visibilitychange',visible);return()=>document.removeEventListener('visibilitychange',visible)},[]);
 useEffect(()=>{const change=()=>setFull(document.fullscreenElement===stage.current);document.addEventListener('fullscreenchange',change);return()=>document.removeEventListener('fullscreenchange',change)},[]);
 useEffect(()=>{const strip=film.current,active=strip?.querySelector<HTMLElement>('[aria-current="true"]');if(strip&&active)strip.scrollTo({left:active.offsetLeft-strip.offsetLeft-strip.clientWidth/2+active.clientWidth/2,behavior:'instant'})},[index]);
 async function fullscreen(){try{if(document.fullscreenElement===stage.current)await document.exitFullscreen();else await stage.current?.requestFullscreen()}catch{setError('Fullscreen is unavailable in this browser. You can still enjoy the slideshow here.')}}
 return <main className="ma-page"><Link className="ma-back" href={`/events/${encodeURIComponent(eventId)}`}><ArrowLeft size={16}/> {localize("ui.e58d2eeaba727ad5",{defaultValue:" Back to your celebration"})}</Link>
 <header className="ma-header"><div><p className="ma-eyebrow"><Sparkles size={15}/> {localize("ui.9aeecdf762e11b3e",{defaultValue:" A LITTLE TIME CAPSULE"})}</p><h1>{localize("ui.a77e141cf3167fe5",{defaultValue:"Your day. Their love."})}<br/><em>{localize("ui.57ce57082395ef32",{defaultValue:"Forever yours."})}</em></h1><p className="ma-intro">{localize("ui.a580c02b630d7bba",{defaultValue:"The smiles, the kind words, the people who made it yours. Come back to this feeling, anytime."})}</p></div><div className="ma-title-card"><Heart size={24} aria-hidden="true"/><h2>{album?.title||localize("ui.af38142a10020703",{defaultValue:"Your celebration"})}</h2>{album?.eventDate&&<p>{date(album.eventDate)}</p>}<span>{localize("ui.4b0a34d3edfd63b5",{defaultValue:"Collected with love"})}</span></div></header>
 <div className="ma-toolbar"><p>{album?localize("ui.00b293fa573bf60e",{defaultValue:"{{value0}} {{value1}} · {{value2}} {{value3}}",value0:(album.totalCount),value1:(album.totalCount===1?'memory':'memories'),value2:(album.photoCount),value3:(album.photoCount===1?'photo':'photos')}):localize("ui.4735ae197ae8cb1e",{defaultValue:"Your memories, all in one place"})}</p><div><a href={`/backend/api/v1/events/${encodeURIComponent(eventId)}/album/keepsake`}>{localize("ui.7ee416c744e489e7",{defaultValue:"Download wishes keepsake"})}</a><label><input type="checkbox" checked={hidden} onChange={e=>setHidden(e.target.checked)}/> {localize("ui.5729ddc0c0cb1201",{defaultValue:" Include hidden wishes"})}</label><button type="button" disabled={loading} onClick={()=>void refresh()}><RefreshCw size={16}/> {localize("ui.7ce64141b163cd0d",{defaultValue:" Refresh"})}</button></div></div>
 {error&&<p className="ma-error" role="alert">{localizeText(error)}</p>}
 {loading?<section className="ma-empty" role="status"><Sparkles size={32}/><h2>{localize("ui.5d66a61e05aaf415",{defaultValue:"Gathering the little moments…"})}</h2><p>{localize("ui.9519f929905050dd",{defaultValue:"Your celebration is about to come back to life."})}</p></section>:!current?<section className="ma-empty"><Images size={42} aria-hidden="true"/><h2>{localize("ui.960847b0106b72e7",{defaultValue:"The best memories are still on their way."})}</h2><p>{localize("ui.dbf0f8ab3fd81eb2",{defaultValue:"When your guests leave a photo or a heartfelt wish on your invitation, it will find a home here."})}</p><Link href={`/events/${encodeURIComponent(eventId)}/qr`}>{localize("ui.5fc19c12d3d2003e",{defaultValue:"Share your invitation"})}</Link></section>:<>
 <div className="ma-player" ref={stage} tabIndex={0} aria-label={localize("ui.d410b8da08bd5785",{defaultValue:"Event memory slideshow"})} onKeyDown={e=>{if((e.target as HTMLElement).closest('button,input,select,a,textarea'))return;if(e.key==='ArrowRight'){e.preventDefault();setPlaying(false);void advance(1)}else if(e.key==='ArrowLeft'){e.preventDefault();setPlaying(false);void advance(-1)}else if(e.code==='Space'){e.preventDefault();setPlaying(value=>!value)}}}>
 <div className="ma-player-top"><span><Heart size={14}/> {album?.title}</span><span>{localize("ui.73310a5f78355026",{defaultValue:"MEMORY "})}{String(index+1).padStart(2,'0')} / {album?.totalCount}</span></div>
 <div className={`ma-slide ${current.photoAvailable?'ma-has-photo':'ma-words-only'}`} key={current.id} aria-roledescription="slide" aria-label={localize("ui.fed9d49a94f91a50",{defaultValue:"Memory {{value0}} from {{value1}}",value0:(index+1),value1:(current.guestName)})}>
 {current.photoAvailable&&<figure className="ma-photo"><MemoryPhoto key={current.id} src={photo(current)} name={current.guestName}/><figcaption>{localize("ui.2f0c47a53e96d299",{defaultValue:"A little piece of your day"})}</figcaption></figure>}
 <div className="ma-letter"><span className="ma-paper-stamp" aria-hidden="true">{localize("ui.67908b51088eb589",{defaultValue:"WITH LOVE"})}<br/>♡</span><span className="ma-quote-mark" aria-hidden="true">“</span><blockquote>{current.message}</blockquote><div className="ma-signature"><span>{localize("ui.855f9785df469ff4",{defaultValue:"With love,"})}</span><strong>{current.guestName}</strong></div><p className="ma-memory-date">{date(current.createdAt)}</p></div></div>
 <div className="ma-progress" aria-hidden="true"><span key={`${current.id}-${seconds}-${playing}-${more}`} style={{'--ma-duration':`${memoryDuration(current.message,seconds)}ms`,animationPlayState:playing&&!more?'running':'paused'} as CSSProperties}/></div>
 <div className="ma-controls"><div className="ma-play-controls"><button type="button" aria-label={localize("ui.80f4fe277c5d683b",{defaultValue:"Previous memory"})} disabled={more} onClick={()=>{setPlaying(false);void advance(-1)}}><ArrowLeft size={19}/></button><button type="button" className="ma-play" disabled={more} onClick={()=>setPlaying(value=>!value)}>{playing?<Pause size={18}/>:<Play size={18}/>} {playing?localize("ui.dca0331d5734fb7d",{defaultValue:"Pause the moment"}):localize("ui.22a40ecca59e6973",{defaultValue:"Play our memories"})}</button><button type="button" aria-label={localize("ui.675bdf186f98ee90",{defaultValue:"Next memory"})} disabled={more} onClick={()=>{setPlaying(false);void advance(1)}}><ArrowRight size={19}/></button></div><div className="ma-options"><label>{localize("ui.e0adcfcf556ebd36",{defaultValue:"Take your time"})}<select value={seconds} onChange={e=>setSeconds(Number(e.target.value))}><option value={6}>{localize("ui.5a9dbb2f2598c016",{defaultValue:"A quick glimpse"})}</option><option value={10}>{localize("ui.db9c6c1c0bf0971d",{defaultValue:"A little longer"})}</option><option value={15}>{localize("ui.74d7398dc2041ddc",{defaultValue:"Savour every moment"})}</option></select></label><button type="button" onClick={()=>void fullscreen()} aria-label={full?localize("ui.37fd4e355ba33fd2",{defaultValue:"Exit fullscreen"}):localize("ui.abe9d6ad311ebbfa",{defaultValue:"View album fullscreen"})}>{full?<Minimize size={18}/>:<Maximize size={18}/>}</button></div></div>
 </div>
 <div className="ma-filmstrip" ref={film} aria-label={localize("ui.e73b87338f1ae1f3",{defaultValue:"Choose a memory"})}>{memories.map((memory,i)=><button type="button" key={memory.id} aria-current={i===index?'true':undefined} aria-label={localize("ui.fed9d49a94f91a50",{defaultValue:"Memory {{value0}} from {{value1}}",value0:(i+1),value1:(memory.guestName)})} onClick={()=>{setPlaying(false);setIndex(i)}}>{memory.photoAvailable?<img loading="lazy" src={photo(memory)} alt="" onError={e=>{e.currentTarget.style.visibility='hidden'}}/>:<span className="ma-thumb-quote" aria-hidden="true">“</span>}<span>{memory.guestName}</span></button>)}</div>
 {album?.hasMore&&<div className="ma-more"><button type="button" disabled={more} onClick={()=>void loadMore()}>{more?localize("ui.c4333689235b8b60",{defaultValue:"Gathering more memories…"}):localize("ui.645c7f6715d44a1a",{defaultValue:"Unfold more memories"})}</button><p>{memories.length} {localize("ui.a4282e4b2298b90b",{defaultValue:" of "})}{album.totalCount} {localize("ui.89ad77ff4eb49808",{defaultValue:" memories"})}</p></div>}
 </>}
 <footer className="ma-footer"><Heart size={15} aria-hidden="true"/><p>{localize("ui.7d288f0050030e61",{defaultValue:"Some days become stories. This one became yours."})}</p><small>{localize("ui.bafad87c2c013724",{defaultValue:"A private keepsake for you. Guestbook visibility stays as you set it."})}</small></footer></main>;
}
