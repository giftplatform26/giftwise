'use client';
import {useEffect,useState} from 'react';
import {useParams,useRouter} from 'next/navigation';
import Link from 'next/link';
import {localizeText} from '../../../lib/localization.mjs';
import {useLocalizationRender} from '../../../components/LocalizationProvider';
import {hostRequest} from '../../../lib/host-api.mjs';
import '../../../components/gift-books.css';

type Preview={eventTitle:string;eventDate:string|null;hostName:string|null;access:string;status:string};

/** A family member opens the budget link the host sent, signs in, and accepts it. */
export default function BudgetInvite(){useLocalizationRender();
 const {token}=useParams();const t=String(token??'');const router=useRouter();
 const [p,setP]=useState<Preview|null>(null),[error,setError]=useState(''),[busy,setBusy]=useState(false),[signedOut,setSignedOut]=useState(false);
 useEffect(()=>{if(!/^[A-Za-z0-9_-]{32}$/.test(t)){setError('This link is not valid.');return}
  fetch(`/backend/api/v1/me/budget-invites/${t}`,{credentials:'include',cache:'no-store'}).then(async r=>{if(r.status===401||r.status===403){setSignedOut(true);return}
   const body=await r.json().catch(()=>({}));if(!r.ok)throw Error(body.error||'This link is not valid.');setP(body)}).catch(e=>setError(e.message))},[t]);
 async function accept(){setBusy(true);setError('');try{const r=await hostRequest(fetch,`/v1/me/budget-invites/${t}/accept`,{method:'POST'}) as {eventId:string};router.replace(`/helping/${r.eventId}/budget`)}catch(e){setError(e instanceof Error?e.message:'Could not open the budget');setBusy(false)}}
 const next=encodeURIComponent(`/budget-invite/${t}`);
 return <main className="focusPage gbPage jtPage"><section className="gbEntry jtCard">
  <p className="focusEyebrow">{localizeText('A budget has been shared with you')}</p>
  {signedOut?<><h1>{localizeText('Sign in to open it')}</h1><p>{localizeText('Sign in or create a free GiftWise account, then come back to this link.')}</p>
   <div className="gbEntryActions"><Link className="focusPrimary" href={`/login?next=${next}`}>{localizeText('Sign in')}</Link><Link className="focusTextLink" href={`/register?next=${next}`}>{localizeText('Create an account')}</Link></div></>:
  error?<><h1>{localizeText('Link unavailable')}</h1><p role="alert">{localizeText(error)}</p></>:
  !p?<p role="status">{localizeText('Opening…')}</p>:
  <><h1>{p.eventTitle}</h1>{p.hostName&&<p>{localizeText('{{name}} has shared the celebration budget with you.').replace('{{name}}',p.hostName)}</p>}
   <p>{localizeText(p.access==='EDIT'?'You will be able to see and update the budget and vendor bookings.':'You will be able to see the budget and vendor bookings, but not change them.')}</p>
   {p.status==='OPEN'?<div className="gbEntryActions"><button type="button" className="focusPrimary" disabled={busy} onClick={()=>void accept()}>{busy?localizeText('Opening…'):localizeText('Accept')}</button></div>:
    <p className="gbQueued">{localizeText(p.status==='ACCEPTED'?'This link has already been used.':p.status==='REVOKED'?'The host cancelled this link.':'This link has expired. Ask for a new one.')}</p>}</>}
 </section></main>;
}
