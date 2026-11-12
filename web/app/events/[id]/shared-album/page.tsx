'use client';
import {useCallback,useEffect,useState} from 'react';
import {useParams} from 'next/navigation';
import {localizeText} from '../../../../lib/localization.mjs';
import {useLocalizationRender} from '../../../../components/LocalizationProvider';
import {hostRequest} from '../../../../lib/host-api.mjs';
import {useHostWorkspace} from '../../../../components/HostWorkspace';
import {friendlySize} from '../../../../lib/shared-album.mjs';
import '../../../../components/gift-books.css';
import '../../../../components/guest-forms.css';

type Photo={id:string;uploaderName:string|null;caption:string|null;status:string;createdAt:string};
type Album={uploadToken:string;uploadPath:string;settings:{uploadsOpen:boolean;moderation:string;guestsCanView:boolean};photos:Photo[];approved:number;pending:number;hidden:number;bytes:number;limit:number};

/** Shared photo album: show or print the QR, guests add photos from their phones, you choose what is shown and download everything. */
export default function SharedAlbum(){useLocalizationRender();
 const {id}=useParams();const eventId=String(id??'');const route=`/v1/events/${eventId}/shared-album`;const {event}=useHostWorkspace();
 const [a,setA]=useState<Album|null>(null),[error,setError]=useState(''),[notice,setNotice]=useState(''),[filter,setFilter]=useState<'ALL'|'PENDING'|'APPROVED'|'HIDDEN'>('ALL'),[qrKey,setQrKey]=useState(0),[view,setView]=useState<Photo|null>(null);
 const load=useCallback(async()=>{try{setA(await hostRequest(fetch,route) as Album);setError('')}catch(e){setError(e instanceof Error?e.message:'Could not load the album')}},[route]);
 useEffect(()=>{if(/^[0-9a-f-]{36}$/i.test(eventId))void load()},[eventId,load]);
 const origin=typeof window!=='undefined'?window.location.origin:'';const link=a?`${origin}${a.uploadPath}`:'';
 async function call(fn:()=>Promise<unknown>,ok?:string){setError('');try{await fn();if(ok)setNotice(ok);await load()}catch(e){setError(e instanceof Error?e.message:'Something went wrong')}}
 const save=(patch:Partial<Album['settings']>)=>a&&void call(()=>hostRequest(fetch,`${route}/settings`,{method:'PUT',payload:{...a.settings,...patch}}),localizeText('Saved.'));
 const status=(p:Photo,s:string)=>void call(()=>hostRequest(fetch,`${route}/photos/${p.id}/status`,{method:'POST',payload:{status:s}}));
 const remove=(p:Photo)=>{if(confirm(localizeText('Delete this photo permanently?')))void call(async()=>{await hostRequest(fetch,`${route}/photos/${p.id}/delete`,{method:'POST'});setView(null)})};
 const shown=(a?.photos??[]).filter(p=>filter==='ALL'||p.status===filter);
 const share=`https://wa.me/?text=${encodeURIComponent(localizeText('Add your photos from {{t}} to our shared album: {{u}}').replace('{{t}}',event?.title||localizeText('the celebration')).replace('{{u}}',link))}`;

 return <main className="focusPage gbPage">
  <header className="focusPageHeading no-print"><div><p className="focusEyebrow">{localizeText('Every guest’s photos in one place')}</p><h1>{localizeText('Shared photo album')}</h1>
   <p>{localizeText('Put this QR code on tables or at the entrance. Guests scan it and add photos from their phone — no app or account needed. The invitation must be published.')}</p></div>
   {a&&a.approved>0&&<a className="focusSecondary gbButtonLink" href={`/backend/api${route}/download.zip`} download>{localizeText('Download all ({{n}})').replace('{{n}}',String(a.approved))}</a>}</header>
  {error&&<p className="focusError" role="alert">{localizeText(error)}</p>}
  {notice&&<p className="gbNotice no-print" role="status">{notice}</p>}
  {a&&<section className="saTop">
   <figure className="saQr"><img key={qrKey} src={`/backend/api${route}/qr.png?v=${qrKey}`} alt={localizeText('QR code for the shared photo album')} width={220} height={220}/>
    <figcaption><strong>{localizeText('Scan to add your photos')}</strong>{event?.title&&<span>{event.title}</span>}</figcaption></figure>
   <div className="saSide no-print">
    <p className="gfFine">{link}</p>
    <div className="gbEntryActions"><button type="button" className="focusSecondary" onClick={()=>{void navigator.clipboard?.writeText(link);setNotice(localizeText('Link copied.'))}}>{localizeText('Copy link')}</button>
     <a className="focusSecondary gbButtonLink" href={share} target="_blank" rel="noopener noreferrer">{localizeText('Share on WhatsApp')}</a>
     <button type="button" className="focusSecondary" onClick={()=>window.print()}>{localizeText('Print QR poster')}</button></div>
    <label className="gbCheck"><input type="checkbox" checked={a.settings.uploadsOpen} onChange={e=>save({uploadsOpen:e.target.checked})}/>{localizeText('Guests can add photos')}</label>
    <label className="gbCheck"><input type="checkbox" checked={a.settings.moderation==='REVIEW'} onChange={e=>save({moderation:e.target.checked?'REVIEW':'AUTO'})}/>{localizeText('I approve photos before they appear')}</label>
    <label className="gbCheck"><input type="checkbox" checked={a.settings.guestsCanView} onChange={e=>save({guestsCanView:e.target.checked})}/>{localizeText('Guests can see the album')}</label>
    <p className="gfFine">{localizeText('{{n}} of {{max}} photos · {{size}}').replace('{{n}}',String(a.photos.length)).replace('{{max}}',String(a.limit)).replace('{{size}}',friendlySize(a.bytes))}</p>
    <button type="button" className="gbPlain" onClick={()=>{if(confirm(localizeText('Make a new QR code? Old printed or shared codes will stop working.')))void call(async()=>{await hostRequest(fetch,`${route}/new-link`,{method:'POST'});setQrKey(k=>k+1)},localizeText('New QR code ready. Print it again.'))}}>{localizeText('Make a new QR code')}</button></div></section>}
  {a&&<nav className="gbTabs no-print" role="tablist">{(['ALL','PENDING','APPROVED','HIDDEN'] as const).map(k=><button key={k} type="button" role="tab" aria-selected={filter===k} onClick={()=>setFilter(k)}>
   {localizeText(k==='ALL'?'All':k==='PENDING'?'Waiting for approval':k==='APPROVED'?'Shown':'Hidden')} ({k==='ALL'?a.photos.length:k==='PENDING'?a.pending:k==='APPROVED'?a.approved:a.hidden})</button>)}</nav>}
  {a&&(shown.length===0?<p className="gbEmpty no-print">{localizeText(a.photos.length?'No photos here.':'No photos yet. Share the QR code with your guests.')}</p>:
   <div className="gfGrid saGrid no-print">{shown.map(p=><div key={p.id} className={`saItem saItem--${p.status.toLowerCase()}`}><button type="button" onClick={()=>setView(p)} aria-label={localizeText('Open photo')}><img src={`/backend/api${route}/photos/${p.id}?thumb=true`} alt={p.caption||''} loading="lazy"/></button>
    <div className="saTools">{p.status!=='APPROVED'&&<button type="button" onClick={()=>status(p,'APPROVED')}>{localizeText('Show')}</button>}{p.status!=='HIDDEN'&&<button type="button" onClick={()=>status(p,'HIDDEN')}>{localizeText('Hide')}</button>}<button type="button" onClick={()=>remove(p)}>{localizeText('Delete')}</button></div>
    {p.uploaderName&&<small>{p.uploaderName}</small>}</div>)}</div>)}
  {view&&<div className="gfLightbox" role="dialog" aria-modal="true" onClick={()=>setView(null)}><button type="button" aria-label={localizeText('Close')} onClick={()=>setView(null)}>✕</button>
   <figure onClick={e=>e.stopPropagation()}><img src={`/backend/api${route}/photos/${view.id}`} alt={view.caption||''}/><figcaption>{[view.caption,view.uploaderName].filter(Boolean).join(' — ')}</figcaption></figure></div>}
 </main>;
}
