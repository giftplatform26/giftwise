'use client';
import {useEffect,useState} from 'react';
import {useParams} from 'next/navigation';
import Link from 'next/link';
import {localizeText} from '../../../lib/localization.mjs';
import {useLocalizationRender} from '../../../components/LocalizationProvider';
import {eventDate} from '../../../lib/focused.mjs';
import '../../../components/seating.css';

type Pass={eventTitle:string;eventDate:string|null;hostName:string|null;guestName:string;partySize:number;shortCode:string;table:string|null;invitationPath:string};

/** The guest's own entry pass: show the QR (or read out the code) at the entrance. */
export default function GuestPass(){useLocalizationRender();
 const {token}=useParams();const t=String(token??'');
 const [p,setP]=useState<Pass|null>(null),[gone,setGone]=useState(false);
 useEffect(()=>{if(!/^[0-9a-f-]{36}$/i.test(t)){setGone(true);return}fetch(`/backend/api/public/passes/${t}`,{cache:'no-store'}).then(r=>{if(!r.ok){setGone(true);return null}return r.json()}).then(v=>v&&setP(v)).catch(()=>setGone(true))},[t]);
 if(gone)return <main className="psPage"><section className="psCard"><h1>{localizeText('This pass is not valid')}</h1><p>{localizeText('Ask the host for a new pass.')}</p></section></main>;
 if(!p)return <main className="psPage"><p role="status">{localizeText('Opening your pass…')}</p></main>;
 return <main className="psPage"><section className="psCard"><p className="psKicker">{localizeText('Entry pass')}</p><h1>{p.eventTitle}</h1><p className="psMeta">{eventDate(p.eventDate)}{p.hostName?` · ${p.hostName}`:''}</p>
  <img className="psQr" src={`/backend/api/public/passes/${t}/qr.png`} alt={localizeText('Your entry QR code')} width={240} height={240}/>
  <p className="psGuest"><strong>{p.guestName}</strong> · {localizeText('{{n}} people').replace('{{n}}',String(p.partySize))}</p>
  <p className="psCodeLine">{localizeText('Code')}: <b className="psCode">{p.shortCode}</b></p>
  {p.table&&<p className="psTable">{localizeText('Your table')}: <strong>{p.table}</strong></p>}
  <p className="psHint">{localizeText('Show this at the entrance. Tip: take a screenshot in case the venue has no signal.')}</p>
  <Link href={p.invitationPath}>{localizeText('Open the invitation')}</Link></section></main>;
}
