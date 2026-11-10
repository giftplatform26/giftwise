'use client';
import {activeLocale} from '../lib/localization.mjs';

/* localization:instrumented */
import {localize,localizeText} from '../lib/localization.mjs';
import {useLocalizationRender} from './LocalizationProvider';

import {useEffect,useState} from 'react';
import GuestVerifiedDestination from './GuestVerifiedDestination';
type Payment={amount_paise:number;claim_id?:string;payment_status?:string};
type Call=(path:string,body?:unknown)=>Promise<any>;
export default function IndividualHostContribution({token,giftId,call,onBusyChange,onReported}:{token:string;giftId:string;call:Call;onBusyChange:(busy:boolean)=>void;onReported:()=>void}){useLocalizationRender();
 const [payment,setPayment]=useState<Payment|null>(null),[loading,setLoading]=useState(true),[error,setError]=useState(''),[retry,setRetry]=useState(0);
 const [method,setMethod]=useState<'upi'|'bank'>('upi'),[destination,setDestination]=useState(''),[reference,setReference]=useState(''),[busy,setBusy]=useState(false);
 const path=`/individual-entry/${encodeURIComponent(giftId)}/host-contribution`;
 useEffect(()=>{let active=true;setLoading(true);setError('');call(path).then(value=>{if(active)setPayment(value)}).catch(e=>{if(active)setError(e instanceof Error?e.message:'Could not load your contribution.')}).finally(()=>{if(active)setLoading(false)});return()=>{active=false}},[token,giftId,retry]);
 useEffect(()=>{onBusyChange(busy);return()=>onBusyChange(false)},[busy,onBusyChange]);
 async function submit(event:React.FormEvent){
  event.preventDefault();if(busy||!destination||!payment)return;setBusy(true);setError('');
  try{const result=await call(path,{destinationId:destination,method:method.toUpperCase(),transactionReference:reference.trim()});setPayment({...payment,claim_id:result.claimId,payment_status:result.status});onReported()}
  catch(e){setError(e instanceof Error?e.message:'Could not submit your transfer details. Please try again with the same reference.')}finally{setBusy(false)}
 }
 const amount=payment?new Intl.NumberFormat(activeLocale("en-IN"),{style:'currency',currency:'INR'}).format(Number(payment.amount_paise)/100):'';
 return <section className="gc-panel"><h3>{localize("ui.efd060c13e93292f",{defaultValue:"Send your gift to the host with love ♡"})}</h3>
 {loading?<p role="status">{localize("ui.0a0f81c5c16fb959",{defaultValue:"Loading your contribution…"})}</p>:payment?.claim_id?<div role="status"><h4>{localize("ui.96c6982dd838f396",{defaultValue:"Thank you for your lovely gift ♡"})}</h4><p>{amount} · {payment.payment_status==='AWAITING_VERIFICATION'?localize("ui.e21741f898549ce3",{defaultValue:"Transfer reported, awaiting verification"}):payment.payment_status?.toLowerCase().replaceAll('_',' ')}</p><p>{localize("ui.1726df7403bd3193",{defaultValue:"Your reference has been shared with the host. "})}{payment.payment_status==='AWAITING_VERIFICATION'?localize("ui.d254876c93e9457f",{defaultValue:"Please keep your payment receipt."}):''}</p></div>:payment?<form className="gc-form" onSubmit={submit}><p>{localize("ui.fb1935824352fac9",{defaultValue:"Your contribution: "})}<strong>{amount}</strong></p><p>{localize("ui.36d20756ddd1f826",{defaultValue:"Send this amount directly to the host’s account below. Return here after paying to share your transaction reference."})}</p>
 <fieldset disabled={busy}><legend>{localize("ui.290f2eea9056c3cd",{defaultValue:"Pay the host"})}</legend><label>{localize("ui.b948ac04b854394f",{defaultValue:"Payment method"})}<select value={method} onChange={e=>{setMethod(e.target.value as 'upi'|'bank');setDestination('');setReference('')}}><option value="upi">{localize("ui.0c6e457021aa1a3a",{defaultValue:"UPI / QR"})}</option><option value="bank">{localize("ui.d69d587c81659616",{defaultValue:"Bank transfer"})}</option></select></label><GuestVerifiedDestination inlineQr token={token} method={method} amountPaise={Number(payment.amount_paise)} destinationId={destination} onSelect={setDestination}/></fieldset>
 <label>{localize("ui.0c0b7cca57d23288",{defaultValue:"UPI transaction ID / Bank UTR"})}<input required disabled={busy} minLength={8} maxLength={40} pattern="[A-Za-z0-9\-]{8,40}" placeholder={localize("ui.11f7fb5414400a9b",{defaultValue:"Enter the reference after paying"})} value={reference} onChange={e=>setReference(e.target.value)}/><small>{localize("ui.684d26a6e3d66d90",{defaultValue:"8–40 letters, numbers or hyphens."})}</small></label>
 <button className="gc-launch" disabled={busy||!destination}>{busy?localize("ui.72dd4b7eecd15686",{defaultValue:"Sharing payment details…"}):localize("ui.72f61dfd8fd1ae98",{defaultValue:"I’ve paid — share details with the host"})}</button><small>{localize("ui.198fe076e7900b70",{defaultValue:"Receipt of funds is confirmed separately. This step reports your transfer."})}</small></form>:<button type="button" className="gc-secondary" onClick={()=>setRetry(v=>v+1)}>{localize("ui.d8b8392e2c542950",{defaultValue:"Try again"})}</button>}
 {error&&<p role="alert" className="gc-error">{localizeText(error)}</p>}</section>;
}
