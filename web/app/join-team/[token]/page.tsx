'use client';
import {useEffect,useState} from 'react';
import {useParams,useRouter} from 'next/navigation';
import Link from 'next/link';
import {localizeText} from '../../../lib/localization.mjs';
import {useLocalizationRender} from '../../../components/LocalizationProvider';
import {hostRequest} from '../../../lib/host-api.mjs';
import '../../../components/gift-books.css';

type Preview={eventTitle:string;eventDate:string|null;hostName:string|null;role:string;status:string};
const ROLE:Record<string,[string,string]>={CO_HOST:['co-host','You will be able to help with the gift register, guests and wishes, the live wall and the schedule.'],GIFT_DESK:['gift-desk helper','You will be able to add envelopes and gifts to the gift register.']};

export default function JoinTeam(){useLocalizationRender();
 const {token}=useParams();const t=String(token??'');const router=useRouter();
 const [p,setP]=useState<Preview|null>(null),[error,setError]=useState(''),[busy,setBusy]=useState(false),[signedOut,setSignedOut]=useState(false);
 useEffect(()=>{if(!/^[A-Za-z0-9_-]{32}$/.test(t)){setError('This invite link is not valid.');return}
  fetch(`/backend/api/v1/me/co-host-invites/${t}`,{credentials:'include',cache:'no-store'}).then(async r=>{if(r.status===401||r.status===403){setSignedOut(true);return}
   const body=await r.json().catch(()=>({}));if(!r.ok)throw Error(body.error||'This invite link is not valid.');setP(body)}).catch(e=>setError(e.message))},[t]);
 async function accept(){setBusy(true);setError('');try{await hostRequest(fetch,`/v1/me/co-host-invites/${t}/accept`,{method:'POST'});router.replace('/helping?joined=1')}catch(e){setError(e instanceof Error?e.message:'Could not join');setBusy(false)}}
 const next=encodeURIComponent(`/join-team/${t}`);
 return <main className="focusPage gbPage jtPage"><section className="gbEntry jtCard">
  <p className="focusEyebrow">{localizeText('You are invited to help')}</p>
  {signedOut?<><h1>{localizeText('Sign in to accept')}</h1><p>{localizeText('Sign in or create a free GiftWise account, then come back to this link.')}</p>
   <div className="gbEntryActions"><Link className="focusPrimary" href={`/login?next=${next}`}>{localizeText('Sign in')}</Link><Link className="focusTextLink" href={`/register?next=${next}`}>{localizeText('Create an account')}</Link></div></>:
  error?<><h1>{localizeText('Invite unavailable')}</h1><p role="alert">{localizeText(error)}</p></>:
  !p?<p role="status">{localizeText('Opening your invite…')}</p>:
  <><h1>{p.eventTitle}</h1>{p.hostName&&<p>{localizeText('{{name}} has asked you to be a {{role}}.').replace('{{name}}',p.hostName).replace('{{role}}',localizeText(ROLE[p.role]?.[0]??''))}</p>}
   <p>{localizeText(ROLE[p.role]?.[1]??'')}</p><p className="gbFine">{localizeText('Payments and event settings stay with the host.')}</p>
   {p.status==='OPEN'?<div className="gbEntryActions"><button type="button" className="focusPrimary" disabled={busy} onClick={()=>void accept()}>{busy?localizeText('Joining…'):localizeText('Accept and join')}</button></div>:
    <p className="gbQueued">{localizeText(p.status==='ACCEPTED'?'This invite has already been used.':p.status==='REVOKED'?'The host cancelled this invite.':'This invite has expired. Ask the host for a new link.')}</p>}</>}
 </section></main>;
}
