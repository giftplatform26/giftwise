'use client';
/* localization:instrumented */
import {activeLocale,localizeText} from '../lib/localization.mjs';
import {useLocalizationRender} from './LocalizationProvider';
import {useEffect,useState} from 'react';
import {hostRequest} from '../lib/host-api.mjs';
import {upiStatusCopy} from '../lib/upi-intent.mjs';

type HistoryEntry={previousStatus:string|null;newStatus:string;actorType:string;evidence:string|null;at:string};
type Details={claimId:string;status:string;reportedVia:string;amountPaise:number;paymentCode:string|null;referenceTail:string|null;transactionRef:string|null;
  platform:string|null;attemptCreatedAt:string|null;hasScreenshot:boolean;note:string;history:HistoryEntry[];
  appResponse:{status:string;responseCode:string|null;txnId:string|null;approvalRef:string|null;amountPaise:number|null;amountMismatch:boolean;source:string|null;receivedAt:string|null}|null};

const ACTORS:Record<string,string>={GUEST:'Guest',HOST:'You',SYSTEM:'GiftWise',PLATFORM_VERIFIER:'Independent verification'};
const EVIDENCE:Record<string,string>={START:'Opened UPI payment',UPI_APP_RESPONSE:'UPI app response (unverified)',GUEST_REPORT:'Guest update',GUEST_UTR:'Guest entered UPI reference',
  UPI_APP_RESPONSE_UTR:'Reference from UPI app (unverified)',HOST_DECISION:'Your decision',ATTEMPT_EXPIRED:'Payment link expired'};
const money=(paise:number)=>(paise/100).toLocaleString(activeLocale('en-IN'),{style:'currency',currency:'INR'});

/** Loads the attempt, untrusted UPI app response, screenshot and status history for one guest payment claim. */
export default function HostUpiClaimDetails({eventId,claimId}:{eventId:string;claimId:string}){useLocalizationRender();
  const [details,setDetails]=useState<Details|null>(null),[error,setError]=useState(''),[image,setImage]=useState('');
  useEffect(()=>{let active=true;hostRequest(fetch,`/v1/events/${eventId}/transactions/claims/${claimId}`).then((d:Details)=>{if(active)setDetails(d)}).catch((e:unknown)=>{if(active)setError(e instanceof Error?e.message:'Could not load details')});return()=>{active=false}},[eventId,claimId]);
  useEffect(()=>()=>{if(image)URL.revokeObjectURL(image)},[image]);
  async function showScreenshot(){try{const r=await fetch(`/backend/api/v1/events/${encodeURIComponent(eventId)}/transactions/claims/${encodeURIComponent(claimId)}/evidence`,{credentials:'include',cache:'no-store'});
    if(!r.ok||r.headers.get('content-type')!=='image/jpeg')throw Error();setImage(URL.createObjectURL(await r.blob()))}catch{setError('Could not load the screenshot')}}
  if(error)return <p role="alert">{localizeText(error)}</p>;
  if(!details)return <p role="status">{localizeText('Loading payment details…')}</p>;
  const r=details.appResponse;
  return <div className="hostUpiClaim">
    <p className="finePrint">{localizeText(details.note)}</p>
    <dl>
      <dt>{localizeText('Reported via')}</dt><dd>{details.reportedVia==='UPI_INTENT'?localizeText('UPI app link'):localizeText('Manual report')}</dd>
      <dt>{localizeText('Status')}</dt><dd>{localizeText(upiStatusCopy(details.status).label)}</dd>
      {details.transactionRef&&<><dt>{localizeText('Payment attempt reference')}</dt><dd>{details.transactionRef}</dd></>}
      {details.referenceTail&&<><dt>{localizeText('Guest UPI reference ends')}</dt><dd>…{details.referenceTail}</dd></>}
      {r&&<><dt>{localizeText('UPI app said')}</dt><dd>{r.status}{r.responseCode?` (${r.responseCode})`:''} · {localizeText('unverified')}</dd>
        {r.approvalRef&&<><dt>{localizeText('App reference (UTR/RRN)')}</dt><dd>{r.approvalRef}</dd></>}
        {r.amountPaise!=null&&<><dt>{localizeText('App amount')}</dt><dd>{money(r.amountPaise)}{r.amountMismatch&&<strong> · {localizeText('does not match the expected amount')}</strong>}</dd></>}</>}
    </dl>
    {details.hasScreenshot&&(image?/* eslint-disable-next-line @next/next/no-img-element */<img src={image} alt={localizeText('Guest payment screenshot')} style={{maxWidth:'100%',height:'auto',borderRadius:8}}/>
      :<button type="button" onClick={()=>void showScreenshot()}>{localizeText('View guest screenshot')}</button>)}
    {details.history.length>0&&<><h4>{localizeText('History')}</h4><ol className="hostUpiHistory">{details.history.map((h,i)=><li key={i}>
      <span>{new Date(h.at).toLocaleString()}</span> · {localizeText(ACTORS[h.actorType]||h.actorType)}: {h.previousStatus?`${localizeText(upiStatusCopy(h.previousStatus).label)} → `:''}{localizeText(upiStatusCopy(h.newStatus).label)}{h.evidence&&EVIDENCE[h.evidence]?` (${localizeText(EVIDENCE[h.evidence])})`:''}
    </li>)}</ol></>}
  </div>;
}
