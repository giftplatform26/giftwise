'use client';
import {recordFlow} from '../lib/diagnostics.mjs';
import {useEffect,useRef,useState} from 'react';
import {useExperience} from './ExperienceProvider';
import FocusedCheckout from './focused/Checkout';
import GuestVerifiedDestination from './GuestVerifiedDestination';

type Gift = {id:string;name:string;targetAmount:number};
type Checkout = {localOrderId:string;razorpayOrderId:string;razorpayKeyId:string;amountPaise:number;currency:string;status:string};
type Capture = {orderId:string;contributionId:string;paymentId:string;giftPaise:number;walletPaise:number;alreadyProcessed:boolean};
type RazorpayResult = {razorpay_payment_id:string;razorpay_order_id:string;razorpay_signature:string};
type RazorpayOptions = {key:string;amount:number;currency:string;name:string;description:string;order_id:string;prefill:{name:string;email:string;contact:string};handler:(result:RazorpayResult)=>void;modal:{ondismiss:()=>void}};
type RazorpayInstance = {open:()=>void;on:(event:string,cb:(error:unknown)=>void)=>void};
declare global {interface Window {Razorpay?:new (options:RazorpayOptions)=>RazorpayInstance}}
// Only the existing provider-confirmed Razorpay checkout can accept payments.
// Direct methods are available when the server exposes a host-confirmed preferred destination.
export const guestPaymentMethodChoices = [
  {id:'razorpay',label:'Razorpay Checkout',description:'Card, UPI or netbanking · verified by Razorpay (test mode)',available:true},
  {id:'upi',label:'Direct UPI / QR',description:'Pay to the host’s preferred destination',available:false},
  {id:'bank',label:'Bank Transfer',description:'Pay to the host’s preferred destination',available:false},
] as const;
const money=(paise:number)=>new Intl.NumberFormat('en-IN',{style:'currency',currency:'INR'}).format(paise/100);
async function json<T>(url:string,options?:RequestInit):Promise<T>{
  const response=await fetch(url,{credentials:'same-origin',cache:'no-store',...options});
  if(!response.ok) throw new Error(`Request failed (${response.status})`);
  return response.json() as Promise<T>;
}
async function csrfHeader(){
  await fetch('/backend/api/auth/csrf',{credentials:'same-origin',cache:'no-store'});
  const token=document.cookie.split('; ').find(c=>c.startsWith('XSRF-TOKEN='))?.split('=')[1];
  if(!token)throw new Error('Could not initialize secure checkout');
  return {'Content-Type':'application/json','X-XSRF-TOKEN':decodeURIComponent(token)};
}
function loadRazorpay():Promise<void>{
  if(window.Razorpay)return Promise.resolve();
  return new Promise((resolve,reject)=>{
    const old=document.querySelector<HTMLScriptElement>('script[data-razorpay-checkout]');
    if(old){old.addEventListener('load',()=>resolve(),{once:true});old.addEventListener('error',()=>reject(new Error('Checkout unavailable')),{once:true});return;}
    const script=document.createElement('script');script.src='https://checkout.razorpay.com/v1/checkout.js';script.async=true;
    script.dataset.razorpayCheckout='true';script.onload=()=>resolve();script.onerror=()=>reject(new Error('Checkout unavailable'));
    document.head.appendChild(script);
  });
}
export default function GuestContributionCheckout({token,gifts,selectedGiftId='',onClose,onBusyChange,onProviderVisibilityChange}:{token:string;gifts:Gift[];selectedGiftId?:string;onClose?:()=>void;onBusyChange?:(busy:boolean)=>void;onProviderVisibilityChange?:(open:boolean)=>void}){
  const {mode}=useExperience();
  const [paymentMethod,setPaymentMethod]=useState<'razorpay'|'upi'|'bank'>('razorpay');
  const [directDestinations,setDirectDestinations]=useState<Array<{id:string;method:string;masked:string;available:boolean}>>([]);
  const [directDestinationId,setDirectDestinationId]=useState('');
  const [destinationsError,setDestinationsError]=useState('');
  const [destinationsLoading,setDestinationsLoading]=useState(true);
  const [destinationRetry,setDestinationRetry]=useState(0);
  const [transferReference,setTransferReference]=useState('');
  const [directStatus,setDirectStatus]=useState('');
  const [giftId,setGiftId]=useState(selectedGiftId);const [amount,setAmount]=useState('500');
  const [name,setName]=useState('');const [email,setEmail]=useState('');const [phone,setPhone]=useState('');
  const [showContact,setShowContact]=useState(false);const [busy,setBusy]=useState(false);const [error,setError]=useState('');const [receipt,setReceipt]=useState<Capture|null>(null);
  useEffect(()=>{onBusyChange?.(busy);return()=>onBusyChange?.(false)},[busy,onBusyChange]);
  const [eventId,setEventId]=useState('');const requestKey=useRef<string>('');
  useEffect(()=>{recordFlow('guest_contribution','opened');let active=true;json<{eventId:string}>(`/backend/api/public/payments/context/${encodeURIComponent(token)}`).then(v=>{if(active)setEventId(v.eventId)}).catch(()=>{if(active)setError('Contributions are temporarily unavailable')});return()=>{active=false}},[token]);
  useEffect(()=>{let active=true;setDestinationsLoading(true);setDestinationsError('');
    // Use the same event-scoped eligibility response as the account details screen.
    json<Array<{id:string;method:string;label:string;preferred:boolean}>>(`/backend/api/public/payments/verified-destinations/${encodeURIComponent(token)}`)
      .then(rows=>{if(!Array.isArray(rows))throw Error('Invalid destination response');if(active)setDirectDestinations(rows.map(d=>({id:d.id,method:d.method,masked:d.label,available:d.preferred})))})
      .catch(()=>{if(active){setDirectDestinations([]);setDestinationsError('Could not load UPI and bank details. Please retry.')}})
      .finally(()=>{if(active)setDestinationsLoading(false)});
    return()=>{active=false};
  },[token,destinationRetry]);
  const availableDirect=directDestinations.filter(d=>d.available);
  // Direct transfers remain unavailable unless the server explicitly offers an eligible destination.
  const canUseDirect=(method:'upi'|'bank')=>availableDirect.some(d=>d.method===method.toUpperCase());
  const referenceValue=transferReference.trim();
  const referenceError=!referenceValue
    ? 'Enter the UPI transaction ID or bank UTR after completing the transfer.'
    : referenceValue.length<8
      ? 'Transaction reference is too short. Enter 8–40 characters using letters, numbers, or hyphens.'
      : referenceValue.length>40
        ? 'Transaction reference is too long. Maximum 40 characters.'
        : !/^[A-Za-z0-9-]{8,40}$/.test(referenceValue)
          ? 'Transaction reference can contain only letters, numbers, and hyphens (no spaces or symbols).'
          : '';
  async function reportDirectPayment(){
    if(!eventId||!validAmount||!name.trim()||busy||!directDestinationId||!canUseDirect(paymentMethod==='upi'?'upi':'bank')||!transferReference.trim())return;
    if(referenceError){setError(referenceError);return;}
    recordFlow('guest_contribution','started');setBusy(true);setError('');setDirectStatus('');
    try{
      if(!requestKey.current)requestKey.current=crypto.randomUUID();
      const result=await json<{claimId:string;status:string;message:string}>(`/backend/api/public/payments/direct/${encodeURIComponent(token)}/claims`,{
        method:'POST',headers:await csrfHeader(),body:JSON.stringify({destinationId:directDestinationId,
          method:paymentMethod==='upi'?'UPI':'BANK',giftId:giftId||null,amountPaise,
          guestName:name.trim(),guestEmail:email.trim()||null,guestPhone:phone.trim()||null,transactionReference:transferReference.trim(),idempotencyKey:requestKey.current})});
      recordFlow('guest_contribution','submitted_unverified');setDirectStatus(`Thank you for sharing your love! Your gift details have been submitted successfully. Reference: ${result.claimId}`);
    }catch(e){recordFlow('guest_contribution','failed');setError(e instanceof Error?e.message:'Unable to submit payment report')}
    finally{setBusy(false)}
  }
  const amountPaise=Math.round(Number(amount)*100);
  const validAmount=Number.isFinite(amountPaise)&&Number.isSafeInteger(amountPaise)&&amountPaise>=100&&amountPaise<=100000000;
  async function submit(e:React.FormEvent){
    e.preventDefault();if(paymentMethod!=='razorpay'||!validAmount||!name.trim()||!eventId||busy)return;
    recordFlow('guest_contribution','started');setBusy(true);setError('');setReceipt(null);
    try{
      const headers=await csrfHeader();
      if(!requestKey.current)requestKey.current=crypto.randomUUID();
      const order=await json<Checkout>('/backend/api/public/payments/checkout',{method:'POST',headers,body:JSON.stringify({eventId,giftId:giftId||null,contributionType:giftId?'GIFT_PARTIAL':'EVENT_WALLET',amountPaise,idempotencyKey:requestKey.current,guestName:name.trim(),guestEmail:email.trim()||null,guestPhone:phone.trim()||null})});
      if(order.currency!=='INR'||order.amountPaise!==amountPaise||!order.razorpayKeyId.startsWith('rzp_test_'))throw new Error('Invalid test checkout response');
      await loadRazorpay();
      if(!window.Razorpay)throw new Error('Razorpay checkout failed to load');
      const instance=new window.Razorpay({key:order.razorpayKeyId,amount:order.amountPaise,currency:'INR',name:'Gift Platform',description:giftId?'Gift contribution':'Event wallet contribution',order_id:order.razorpayOrderId,prefill:{name,email,contact:phone},modal:{ondismiss:()=>{onProviderVisibilityChange?.(false);recordFlow('guest_contribution','cancelled');setBusy(false)}},handler:async result=>{
        try{
          const confirmation=await json<Capture>('/backend/api/public/payments/confirm',{method:'POST',headers:await csrfHeader(),body:JSON.stringify({localOrderId:order.localOrderId,razorpayOrderId:result.razorpay_order_id,razorpayPaymentId:result.razorpay_payment_id,razorpaySignature:result.razorpay_signature})});
          recordFlow('guest_contribution','confirmed');setReceipt(confirmation);requestKey.current='';
        }catch{recordFlow('guest_contribution','confirmation_pending');setError('Payment submitted, but confirmation is pending. Do not pay again. Contact the host with your Razorpay payment ID: '+result.razorpay_payment_id)}
        finally{onProviderVisibilityChange?.(false);setBusy(false)}
      }});
      instance.on('payment.failed',()=>{onProviderVisibilityChange?.(false);recordFlow('guest_contribution','failed');setError('Payment failed or was cancelled. Please check Razorpay before retrying.');setBusy(false)});
      onProviderVisibilityChange?.(true);instance.open();
    }catch(e){onProviderVisibilityChange?.(false);recordFlow('guest_contribution','failed');setError(e instanceof Error?e.message:'Unable to start payment');setBusy(false)}
  }
  if(mode==='FOCUSED')return <><div aria-live="polite">{destinationsLoading&&<p role="status">Loading payment methods…</p>}{destinationsError&&<p role="alert">{destinationsError} <button type="button" onClick={()=>setDestinationRetry(v=>v+1)}>Retry</button></p>}</div><FocusedCheckout token={token} amount={amount} name={name} email={email} phone={phone} method={paymentMethod} busy={busy} error={error} valid={validAmount} ready={!!eventId} gift={!!giftId} destination={directDestinationId} reference={transferReference} referenceError={referenceError} reported={directStatus} receipt={receipt} available={canUseDirect} setAmount={v=>{setAmount(v);requestKey.current=''}} setName={v=>{setName(v);requestKey.current=''}} setEmail={v=>{setEmail(v);requestKey.current=''}} setPhone={v=>{setPhone(v);requestKey.current=''}} setMethod={v=>{setPaymentMethod(v);setDirectDestinationId('');setDirectStatus('');requestKey.current=''}} setDestination={v=>{setDirectDestinationId(v);requestKey.current='';setDirectStatus('')}} setReference={v=>{setTransferReference(v);requestKey.current='';setError('')}} submit={submit} report={()=>void reportDirectPayment()} close={onClose}/></>;
  return <section id="contribute" aria-label="Contribute to celebration" className="loveCheckout">
    <div className="loveCheckoutHeader"><div><h2>Share a Gift with Love ❤️</h2><p>Razorpay: card, UPI and netbanking · Direct transfers to the host when enabled</p></div>{onClose&&<button type="button" onClick={onClose} aria-label="Close contribution popup" className="loveModalClose">✕</button>}</div>
    {directStatus?<div role="status" className="loveCheckoutReceipt">
      <h3>Thank you for sharing your love! ❤️</h3>
      <p>Your gift details have been submitted successfully.</p><p className="loveDirectSafety">Transfer reported. Receipt of funds still needs independent verification.</p>
      <p>Amount: <strong>{money(amountPaise)}</strong></p>
      <p>Payment method: <strong>{paymentMethod==='upi'?'UPI':'Bank Transfer'}</strong></p>
      <p>{directStatus.split('Reference: ')[1] ? `Submission reference: ${directStatus.split('Reference: ')[1]}` : ''}</p>
      <button type="button" onClick={()=>{setDirectStatus('');setTransferReference('');requestKey.current='';}}>Make another contribution</button>
    </div>:receipt?<div role="status" className="loveCheckoutReceipt"><h3>Your gift of love is confirmed ❤️</h3><p>Gift allocation: {money(receipt.giftPaise)}</p><p>Event wallet: {money(receipt.walletPaise)}</p><p>Payment ID: {receipt.paymentId}</p><button type="button" onClick={()=>{setReceipt(null);requestKey.current='';setDirectStatus('');}}>Make another contribution</button></div>:
    <form onSubmit={submit} className="loveCheckoutForm">
      {destinationsLoading&&<p role="status">Loading UPI and bank details…</p>}
      {destinationsError&&<p role="alert">{destinationsError} <button type="button" onClick={()=>setDestinationRetry(v=>v+1)}>Retry</button></p>}
      <fieldset className="lovePaymentMethods" aria-label="Payment method" style={{border:'1px solid #ded6cc',borderRadius:12,padding:14,marginBottom:16}}>
        <legend style={{fontWeight:700,padding:'0 6px'}}>Choose how to send your gift</legend>
        {guestPaymentMethodChoices.map(method=><label key={method.id} style={{display:'flex',alignItems:'flex-start',gap:10,margin:'10px 0',opacity:method.available||availableDirect.some(d=>d.method===method.id.toUpperCase())?1:0.65}}>
          <input type="radio" name="guestPaymentMethod" value={method.id} checked={paymentMethod===method.id} disabled={busy||(!method.available&&!availableDirect.some(d=>d.method===method.id.toUpperCase()))} onChange={()=>{setPaymentMethod(method.id);setDirectDestinationId('');setDirectStatus('');requestKey.current=''}}/>
          <span><strong>{method.label}</strong>{!method.available&&!availableDirect.some(d=>d.method===method.id.toUpperCase())&&' · Unavailable'}<small style={{display:'block'}}>{method.description}</small></span>
        </label>)}
      </fieldset>
      {!selectedGiftId&&<label className="loveCheckoutField">Destination<select value={giftId} onChange={e=>{setGiftId(e.target.value);requestKey.current=''}}><option value="">Celebration wallet</option>{gifts.map(g=><option key={g.id} value={g.id}>{g.name}</option>)}</select></label>}
      <div className="loveCheckoutMainFields"><label className="loveCheckoutField">Amount (₹)<input type="number" inputMode="decimal" min="1" max="1000000" step="0.01" required value={amount} onChange={e=>{setAmount(e.target.value);requestKey.current=''}}/></label>
      <label className="loveCheckoutField">Your name *<input required autoComplete="name" maxLength={200} value={name} onChange={e=>{setName(e.target.value);requestKey.current=''}} placeholder="Your name"/></label></div>
      <button className="loveCheckoutContactToggle" type="button" aria-expanded={showContact} onClick={()=>setShowContact(v=>!v)}>{showContact?'− Hide optional contact details':'+ Add email or phone (optional)'}</button>
      {showContact&&<div className="loveCheckoutContactFields"><label className="loveCheckoutField">Email (optional)<input type="email" autoComplete="email" maxLength={320} value={email} onChange={e=>{setEmail(e.target.value);requestKey.current=''}}/></label><label className="loveCheckoutField">Phone (optional)<input type="tel" autoComplete="tel" maxLength={30} value={phone} onChange={e=>{setPhone(e.target.value);requestKey.current=''}}/></label></div>}
      {paymentMethod!=='razorpay'&&<div className="loveCheckoutField">
        <GuestVerifiedDestination amountPaise={amountPaise} token={token} method={paymentMethod} destinationId={directDestinationId}
          onSelect={id=>{setDirectDestinationId(id);requestKey.current='';setDirectStatus('')}} />
        <label>UPI transaction ID / Bank UTR
          <input value={transferReference} maxLength={40} aria-invalid={!!referenceValue&&!!referenceError} aria-describedby="transfer-reference-help" onChange={e=>{setTransferReference(e.target.value);requestKey.current='';setError('')}} placeholder="Enter the reference after paying" />
          <span id="transfer-reference-help" role={referenceValue&&referenceError?'alert':undefined}>{referenceValue&&referenceError?referenceError:'Use 8–40 letters, numbers, or hyphens. No spaces or symbols.'}</span>
        </label>
        <p className="loveDirectSafety">After paying, enter your transaction reference and tap “I have done the payment.”</p>
        {directStatus&&<p role="status">{directStatus}</p>}
        <button type="button" className="loveCheckoutPay"
          disabled={busy||!canUseDirect(paymentMethod==='upi'?'upi':'bank')||!directDestinationId||!referenceValue||!validAmount||!name.trim()}
          onClick={reportDirectPayment}>I have done the payment</button>
        {!directDestinationId&&<p>Select the host’s preferred payment destination to continue.</p>}
      </div>}
      <p className="loveCheckoutDisclosure">{giftId?'Your gift contribution may be split with the celebration wallet if the gift reaches its value. Any excess is handled in the celebration wallet when the payment is confirmed in our records.':'Your contribution goes to the celebration wallet.'}</p>
      {error&&<p role="alert" className="loveCheckoutError">{error}</p>}
      <div className="loveCheckoutActions"><div className="loveCheckoutTotal"><span>Total</span><strong>{validAmount?money(amountPaise):'Invalid amount'}</strong></div><button className="loveCheckoutPay" type="submit" style={{display:paymentMethod==='razorpay'?undefined:'none'}} disabled={paymentMethod!=='razorpay'||!eventId||!validAmount||!name.trim()||busy}>{busy?'Processing…':`Pay ${validAmount?money(amountPaise):''} (Test Mode)`}</button></div>
    </form>}
  </section>;
}
