'use client';
import {useEffect,useState} from 'react';
import {useParams,useRouter} from 'next/navigation';
import Link from 'next/link';
import {localizeText} from '../../../lib/localization.mjs';
import {useLocalizationRender} from '../../../components/LocalizationProvider';
import {hostRequest} from '../../../lib/host-api.mjs';
import '../../../components/gift-books.css';
import '../../../components/functions.css';

type Preview={eventTitle:string;eventDate:string|null;partnerName:string;status:string};

/** Client side of a partner handover: accept to become the owner of the celebration. */
export default function AcceptHandover(){useLocalizationRender();
 const {token}=useParams();const t=String(token??'');const router=useRouter();
 const [p,setP]=useState<Preview|null>(null),[error,setError]=useState(''),[signedOut,setSignedOut]=useState(false),[busy,setBusy]=useState(false);
 useEffect(()=>{if(!/^[A-Za-z0-9_-]{32}$/.test(t)){setError('This link is not valid.');return}
  fetch(`/backend/api/v1/me/partner-handover/${t}`,{credentials:'include',cache:'no-store'}).then(async r=>{if(r.status===401||r.status===403){setSignedOut(true);return}
   const b=await r.json().catch(()=>({}));if(!r.ok)throw Error(b.error||'This link is not valid.');setP(b)}).catch(e=>setError(e.message))},[t]);
 async function accept(){setBusy(true);setError('');try{const r=await hostRequest(fetch,`/v1/me/partner-handover/${t}/accept`,{method:'POST'}) as {eventId:string};router.replace(`/events/${r.eventId}/receiving-accounts`)}catch(e){setError(e instanceof Error?e.message:'Could not accept');setBusy(false)}}
 const next=encodeURIComponent(`/handover/${t}`);
 return <main className="focusPage gbPage jtPage"><section className="gbEntry jtCard"><p className="focusEyebrow">{localizeText('Your celebration is ready')}</p>
  {signedOut?<><h1>{localizeText('Sign in to accept')}</h1><p>{localizeText('Create a free GiftWise account or sign in, then come back to this link.')}</p>
   <div className="gbEntryActions"><Link className="focusPrimary" href={`/register?next=${next}`}>{localizeText('Create account')}</Link><Link className="focusTextLink" href={`/login?next=${next}`}>{localizeText('Sign in')}</Link></div></>:
  error?<><h1>{localizeText('Link unavailable')}</h1><p role="alert">{localizeText(error)}</p></>:!p?<p role="status">{localizeText('Opening…')}</p>:
  <><h1>{p.eventTitle}</h1><p>{localizeText('{{partner}} has set up this celebration for you.').replace('{{partner}}',p.partnerName)}</p>
   <ul className="jtList"><li>{localizeText('It becomes yours: you control the invitation, guests and gifts.')}</li><li>{localizeText('Gifts go to your own UPI or bank account, which you add next.')}</li><li>{localizeText('{{partner}} stays on to help as your planner. You can remove them any time.').replace('{{partner}}',p.partnerName)}</li></ul>
   {p.status==='OPEN'?<div className="gbEntryActions"><button type="button" className="focusPrimary" disabled={busy} onClick={()=>void accept()}>{busy?localizeText('Accepting…'):localizeText('Accept my celebration')}</button></div>:
    <p className="gbQueued">{localizeText(p.status==='USED'?'This celebration has already been handed over.':'This link has expired. Ask your planner for a new one.')}</p>}</>}
 </section></main>;
}
