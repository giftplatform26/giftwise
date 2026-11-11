'use client';
import {useCallback,useEffect,useRef,useState} from 'react';
import {useParams} from 'next/navigation';
import {localizeText} from '../../../lib/localization.mjs';
import {useLocalizationRender} from '../../../components/LocalizationProvider';
import {nextSlides} from '../../../lib/register.mjs';

type Item={id:string;guestName:string;message:string|null;photoUrl:string|null;createdAt:string};
type Wall={eventTitle:string;hostName:string|null;headline:string|null;theme:string;showJoinQr:boolean;slideSeconds:number;invitationPath:string;items:Item[]};
const POLL_MS=15000;
const safePhoto=(u:string|null)=>u&&/^\/backend\/api\/public\/celebrations\/[0-9a-f-]{36}\/wishes\/[0-9a-f-]{36}\/photo$/i.test(u)?u:null;

/** Full-screen slideshow for a TV or projector. Polls quietly, keeps playing through brief network drops. */
export default function LiveWall(){useLocalizationRender();
 const {token}=useParams();const id=String(token??'');
 const [wall,setWall]=useState<Wall|null>(null),[slides,setSlides]=useState<Item[]>([]),[index,setIndex]=useState(0),[state,setState]=useState<'loading'|'ok'|'gone'|'offline'>('loading'),[isNew,setIsNew]=useState(false);
 const shown=useRef(new Set<string>()),slidesRef=useRef<Item[]>([]),queue=useRef<string[]>([]);
 const load=useCallback(async()=>{if(!/^[0-9a-f-]{36}$/i.test(id)){setState('gone');return}
  try{const r=await fetch(`/backend/api/public/live/${id}`,{cache:'no-store'});if(r.status===404){setState('gone');return}if(!r.ok)throw Error();
   const w=await r.json() as Wall;setWall(w);setState('ok');
   const {fresh,all}=nextSlides(slidesRef.current,w.items,shown.current);
   if(slidesRef.current.length===0){slidesRef.current=w.items;setSlides(w.items)}else{queue.current.push(...fresh.map((f:Item)=>f.id));slidesRef.current=all;setSlides(all)}
  }catch{setState(s=>s==='ok'||s==='offline'?'offline':'loading')}},[id]);
 useEffect(()=>{void load();const t=setInterval(load,POLL_MS);return()=>clearInterval(t)},[load]);
 // Advance: newly approved wishes jump the queue, then the slideshow keeps looping.
 useEffect(()=>{if(!wall||slides.length===0)return;const t=setTimeout(()=>{const nextNew=queue.current.shift();
   if(nextNew){const i=slidesRef.current.findIndex(s=>s.id===nextNew);if(i>=0){setIndex(i);setIsNew(true);return}}
   setIsNew(false);setIndex(i=>(i+1)%slidesRef.current.length)},wall.slideSeconds*1000);return()=>clearTimeout(t)},[wall,slides,index]);
 useEffect(()=>{const s=slides[index];if(s)shown.current.add(s.id)},[slides,index]);
 useEffect(()=>{function key(e:KeyboardEvent){if(e.key==='f'||e.key==='F'){if(document.fullscreenElement)void document.exitFullscreen();else void document.documentElement.requestFullscreen?.()}}
  window.addEventListener('keydown',key);let lock:{release:()=>Promise<void>}|null=null;
  // Keep the TV from sleeping during the celebration.
  (navigator as Navigator&{wakeLock?:{request:(t:'screen')=>Promise<{release:()=>Promise<void>}>}}).wakeLock?.request('screen').then(l=>{lock=l}).catch(()=>{});
  return()=>{window.removeEventListener('keydown',key);void lock?.release()}},[]);

 if(state==='gone')return <main className="lw lw--night lwCenter"><div><p className="lwEyebrow">GiftWise</p><h1>{localizeText('This wall is not playing right now')}</h1><p>{localizeText('The host may have paused it or created a new link.')}</p></div></main>;
 if(!wall)return <main className="lw lw--night lwCenter"><p className="lwEyebrow">{localizeText('Getting the wall ready…')}</p></main>;
 const slide=slides[index%Math.max(1,slides.length)];const photo=safePhoto(slide?.photoUrl??null);
 return <main className={`lw lw--${wall.theme.toLowerCase()}`} onDoubleClick={()=>void document.documentElement.requestFullscreen?.()}>
  <header className="lwTop"><div><p className="lwEyebrow">{wall.hostName?localizeText('Celebrating with {{name}}').replace('{{name}}',wall.hostName):localizeText('Live wishes')}</p><h1>{wall.headline||wall.eventTitle}</h1></div>
   {state==='offline'&&<span className="lwOffline" role="status">{localizeText('Reconnecting…')}</span>}</header>
  {!slide?<section className="lwEmpty"><h2>{localizeText('Be the first to send your wishes!')}</h2>{wall.showJoinQr&&<p>{localizeText('Scan the code to write a message or share a photo.')}</p>}</section>:
   <section key={slide.id} className={`lwSlide${photo?' lwSlide--photo':''}${isNew?' is-new':''}`} aria-live="polite">
    {isNew&&<span className="lwNewBadge">{localizeText('Just in')}</span>}
    {photo&&<img src={photo} alt={localizeText('Photo from {{name}}').replace('{{name}}',slide.guestName)}/>}
    <figure>{slide.message&&<blockquote>{slide.message}</blockquote>}<figcaption>— {slide.guestName}</figcaption></figure>
   </section>}
  <footer className="lwFoot">
   {slides.length>1&&<div className="lwDots" aria-hidden="true">{slides.slice(0,30).map((s,i)=><span key={s.id} className={i===index?'on':''}/>)}</div>}
   {wall.showJoinQr&&<div className="lwJoin"><img src={`/backend/api/public/live/${id}/join-qr.png`} alt="" width={132} height={132}/><p><strong>{localizeText('Add your wish')}</strong><span>{localizeText('Scan with your phone camera')}</span></p></div>}
  </footer>
 </main>;
}
