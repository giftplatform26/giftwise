'use client';
import {useEffect,useState} from 'react';
import GuestVerifiedDestination from './GuestVerifiedDestination';
type Payment={amount_paise:number;claim_id?:string;payment_status?:string};
type Call=(path:string,body?:unknown)=>Promise<any>;
export default function IndividualHostContribution({token,giftId,call,onBusyChange,onReported}:{token:string;giftId:string;call:Call;onBusyChange:(busy:boolean)=>void;onReported:()=>void}){
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
 const amount=payment?new Intl.NumberFormat('en-IN',{style:'currency',currency:'INR'}).format(Number(payment.amount_paise)/100):'';
 return <section className="gc-panel"><h3>Send your gift to the host with love ♡</h3>
 {loading?<p role="status">Loading your contribution…</p>:payment?.claim_id?<div role="status"><h4>Thank you for your lovely gift ♡</h4><p>{amount} · {payment.payment_status==='AWAITING_VERIFICATION'?'Transfer reported, awaiting verification':payment.payment_status?.toLowerCase().replaceAll('_',' ')}</p><p>Your reference has been shared with the host. {payment.payment_status==='AWAITING_VERIFICATION'?'Please keep your payment receipt.':''}</p></div>:payment?<form className="gc-form" onSubmit={submit}><p>Your contribution: <strong>{amount}</strong></p><p>Send this amount directly to the host’s account below. Return here after paying to share your transaction reference.</p>
 <fieldset disabled={busy}><legend>Pay the host</legend><label>Payment method<select value={method} onChange={e=>{setMethod(e.target.value as 'upi'|'bank');setDestination('');setReference('')}}><option value="upi">UPI / QR</option><option value="bank">Bank transfer</option></select></label><GuestVerifiedDestination inlineQr token={token} method={method} amountPaise={Number(payment.amount_paise)} destinationId={destination} onSelect={setDestination}/></fieldset>
 <label>UPI transaction ID / Bank UTR<input required disabled={busy} minLength={8} maxLength={40} pattern="[A-Za-z0-9\-]{8,40}" placeholder="Enter the reference after paying" value={reference} onChange={e=>setReference(e.target.value)}/><small>8–40 letters, numbers or hyphens.</small></label>
 <button className="gc-launch" disabled={busy||!destination}>{busy?'Sharing payment details…':'I’ve paid — share details with the host'}</button><small>Receipt of funds is confirmed separately. This step reports your transfer.</small></form>:<button type="button" className="gc-secondary" onClick={()=>setRetry(v=>v+1)}>Try again</button>}
 {error&&<p role="alert" className="gc-error">{error}</p>}</section>;
}
