'use client';
import {useEffect,useState} from 'react';
import {useParams} from 'next/navigation';
import {localizeText} from '../../../../../lib/localization.mjs';
import {useLocalizationRender} from '../../../../../components/LocalizationProvider';
import {hostRequest} from '../../../../../lib/host-api.mjs';
import {useHostWorkspace} from '../../../../../components/HostWorkspace';
import '../../../../../components/gift-books.css';
import '../../../../../components/seating.css';

type Pass={id:string;guestName:string;partySize:number;token:string;shortCode:string;revoked:boolean};

/** Every active pass on one printable sheet — cut along the dashed lines and hand them out or put them in invitation envelopes. */
export default function PrintPasses(){useLocalizationRender();
 const {id}=useParams();const eventId=String(id??'');const {event}=useHostWorkspace();
 const [passes,setPasses]=useState<Pass[]|null>(null),[error,setError]=useState('');
 useEffect(()=>{if(/^[0-9a-f-]{36}$/i.test(eventId))hostRequest(fetch,`/v1/events/${eventId}/passes`).then((d:{passes:Pass[]})=>setPasses(d.passes.filter(p=>!p.revoked))).catch(e=>setError(e instanceof Error?e.message:'Could not load passes'))},[eventId]);
 return <main className="focusPage gbPage">
  <header className="focusPageHeading no-print"><div><p className="focusEyebrow">{localizeText('Guest passes')}</p><h1>{localizeText('Print all passes')}</h1>
   <p>{localizeText('One card per family with their QR code and 6-letter code. The invitation must be published for the QR codes to show.')}</p></div>
   <button type="button" className="focusPrimary" onClick={()=>window.print()} disabled={!passes?.length}>{localizeText('Print')}</button></header>
  {error&&<p className="focusError" role="alert">{localizeText(error)}</p>}
  {passes&&passes.length===0&&<p className="gbEmpty">{localizeText('No passes yet. Create them on the Guest passes page first.')}</p>}
  <section className="psSheet">{passes?.map(p=><article key={p.id} className="psSheetCard">
   {event?.title&&<p className="psKicker">{event.title}</p>}
   <img src={`/backend/api/public/passes/${p.token}/qr.png`} alt={localizeText('Entry QR code for {{n}}').replace('{{n}}',p.guestName)} width={150} height={150} loading="lazy"/>
   <h3>{p.guestName}</h3><p>{localizeText('{{n}} people').replace('{{n}}',String(p.partySize))}</p><p>{localizeText('Code')}: <b className="psCode">{p.shortCode}</b></p></article>)}</section>
 </main>;
}
