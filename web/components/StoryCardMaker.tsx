'use client';
import {useEffect,useRef,useState} from 'react';
import {localizeText} from '../lib/localization.mjs';
import {useLocalizationRender} from './LocalizationProvider';
import {occasionArt,eventDate} from '../lib/focused.mjs';
import {wrapLines,cardCopy,daysUntil} from '../lib/acquisition.mjs';
import './acquisition.css';

export type CardKind='INVITE'|'COUNTDOWN'|'THANK_YOU'|'LIVE';
type Props={title:string;hostName?:string|null;occasion:string;date?:string|null;link:string;kinds?:CardKind[];accent?:string;eventToken?:string};
const KIND_LABEL:Record<CardKind,string>={INVITE:'Invitation',COUNTDOWN:'Countdown',THANK_YOU:'Thank you',LIVE:'Happening now'};
const W=1080,H=1920;

/** Draws a 1080×1920 card for WhatsApp Status / Instagram Stories, then shares it as a file or downloads it. */
export default function StoryCardMaker({title,hostName,occasion,date,link,kinds=['INVITE','COUNTDOWN','THANK_YOU'],accent='#683c55',eventToken}:Props){useLocalizationRender();
 const counted=useRef(false);
 const count=()=>{if(counted.current)return;counted.current=true;void fetch('/backend/api/public/acquisition-events',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({kind:'STORY_CARD_SHARED',...(eventToken?{eventToken}:{})})}).catch(()=>{})};
 const canvas=useRef<HTMLCanvasElement>(null);const [kind,setKind]=useState<CardKind>(kinds[0]);const [busy,setBusy]=useState(false),[note,setNote]=useState('');
 useEffect(()=>{const c=canvas.current;if(!c)return;const ctx=c.getContext('2d');if(!ctx)return;let cancelled=false;
  const art=new Image();art.src=`/invitation-backgrounds/${occasionArt(occasion)}.svg`;
  const draw=(withArt:boolean)=>{if(cancelled)return;
   const g=ctx.createLinearGradient(0,0,0,H);g.addColorStop(0,'#fffaf3');g.addColorStop(1,'#f6ece4');ctx.fillStyle=g;ctx.fillRect(0,0,W,H);
   if(withArt){ctx.globalAlpha=.9;ctx.drawImage(art,0,0,W,760);ctx.globalAlpha=1;const fade=ctx.createLinearGradient(0,560,0,780);fade.addColorStop(0,'rgba(255,250,243,0)');fade.addColorStop(1,'#fffaf3');ctx.fillStyle=fade;ctx.fillRect(0,560,W,220)}
   const copy=cardCopy(kind,{title,hostName:hostName??'',days:daysUntil(date??null)});
   ctx.textAlign='center';ctx.fillStyle=accent;ctx.font='700 46px Inter, system-ui, sans-serif';ctx.fillText(localizeText(copy.kicker).toUpperCase(),W/2,880);
   ctx.fillStyle='#2d2420';ctx.font='500 104px Georgia, serif';const lines=wrapLines(kind==="THANK_YOU"?localizeText(copy.headline):copy.headline,900,(t:string)=>ctx.measureText(t).width,4);
   lines.forEach((l,i)=>ctx.fillText(l,W/2,1030+i*124));let y=1030+lines.length*124+40;
   ctx.font='400 50px Inter, system-ui, sans-serif';ctx.fillStyle='#5f5049';
   if(kind!=='THANK_YOU'&&date){ctx.fillText(eventDate(date),W/2,y);y+=74}
   if(kind!=='THANK_YOU'&&hostName){ctx.fillText(localizeText('Hosted by {{name}}').replace('{{name}}',hostName),W/2,y);y+=74}
   ctx.fillStyle=accent;const bw=720,bh=120,bx=(W-bw)/2,by=1560;ctx.beginPath();ctx.roundRect(bx,by,bw,bh,60);ctx.fill();
   ctx.fillStyle='#fff';ctx.font='700 48px Inter, system-ui, sans-serif';ctx.fillText(kind==='THANK_YOU'?copy.footer:localizeText(copy.footer),W/2,by+76);
   ctx.fillStyle='#8a7a70';ctx.font='400 34px Inter, system-ui, sans-serif';ctx.fillText(link.replace(/^https?:\/\//,'').slice(0,48),W/2,1760);
   ctx.font='600 30px Inter, system-ui, sans-serif';ctx.fillText(localizeText('Made with GiftWise'),W/2,1850)};
  art.onload=()=>draw(true);art.onerror=()=>draw(false);draw(false);return()=>{cancelled=true}},[kind,title,hostName,occasion,date,link,accent]);
 async function blob(){return new Promise<Blob|null>(r=>canvas.current?.toBlob(b=>r(b),'image/png'))}
 async function share(){setBusy(true);setNote('');try{const b=await blob();if(!b)throw Error();const file=new File([b],'giftwise-card.png',{type:'image/png'});
  if(navigator.canShare?.({files:[file]})){await navigator.share({files:[file],text:`${title} — ${link}`});count()}else{download(b);setNote('Saved the image. Open WhatsApp → Status → add the image, and paste the link.')}}
  catch(e){if((e as Error)?.name!=='AbortError')setNote('Could not share. Use Download instead.')}finally{setBusy(false)}}
 function download(b?:Blob|null){count();const go=(x:Blob)=>{const u=URL.createObjectURL(x);const a=document.createElement('a');a.href=u;a.download='giftwise-card.png';a.click();setTimeout(()=>URL.revokeObjectURL(u),1000)};if(b)go(b);else void blob().then(x=>x&&go(x))}
 async function copyLink(){try{await navigator.clipboard.writeText(link);setNote('Link copied — paste it with your Status.')}catch{setNote(link)}}
 return <div className="scMaker">
  {kinds.length>1&&<div className="gbTabs" role="tablist" aria-label={localizeText('Card type')}>{kinds.map(k=><button key={k} type="button" role="tab" aria-selected={kind===k} onClick={()=>setKind(k)}>{localizeText(KIND_LABEL[k])}</button>)}</div>}
  <canvas ref={canvas} width={W} height={H} className="scCanvas" role="img" aria-label={localizeText('Story card preview')}/>
  <div className="scActions"><button type="button" className="focusPrimary" disabled={busy} onClick={()=>void share()}>{localizeText('Share to Status')}</button>
   <button type="button" className="focusSecondary" onClick={()=>download()}>{localizeText('Download')}</button><button type="button" className="gbPlain" onClick={()=>void copyLink()}>{localizeText('Copy link')}</button></div>
  {note&&<p className="gbFine" role="status">{localizeText(note)}</p>}
 </div>;
}
