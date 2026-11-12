'use client';
import {useEffect,useRef,useState} from 'react';
import {localizeText} from '../lib/localization.mjs';
import {useLocalizationRender} from './LocalizationProvider';
import {hostRequest} from '../lib/host-api.mjs';

type Result={guestName:string;partySize:number;table:string|null;alreadyArrived:boolean};
type Detector={detect:(src:HTMLVideoElement)=>Promise<{rawValue:string}[]>};

/** Check-in by pass: camera scan where the browser supports it (Chrome on Android), or the 6-letter code anywhere. */
export default function PassScanner({eventId,onDone}:{eventId:string;onDone:()=>void}){useLocalizationRender();
 const [code,setCode]=useState(''),[msg,setMsg]=useState<{ok:boolean;text:string}|null>(null),[camera,setCamera]=useState(false),[canScan,setCanScan]=useState(false);
 const video=useRef<HTMLVideoElement>(null),busy=useRef(false);
 useEffect(()=>{setCanScan(typeof window!=='undefined'&&'BarcodeDetector' in window&&!!navigator.mediaDevices?.getUserMedia)},[]);
 async function submit(value:string){if(busy.current||!value.trim())return;busy.current=true;
  try{const r=await hostRequest(fetch,`/v1/events/${eventId}/checkin/pass`,{method:'POST',payload:{code:value}}) as Result;
   setMsg({ok:true,text:(r.alreadyArrived?localizeText('{{n}} was already checked in.'):localizeText('Welcome, {{n}}! {{p}} people checked in.')).replace('{{n}}',r.guestName).replace('{{p}}',String(r.partySize))+(r.table?' '+localizeText('Table: {{t}}').replace('{{t}}',r.table):'')});setCode('');onDone()}
  catch(e){setMsg({ok:false,text:e instanceof Error?e.message:'Could not check in'})}finally{setTimeout(()=>{busy.current=false},1500)}}
 useEffect(()=>{if(!camera)return;let stream:MediaStream|null=null,timer:ReturnType<typeof setInterval>|null=null,stopped=false;
  const Ctor=(window as unknown as {BarcodeDetector:new(o:{formats:string[]})=>Detector}).BarcodeDetector;const detector=new Ctor({formats:['qr_code']});
  navigator.mediaDevices.getUserMedia({video:{facingMode:'environment'}}).then(s=>{if(stopped){s.getTracks().forEach(t=>t.stop());return}stream=s;if(video.current){video.current.srcObject=s;void video.current.play()}
   timer=setInterval(async()=>{if(!video.current||busy.current)return;try{const found=await detector.detect(video.current);if(found[0])void submit(found[0].rawValue)}catch{}},400)}).catch(()=>{setMsg({ok:false,text:'Camera not available. Type the code instead.'});setCamera(false)});
  return()=>{stopped=true;if(timer)clearInterval(timer);stream?.getTracks().forEach(t=>t.stop())}},[camera]);
 return <section className="gbEntry pscan"><h2>{localizeText('Guest pass')}</h2>
  <form className="gbEntryRow" onSubmit={e=>{e.preventDefault();void submit(code)}}><label className="gbGrow">{localizeText('6-letter code')}<input value={code} maxLength={60} autoCapitalize="characters" onChange={e=>setCode(e.target.value)} placeholder="K7PX2Q"/></label><button className="focusPrimary">{localizeText('Check in')}</button></form>
  {canScan&&<button type="button" className="focusSecondary" onClick={()=>setCamera(c=>!c)}>{camera?localizeText('Stop camera'):localizeText('Scan a pass with the camera')}</button>}
  {camera&&<video ref={video} className="pscanVideo" muted playsInline aria-label={localizeText('Camera preview')}/>}
  {msg&&<p className={msg.ok?'gbNotice':'focusError'} role="status">{localizeText(msg.text)}</p>}</section>;
}
