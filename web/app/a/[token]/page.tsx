'use client';
import {useCallback,useEffect,useRef,useState} from 'react';
import {useParams} from 'next/navigation';
import Link from 'next/link';
import {localizeText} from '../../../lib/localization.mjs';
import {useLocalizationRender} from '../../../components/LocalizationProvider';
import {eventDate} from '../../../lib/focused.mjs';
import {MAX_UPLOAD_BYTES,shrinkPhoto} from '../../../lib/shared-album.mjs';
import '../../../components/guest-forms.css';

type Photo={id:string;uploaderName:string|null;caption:string|null;createdAt:string};
type Album={eventTitle:string;eventDate:string|null;hostName:string|null;uploadsOpen:boolean;guestsCanView:boolean;reviewed:boolean;photos:Photo[];total:number;hasMore:boolean;invitationPath:string};

/** Opened by scanning the album QR: guests add photos (several at once) and browse everyone's photos. No account needed. */
export default function GuestAlbum(){useLocalizationRender();
 const {token}=useParams();const t=String(token??'');const base=`/backend/api/public/albums/${t}`;
 const [a,setA]=useState<Album|null>(null),[photos,setPhotos]=useState<Photo[]>([]),[gone,setGone]=useState(false),[name,setName]=useState(''),[progress,setProgress]=useState<{done:number;total:number}|null>(null);
 const [message,setMessage]=useState(''),[error,setError]=useState(''),[view,setView]=useState<Photo|null>(null);const input=useRef<HTMLInputElement>(null);
 const load=useCallback(async(offset=0)=>{try{const r=await fetch(`${base}?offset=${offset}`,{cache:'no-store'});if(!r.ok){setGone(true);return}const j=await r.json() as Album;setA(j);setPhotos(p=>offset?[...p,...j.photos]:j.photos)}catch{setGone(true)}},[base]);
 useEffect(()=>{if(!/^[0-9a-f-]{36}$/i.test(t)){setGone(true);return}try{setName(localStorage.getItem('gift.album.name')||'')}catch{}void load()},[t,load]);
 async function upload(files:FileList|null){if(!files?.length)return;setError('');setMessage('');const list=Array.from(files).slice(0,30);setProgress({done:0,total:list.length});let ok=0,last='';const fails:string[]=[];
  try{localStorage.setItem('gift.album.name',name)}catch{}
  for(const file of list){try{const small=await shrinkPhoto(file);if(small.size>MAX_UPLOAD_BYTES)throw new Error('This photo is too large. Try a smaller one.');
    const form=new FormData();form.append('photo',small);if(name.trim())form.append('name',name.trim());
    const r=await fetch(`${base}/photos`,{method:'POST',body:form});const j=await r.json().catch(()=>({}));if(!r.ok)throw new Error(j.error||(r.status===413?'This photo is too large. Try a smaller one.':'Upload failed'));ok++;last=j.message||''}
   catch(e){fails.push(`${file.name}: ${e instanceof Error?localizeText(e.message):''}`)}setProgress(p=>p&&{...p,done:p.done+1})}
  setProgress(null);if(input.current)input.current.value='';
  if(ok)setMessage(ok===1?localizeText(last):localizeText('{{n}} photos added. Thank you!').replace('{{n}}',String(ok))+(a?.reviewed?' '+localizeText('They will appear once the host approves them.'):''));
  if(fails.length)setError(fails.join(' · '));await load()}
 if(gone)return <main className="gfPage"><section className="gfCard"><h1>{localizeText('This album is not available')}</h1><p>{localizeText('The QR code may be old. Ask the host for the latest one.')}</p></section></main>;
 if(!a)return <main className="gfPage"><p role="status">{localizeText('Opening the album…')}</p></main>;
 return <main className="gfPage"><section className="gfCard gfCard--wide">
  <p className="gfKicker">{localizeText('Shared photo album')}</p><h1>{a.eventTitle}</h1><p className="gfMeta">{eventDate(a.eventDate)}{a.hostName?` · ${a.hostName}`:''}</p>
  {a.uploadsOpen?<div className="gfDrop"><strong>{localizeText('Add your photos')}</strong>
   <label style={{width:'min(100%,320px)'}}>{localizeText('Your name (optional)')}<input value={name} maxLength={80} onChange={e=>setName(e.target.value)} placeholder={localizeText('So the hosts know who took them')}/></label>
   <input ref={input} type="file" accept="image/*" multiple hidden onChange={e=>void upload(e.target.files)}/>
   <button type="button" className="gfButton" disabled={!!progress} onClick={()=>input.current?.click()}>{progress?localizeText('Uploading {{d}} of {{t}}…').replace('{{d}}',String(Math.min(progress.done+1,progress.total))).replace('{{t}}',String(progress.total)):localizeText('Choose photos')}</button>
   {progress&&<div className="gfProgress" style={{width:'min(100%,320px)'}} aria-hidden="true"><span style={{width:`${progress.done/progress.total*100}%`}}/></div>}
   <p className="gfFine">{localizeText('Up to 30 at a time. Location data is removed from your photos.')}{a.reviewed?' '+localizeText('The hosts approve photos before they appear.'):''}</p></div>
   :<p className="gfMeta">{localizeText('The hosts have closed the album to new photos.')}</p>}
  {message&&<p className="gfOk" role="status">{message}</p>}{error&&<p className="gfErr" role="alert">{error}</p>}
  {a.guestsCanView?<>{photos.length===0?<p className="gfFine">{localizeText('No photos yet — be the first!')}</p>:<><h2>{localizeText('{{n}} photos').replace('{{n}}',String(a.total))}</h2>
   <div className="gfGrid">{photos.map(p=><button key={p.id} type="button" onClick={()=>setView(p)} aria-label={localizeText('Open photo')}><img src={`${base}/photos/${p.id}/thumb`} alt={p.caption||''} loading="lazy"/></button>)}</div>
   {a.hasMore&&<button type="button" className="gfButton gfButton--ghost" onClick={()=>void load(photos.length)}>{localizeText('Show more')}</button>}</>}</>
   :<p className="gfFine">{localizeText('Your photos go straight to the hosts.')}</p>}
  <Link className="gfFine" href={a.invitationPath}>{localizeText('Open the invitation')}</Link></section>
  {view&&<div className="gfLightbox" role="dialog" aria-modal="true" onClick={()=>setView(null)}><button type="button" aria-label={localizeText('Close')} onClick={()=>setView(null)}>✕</button>
   <figure onClick={e=>e.stopPropagation()}><img src={`${base}/photos/${view.id}`} alt={view.caption||''}/><figcaption>{[view.caption,view.uploaderName].filter(Boolean).join(' — ')}</figcaption></figure></div>}
 </main>;
}
