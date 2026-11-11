'use client';
import {useCallback,useEffect,useState} from 'react';
import {useParams} from 'next/navigation';
import {localizeText} from '../../../../lib/localization.mjs';
import {useLocalizationRender} from '../../../../components/LocalizationProvider';
import {hostRequest} from '../../../../lib/host-api.mjs';
import {inr} from '../../../../lib/focused.mjs';
import {functionWhen} from '../../../../lib/functions.mjs';
import './memory-book.css';

type Book={title:string;hostName:string|null;occasion:string;eventDate:string|null;description:string|null;functions:{name:string;date:string|null;start:string|null;venue:string|null}[];
 wishes:{id:string;guestName:string;message:string;photoUrl:string|null}[];guestsAttending:number;rsvpResponses:number;givers:{name:string;amountPaise:number|null;item:string|null}[];amountsIncluded:boolean};
const safePhoto=(u:string|null)=>u&&/^\/backend\/api\/v1\/events\/[0-9a-f-]{36}\/wishes\/[0-9a-f-]{36}\/photo$/i.test(u)?u:null;

/** Print-ready keepsake (A4). "Print or save as PDF" uses the browser's own PDF export. */
export default function MemoryBook(){useLocalizationRender();
 const {id}=useParams();const eventId=String(id??'');
 const [book,setBook]=useState<Book|null>(null),[amounts,setAmounts]=useState(false),[showGivers,setShowGivers]=useState(true),[showPhotos,setShowPhotos]=useState(true),[error,setError]=useState('');
 const load=useCallback(async()=>{try{setBook(await hostRequest(fetch,`/v1/events/${eventId}/memory-book?amounts=${amounts}`) as Book);setError('')}catch(e){setError(e instanceof Error?e.message:'Could not build the book')}},[eventId,amounts]);
 useEffect(()=>{if(/^[0-9a-f-]{36}$/i.test(eventId))void load()},[eventId,load]);
 const date=book?.eventDate?new Intl.DateTimeFormat(undefined,{dateStyle:'long'}).format(new Date(`${book.eventDate}T12:00:00`)):'';
 return <main className="mbWrap">
  <div className="mbToolbar no-print"><div><h1>{localizeText('Memory book')}</h1><p>{localizeText('A keepsake of your celebration. Choose what to include, then print it or save it as a PDF.')}</p></div>
   <div className="mbOptions"><label><input type="checkbox" checked={showPhotos} onChange={e=>setShowPhotos(e.target.checked)}/>{localizeText('Photos')}</label>
    <label><input type="checkbox" checked={showGivers} onChange={e=>setShowGivers(e.target.checked)}/>{localizeText('People who gave gifts')}</label>
    <label><input type="checkbox" checked={amounts} disabled={!showGivers} onChange={e=>setAmounts(e.target.checked)}/>{localizeText('Include amounts')}</label>
    <button type="button" className="focusPrimary" disabled={!book} onClick={()=>window.print()}>{localizeText('Print or save as PDF')}</button></div></div>
  {error&&<p className="focusError no-print" role="alert">{localizeText(error)}</p>}
  {book&&<article className="mbBook">
   <section className="mbCover"><p className="mbKicker">{localizeText('Our celebration')}</p><h2>{book.title}</h2>{date&&<p className="mbDate">{date}</p>}{book.hostName&&<p className="mbHost">{localizeText('with love, {{name}}').replace('{{name}}',book.hostName)}</p>}
    {book.rsvpResponses>0&&<p className="mbStat">{localizeText('{{n}} guests celebrated with us').replace('{{n}}',String(book.guestsAttending))}</p>}</section>
   {book.description&&<section className="mbPage"><h3>{localizeText('Our story')}</h3><p className="mbStory">{book.description}</p></section>}
   {book.functions.length>0&&<section className="mbPage"><h3>{localizeText('The celebrations')}</h3><ol className="mbFns">{book.functions.map((f,i)=><li key={i}><strong>{f.name}</strong><span>{[functionWhen({functionDate:f.date,startTime:f.start}),f.venue].filter(Boolean).join(' · ')}</span></li>)}</ol></section>}
   {book.wishes.length>0&&<section className="mbPage"><h3>{localizeText('Wishes from our loved ones')}</h3><div className="mbWishes">{book.wishes.map(w=>{const p=showPhotos?safePhoto(w.photoUrl):null;return <figure key={w.id} className={p?'mbWish mbWish--photo':'mbWish'}>
    {p&&<img src={p} alt="" loading="lazy"/>}<blockquote>{w.message}</blockquote><figcaption>— {w.guestName}</figcaption></figure>})}</div></section>}
   {showGivers&&book.givers.length>0&&<section className="mbPage"><h3>{localizeText('With gratitude to')}</h3><ul className="mbGivers">{book.givers.map((g,i)=><li key={i}><span>{g.name}</span>{(g.amountPaise!=null||g.item)&&<small>{[g.amountPaise!=null&&book.amountsIncluded?inr(g.amountPaise):null,g.item].filter(Boolean).join(' · ')}</small>}</li>)}</ul></section>}
   <footer className="mbEnd"><p>{localizeText('Made with GiftWise')}</p></footer>
  </article>}
 </main>;
}
