'use client';
import {useCallback,useEffect,useState} from 'react';
import Link from 'next/link';
import {useParams} from 'next/navigation';
import {localizeText} from '../../../lib/localization.mjs';
import {useLocalizationRender} from '../../../components/LocalizationProvider';
import {RECEIPT_STEPS,receiptStage} from '../../../lib/direct-transfer.mjs';
import {inr} from '../../../lib/focused.mjs';
import CustomReceipt,{type ReceiptDesign} from '../../../components/CustomReceipt';
import SaveToLedger from '../../../components/SaveToLedger';
import {ReceiptOffer} from '../../../components/InvitationGrowth';

type Receipt={eventTitle:string;hostName:string|null;giftName:string|null;guestName:string;amountPaise:number;method:'UPI'|'BANK';
  paymentCode:string|null;referenceTail:string|null;status:string;createdAt:string;hostDecidedAt:string|null;invitationPath:string|null;
  receiptNumber:string|null;eventDate:string|null;design:ReceiptDesign|null};
const when=(value:string|null)=>value?new Intl.DateTimeFormat(undefined,{dateStyle:'medium',timeStyle:'short'}).format(new Date(value)):'';

export default function ReceiptClient(){useLocalizationRender();
  const {token}=useParams();const id=String(token??'');
  const [receipt,setReceipt]=useState<Receipt|null>(null),[error,setError]=useState(''),[notice,setNotice]=useState('');
  const load=useCallback(async()=>{
    if(!/^[0-9a-f-]{36}$/i.test(id)){setError('This receipt link is not valid.');return}
    try{const r=await fetch(`/backend/api/public/payments/direct/receipts/${encodeURIComponent(id)}`,{cache:'no-store'});
      if(r.status===404)throw Error('We could not find this receipt. Check the link and try again.');
      if(!r.ok)throw Error('We could not load your receipt right now. Please try again.');
      setReceipt(await r.json());setError('')}catch(e){setError(e instanceof Error?e.message:'We could not load your receipt.')}
  },[id]);
  useEffect(()=>{void load()},[load]);
  // While the host has not decided, refresh quietly so the guest sees the confirmation arrive.
  useEffect(()=>{if(!receipt||receipt.status!=='AWAITING_VERIFICATION')return;
    const timer=setInterval(()=>{if(document.visibilityState==='visible')void load()},30000);
    const visible=()=>{if(document.visibilityState==='visible')void load()};document.addEventListener('visibilitychange',visible);
    return()=>{clearInterval(timer);document.removeEventListener('visibilitychange',visible)}},[receipt,load]);
  async function copyLink(){try{await navigator.clipboard.writeText(window.location.href);setNotice(localizeText('Receipt link copied.'))}catch{setNotice(localizeText('Copy the address from your browser to keep this receipt.'))}}

  if(!receipt)return <main className="receiptPage"><section className="receiptCard"><p className="receiptEyebrow">{localizeText('Gift receipt')}</p><h1>{error?localizeText('Receipt unavailable'):localizeText('Opening your receipt…')}</h1>{error&&<p role="alert">{localizeText(error)}</p>}</section></main>;
  const stage=receiptStage(receipt.status);
  const custom=receipt.design;
  return <main className="receiptPage"><div className="receiptStack">{custom&&<CustomReceipt design={custom} facts={receipt}/>}<section className={`receiptCard${custom?' receiptCard--companion':''}`} aria-labelledby="receipt-title">
    {!custom&&<><p className="receiptEyebrow">{localizeText('Gift receipt')}</p>
    <h1 id="receipt-title">{inr(receipt.amountPaise)}</h1>
    <p className="receiptFor">{receipt.giftName?<>{localizeText('Toward')} <strong>{receipt.giftName}</strong> · </>:null}{receipt.eventTitle}</p></>}
    {custom&&<h2 id="receipt-title" className="receiptCompanionTitle">{localizeText('Payment status')}</h2>}
    <p className={`receiptBadge receiptBadge--${stage.tone}`} role="status">{localizeText(stage.label)}</p>
    <ol className="receiptSteps" aria-label={localizeText('Progress')}>{RECEIPT_STEPS.map((step,i)=>{const done=i<stage.step||(i===stage.step&&stage.tone==='success');const current=i===stage.step&&stage.tone!=='success';
      return <li key={step.key} className={done?'is-done':current?`is-current is-${stage.tone}`:''} aria-current={current?'step':undefined}><span aria-hidden="true">{done?'✓':i+1}</span><div><strong>{i===2&&stage.tone==='warning'?localizeText('Not confirmed yet'):localizeText(step.label)}</strong><small>{i===0?when(receipt.createdAt):i===2&&receipt.hostDecidedAt?when(receipt.hostDecidedAt):''}</small></div></li>})}</ol>
    {stage.tone==='warning'&&<p className="receiptHelp">{localizeText('The host could not match this transfer in their account yet. Check the amount and reference in your UPI or bank app, then contact the host with your payment code.')}</p>}
    {!custom&&<dl className="receiptDetails">
      {receipt.receiptNumber&&<div><dt>{localizeText('Receipt no.')}</dt><dd><strong>{receipt.receiptNumber}</strong></dd></div>}
      <div><dt>{localizeText('From')}</dt><dd>{receipt.guestName}</dd></div>
      {receipt.hostName&&<div><dt>{localizeText('To')}</dt><dd>{receipt.hostName}</dd></div>}
      <div><dt>{localizeText('Paid with')}</dt><dd>{receipt.method==='UPI'?localizeText('UPI · directly to the host'):localizeText('Bank transfer · directly to the host')}</dd></div>
      {receipt.paymentCode&&<div><dt>{localizeText('Payment code')}</dt><dd><strong>{receipt.paymentCode}</strong></dd></div>}
      {receipt.referenceTail&&<div><dt>{localizeText('Your reference')}</dt><dd>…{receipt.referenceTail}</dd></div>}
      <div><dt>{localizeText('Reported')}</dt><dd>{when(receipt.createdAt)}</dd></div>
    </dl>}
    <div className="receiptActions">
      <button type="button" className="focusPrimary" onClick={()=>void copyLink()}>{localizeText('Copy receipt link')}</button>
      <SaveToLedger receiptToken={id}/><button type="button" className="focusSecondary" onClick={()=>window.print()}>{localizeText('Print or save as PDF')}</button>
      {receipt.invitationPath&&<Link className="focusTextLink" href={receipt.invitationPath}>{localizeText('Back to the invitation')}</Link>}
    </div>
    {notice&&<p role="status">{notice}</p>}
    <ReceiptOffer invitationPath={receipt.invitationPath}/>
    {!custom&&<p className="receiptFine">{localizeText('Money was sent directly from your account to the host. GiftWise does not hold or process this payment; the host confirms when it arrives.')}</p>}
  </section></div></main>;
}
