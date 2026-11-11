'use client';
import {useCallback,useEffect,useState} from 'react';
import {useParams} from 'next/navigation';
import Link from 'next/link';
import {localizeText} from '../../../../lib/localization.mjs';
import {useLocalizationRender} from '../../../../components/LocalizationProvider';
import {hostRequest} from '../../../../lib/host-api.mjs';
import {WALL_THEMES} from '../../../../lib/register.mjs';
import '../../../../components/gift-books.css';

type Settings={active:boolean;headline:string|null;theme:string;showMessages:boolean;showPhotos:boolean;showJoinQr:boolean;slideSeconds:number;wallToken:string|null;guestbookEnabled:boolean;published:boolean;version:number};

export default function LiveWallSettings(){useLocalizationRender();
 const {id}=useParams();const eventId=String(id??'');const route=`/v1/events/${eventId}/live-wall`;
 const [s,setS]=useState<Settings|null>(null),[saved,setSaved]=useState(''),[busy,setBusy]=useState(false),[error,setError]=useState(''),[notice,setNotice]=useState('');
 const load=useCallback(async()=>{try{const v=await hostRequest(fetch,route) as Settings;setS(v);setSaved(JSON.stringify(v));setError('')}catch(e){setError(e instanceof Error?e.message:'Could not load the wall')}},[route]);
 useEffect(()=>{if(/^[0-9a-f-]{36}$/i.test(eventId))void load()},[eventId,load]);
 const dirty=s!==null&&JSON.stringify(s)!==saved;
 const set=<K extends keyof Settings>(k:K,v:Settings[K])=>{setNotice('');setS(x=>x&&{...x,[k]:v})};
 async function save(next?:Partial<Settings>){if(!s)return;const body={...s,...next};setBusy(true);setError('');
  try{const v=await hostRequest(fetch,route,{method:'PUT',payload:{active:body.active,headline:body.headline,theme:body.theme,showMessages:body.showMessages,showPhotos:body.showPhotos,showJoinQr:body.showJoinQr,slideSeconds:body.slideSeconds,version:body.version}}) as Settings;
   setS(v);setSaved(JSON.stringify(v));setNotice(v.active?'Saved. Your wall is live.':'Saved. The wall is paused; the screen shows nothing until you start it.')}
  catch(e){setError(e instanceof Error?e.message:'Could not save')}finally{setBusy(false)}}
 async function rotate(){if(!confirm(localizeText('Create a new wall link? Screens using the old link will stop showing the wall.')))return;setBusy(true);
  try{const v=await hostRequest(fetch,`${route}/rotate`,{method:'POST'}) as Settings;setS(v);setSaved(JSON.stringify(v));setNotice('New link created. Open it on the screen again.')}catch(e){setError(e instanceof Error?e.message:'Could not create a new link')}finally{setBusy(false)}}
 const wallUrl=s?.wallToken&&typeof window!=='undefined'?`${window.location.origin}/live/${s.wallToken}`:'';
 async function copy(){try{await navigator.clipboard.writeText(wallUrl);setNotice('Wall link copied.')}catch{setNotice('Select the link and copy it.')}}

 return <main className="focusPage gbPage">
  <header className="focusPageHeading"><div><p className="focusEyebrow">{localizeText('At the venue')}</p><h1>{localizeText('Live venue wall')}</h1>
   <p>{localizeText('Put your guests’ wishes and photos on a TV or projector. New wishes slide in as soon as you approve them.')}</p></div></header>
  {error&&<p className="focusError" role="alert">{localizeText(error)}{/another tab/i.test(error)&&<> <button type="button" onClick={()=>void load()}>{localizeText('Reload')}</button></>}</p>}
  {notice&&<p className="gbNotice" role="status">{localizeText(notice)}</p>}
  {s&&!s.guestbookEnabled&&<p className="gbQueued">{localizeText('The wall shows guestbook wishes, and the guestbook is not available on your account. Ask GiftWise support to enable it.')}</p>}
  {s&&!s.published&&<p className="gbQueued">{localizeText('Publish your invitation first. The wall only plays for published celebrations.')}</p>}
  {!s&&!error&&<div className="focusSkeleton" role="status">{localizeText('Loading…')}</div>}
  {s&&<div className="lwGrid">
   <section className={`lwStatus${s.active?' is-live':''}`} aria-live="polite">
    <span className="lwDot" aria-hidden="true"/><div><strong>{s.active?localizeText('Wall is live'):localizeText('Wall is paused')}</strong><small>{s.active?localizeText('Anyone with the wall link sees approved wishes.'):localizeText('Start it when the celebration begins.')}</small></div>
    <button type="button" className={s.active?'focusSecondary':'focusPrimary'} disabled={busy||!s.guestbookEnabled||!s.published} onClick={()=>void save({active:!s.active})}>{s.active?localizeText('Pause wall'):localizeText('Start wall')}</button>
   </section>
   {s.wallToken&&<section className="focusPanel lwLink"><h2>{localizeText('Open on the big screen')}</h2>
    <p>{localizeText('Open this link in the browser of a smart TV or a laptop connected to the projector, then press F for full screen.')}</p>
    <div className="lwCopy"><input readOnly value={wallUrl} onFocus={e=>e.currentTarget.select()} aria-label={localizeText('Wall link')}/><button type="button" className="focusSecondary" onClick={()=>void copy()}>{localizeText('Copy')}</button>
     <a className="focusSecondary gbButtonLink" href={wallUrl} target="_blank" rel="noopener noreferrer">{localizeText('Open wall')}</a></div>
    <button type="button" className="focusTextLink gbPlain" disabled={busy} onClick={()=>void rotate()}>{localizeText('Link shared by mistake? Create a new one')}</button></section>}
   <form className="focusForm focusPanel" onSubmit={e=>{e.preventDefault();void save()}}>
    <h2>{localizeText('Look and feel')}</h2>
    <label>{localizeText('Headline')}<input value={s.headline??''} maxLength={120} onChange={e=>set('headline',e.target.value)} placeholder={localizeText('e.g. Send your love to Meera & Arjun')}/></label>
    <fieldset><legend>{localizeText('Theme')}</legend><div className="lwThemes">{WALL_THEMES.map(([k,name,hint])=><label key={k} className={`lwTheme lwTheme--${k.toLowerCase()}`}><input type="radio" name="theme" checked={s.theme===k} onChange={()=>set('theme',k)}/><span><strong>{localizeText(name)}</strong><small>{localizeText(hint)}</small></span></label>)}</div></fieldset>
    <fieldset><legend>{localizeText('Show')}</legend>
     <label className="gbCheck"><input type="checkbox" checked={s.showMessages} onChange={e=>set('showMessages',e.target.checked)}/>{localizeText('Written wishes')}</label>
     <label className="gbCheck"><input type="checkbox" checked={s.showPhotos} onChange={e=>set('showPhotos',e.target.checked)}/>{localizeText('Photos')}</label>
     <label className="gbCheck"><input type="checkbox" checked={s.showJoinQr} onChange={e=>set('showJoinQr',e.target.checked)}/>{localizeText('“Scan to add your wish” QR code in the corner')}</label></fieldset>
    <label>{localizeText('Seconds per slide')}: <strong>{s.slideSeconds}</strong><input type="range" min={4} max={30} value={s.slideSeconds} onChange={e=>set('slideSeconds',Number(e.target.value))}/></label>
    <p className="gbFine">{localizeText('Only wishes you approve appear on the wall. Review new wishes in')} <Link href={`/events/${eventId}/celebration`}>{localizeText('Guests & responses')}</Link>.</p>
    <div className="gbEntryActions"><button className="focusPrimary" disabled={busy||!dirty}>{busy?localizeText('Saving…'):dirty?localizeText('Save'):localizeText('All changes saved')}</button></div>
   </form>
  </div>}
 </main>;
}
