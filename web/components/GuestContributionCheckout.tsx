'use client';
import PaymentDisclosure from './PaymentDisclosure';
import {beginAction,completeAction} from '../lib/interaction-analytics.mjs';
import {activeLocale} from '../lib/localization.mjs';

/* localization:instrumented */
import {localize,localizeText} from '../lib/localization.mjs';
import {useLocalizationRender} from './LocalizationProvider';

import {recordFlow} from '../lib/diagnostics.mjs';
import {useEffect,useRef,useState} from 'react';
import {useExperience} from './ExperienceProvider';
import FocusedCheckout from './focused/Checkout';
import GuestVerifiedDestination from './GuestVerifiedDestination';

type Gift = {id:string;name:string;targetAmount:number};
type Checkout = {localOrderId:string;razorpayOrderId:string;razorpayKeyId:string;amountPaise:number;currency:string;status:string;platformFeePaise:number;hostReceivesPaise:number};
type Pricing = {eventId:string;giftId:string|null;contributionPaise:number;platformFeePaise:number;totalPaise:number;hostReceivesPaise:number;feeBearer:string};
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
const money=(paise:number)=>new Intl.NumberFormat(activeLocale("en-IN"),{style:'currency',currency:'INR'}).format(paise/100);
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
export default function GuestContributionCheckout({token,gifts,selectedGiftId='',onClose,onBusyChange,onProviderVisibilityChange}:{token:string;gifts:Gift[];selectedGiftId?:string;onClose?:()=>void;onBusyChange?:(busy:boolean)=>void;onProviderVisibilityChange?:(open:boolean)=>void}){useLocalizationRender();
  const {mode}=useExperience();
  const [razorpayAllowed,setRazorpayAllowed]=useState(false);
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
  const [pricing,setPricing]=useState<Pricing|null>(null),[pricingError,setPricingError]=useState(''),[pricingRetry,setPricingRetry]=useState(0);
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
  useEffect(()=>{let active=true;setRazorpayAllowed(false);fetch(`/backend/api/public/payments/controls/${encodeURIComponent(token)}`,{credentials:'include',cache:'no-store'}).then(async r=>{if(!r.ok)throw Error();return r.json()}).then(x=>{if(active)setRazorpayAllowed(x.channels?.RAZORPAY===true&&(!x.channels?.ROUTING||x.routingIntegrationReady===true))}).catch(()=>{if(active)setError('Could not load payment availability. Reload before continuing.')});return()=>{active=false}},[token]);
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
    const interaction=beginAction('PAYMENT_REPORT',{token});
    recordFlow('guest_contribution','started');setBusy(true);setError('');setDirectStatus('');
    try{
      if(!requestKey.current)requestKey.current=crypto.randomUUID();
      const result=await json<{claimId:string;status:string;message:string}>(`/backend/api/public/payments/direct/${encodeURIComponent(token)}/claims`,{
        method:'POST',headers:await csrfHeader(),body:JSON.stringify({destinationId:directDestinationId,
          method:paymentMethod==='upi'?'UPI':'BANK',giftId:giftId||null,amountPaise,
          guestName:name.trim(),guestEmail:email.trim()||null,guestPhone:phone.trim()||null,transactionReference:transferReference.trim(),idempotencyKey:requestKey.current})});
      completeAction(interaction,'SUCCESS','TRANSFER_REPORT_SUBMITTED_UNVERIFIED');recordFlow('guest_contribution','submitted_unverified');setDirectStatus(`Thank you for sharing your love! Your gift details have been submitted successfully. Reference: ${result.claimId}`);
    }catch(e){completeAction(interaction,'FAILURE','TRANSFER_REPORT_FAILED');recordFlow('guest_contribution','failed');setError(e instanceof Error?e.message:'Unable to submit payment report')}
    finally{setBusy(false)}
  }
  const amountPaise=Math.round(Number(amount)*100);
  const validAmount=Number.isFinite(amountPaise)&&Number.isSafeInteger(amountPaise)&&amountPaise>=100&&amountPaise<=100000000;
  useEffect(()=>{
    let active=true;setPricing(null);setPricingError('');
    if(!eventId||!validAmount||paymentMethod!=='razorpay'||!razorpayAllowed)return;
    const timer=setTimeout(()=>{void csrfHeader().then(headers=>json<Pricing>('/backend/api/public/payments/hybrid/quote',{method:'POST',headers,body:JSON.stringify({eventId,giftId:giftId||null,contributionPaise:amountPaise})})).then(q=>{if(active)setPricing(q)}).catch(()=>{if(active)setPricingError('Could not load platform fees. Please retry.');});},250);
    return()=>{active=false;clearTimeout(timer)};
  },[eventId,giftId,amountPaise,validAmount,paymentMethod,pricingRetry,razorpayAllowed]);
  const quoteReady=!!pricing&&pricing.eventId===eventId&&pricing.giftId===(giftId||null)&&pricing.contributionPaise===amountPaise;
  const pricingNotice=<>{paymentMethod==='razorpay'&&!razorpayAllowed?<p>{localize("ui.026e0423ba60af9c",{defaultValue:"Razorpay checkout is unavailable for this event. Choose an available direct payment method."})}</p>:paymentMethod==='razorpay'?<div aria-live="polite">{pricingError?<p role="alert">{pricingError} <button type="button" onClick={()=>{requestKey.current='';setPricingRetry(v=>v+1)}}>{localize("ui.5b987d1a4ee6d349",{defaultValue:"Refresh fee"})}</button></p>:quoteReady&&pricing?<p>{localize("ui.cc50a8af99b9fa21",{defaultValue:"Platform fee: "})}<strong>{money(pricing.platformFeePaise)}</strong>{pricing.platformFeePaise>0?(pricing.feeBearer==='HOST'?localize("ui.12a5f8a9efd2015c",{defaultValue:" · deducted from host receipt"}):localize("ui.c58cdf6d8b62b400",{defaultValue:" · added to your contribution"})):''} {localize("ui.2d10d489ae23d59f",{defaultValue:" · You pay "})}<strong>{money(pricing.totalPaise)}</strong> {localize("ui.8255c8ff7a559016",{defaultValue:" · Host receives "})}<strong>{money(pricing.hostReceivesPaise)}</strong></p>:<p>{localize("ui.114f40c2dc979cbc",{defaultValue:"Loading platform fees…"})}</p>}</div>:null}<PaymentDisclosure/></>;
  async function submit(e:React.FormEvent){
    e.preventDefault();if(!razorpayAllowed||paymentMethod!=='razorpay'||!validAmount||!name.trim()||!eventId||busy||!quoteReady||!pricing)return;
    const interaction=beginAction('PAYMENT_CHECKOUT',{token});
    recordFlow('guest_contribution','started');setBusy(true);setError('');setReceipt(null);
    try{
      const headers=await csrfHeader();
      if(!requestKey.current)requestKey.current=crypto.randomUUID();
      const order=await json<Checkout>('/backend/api/public/payments/checkout',{method:'POST',headers,body:JSON.stringify({eventId,giftId:giftId||null,contributionType:giftId?'GIFT_PARTIAL':'EVENT_WALLET',amountPaise,acceptedFeePaise:pricing.platformFeePaise,acceptedTotalPaise:pricing.totalPaise,acceptedHostReceivesPaise:pricing.hostReceivesPaise,idempotencyKey:requestKey.current,guestName:name.trim(),guestEmail:email.trim()||null,guestPhone:phone.trim()||null})});
      if(order.currency!=='INR'||order.amountPaise!==pricing.totalPaise||order.platformFeePaise!==pricing.platformFeePaise||order.hostReceivesPaise!==pricing.hostReceivesPaise||!order.razorpayKeyId.startsWith('rzp_test_'))throw new Error('Invalid test checkout response');
      await loadRazorpay();
      if(!window.Razorpay)throw new Error('Razorpay checkout failed to load');
      const instance=new window.Razorpay({key:order.razorpayKeyId,amount:order.amountPaise,currency:'INR',name:'GiftWise',description:giftId?'Gift contribution':'Event wallet contribution',order_id:order.razorpayOrderId,prefill:{name,email,contact:phone},modal:{ondismiss:()=>{onProviderVisibilityChange?.(false);completeAction(interaction,'CANCELLED','CHECKOUT_DISMISSED');recordFlow('guest_contribution','cancelled');setBusy(false)}},handler:async result=>{
        try{
          const confirmation=await json<Capture>('/backend/api/public/payments/confirm',{method:'POST',headers:await csrfHeader(),body:JSON.stringify({localOrderId:order.localOrderId,razorpayOrderId:result.razorpay_order_id,razorpayPaymentId:result.razorpay_payment_id,razorpaySignature:result.razorpay_signature})});
          completeAction(interaction,'SUCCESS','PAYMENT_CONFIRMED');recordFlow('guest_contribution','confirmed');setReceipt(confirmation);requestKey.current='';
        }catch{completeAction(interaction,'UNKNOWN','PAYMENT_CONFIRMATION_PENDING');recordFlow('guest_contribution','confirmation_pending');setError('Payment submitted, but confirmation is pending. Do not pay again. Contact the host with your Razorpay payment ID: '+result.razorpay_payment_id)}
        finally{onProviderVisibilityChange?.(false);setBusy(false)}
      }});
      instance.on('payment.failed',()=>{completeAction(interaction,'FAILURE','PROVIDER_REPORTED_FAILURE');onProviderVisibilityChange?.(false);recordFlow('guest_contribution','failed');setError('Payment failed or was cancelled. Please check Razorpay before retrying.');setBusy(false)});
      onProviderVisibilityChange?.(true);instance.open();
    }catch(e){completeAction(interaction,'FAILURE','CHECKOUT_REQUEST_FAILED');setPricing(null);setPricingError('Checkout could not start. Refresh fees before retrying.');onProviderVisibilityChange?.(false);recordFlow('guest_contribution','failed');setError(e instanceof Error?e.message:'Unable to start payment');setBusy(false)}
  }
  if(mode==='FOCUSED')return <><div aria-live="polite">{destinationsLoading&&<p role="status">{localize("ui.587f32a685d7c85e",{defaultValue:"Loading payment methods…"})}</p>}{destinationsError&&<p role="alert">{destinationsError} <button type="button" onClick={()=>setDestinationRetry(v=>v+1)}>{localize("ui.942087cc2d41e013",{defaultValue:"Retry"})}</button></p>}</div><FocusedCheckout token={token} amount={amount} name={name} email={email} phone={phone} method={paymentMethod} busy={busy} error={error} valid={validAmount} ready={!!eventId&&quoteReady&&razorpayAllowed} razorpayAllowed={razorpayAllowed} pricingNotice={pricingNotice} totalPaise={pricing?.totalPaise} gift={!!giftId} destination={directDestinationId} reference={transferReference} referenceError={referenceError} reported={directStatus} receipt={receipt} available={canUseDirect} setAmount={v=>{setAmount(v);requestKey.current=''}} setName={v=>{setName(v);requestKey.current=''}} setEmail={v=>{setEmail(v);requestKey.current=''}} setPhone={v=>{setPhone(v);requestKey.current=''}} setMethod={v=>{setPaymentMethod(v);setDirectDestinationId('');setDirectStatus('');requestKey.current=''}} setDestination={v=>{setDirectDestinationId(v);requestKey.current='';setDirectStatus('')}} setReference={v=>{setTransferReference(v);requestKey.current='';setError('')}} submit={submit} report={()=>void reportDirectPayment()} close={onClose}/></>;
  return <section id="contribute" aria-label={localize("ui.f03be6fa028cbdd8",{defaultValue:"Contribute to celebration"})} className="loveCheckout">
    <div className="loveCheckoutHeader"><div><h2>{localize("ui.c643cf7e27505d20",{defaultValue:"Share a Gift with Love ❤️"})}</h2><p>{localize("ui.ecf968003d31f7a8",{defaultValue:"Razorpay: card, UPI and netbanking · Direct transfers to the host when enabled"})}</p></div>{onClose&&<button type="button" onClick={onClose} aria-label={localize("ui.cd1fde315c4e8998",{defaultValue:"Close contribution popup"})} className="loveModalClose">✕</button>}</div>
    {directStatus?<div role="status" className="loveCheckoutReceipt">
      <h3>{localize("ui.ddef4db6fbaebc33",{defaultValue:"Thank you for sharing your love! ❤️"})}</h3>
      <p>{localize("ui.1cd81ea4e1cfcce1",{defaultValue:"Your gift details have been submitted successfully."})}</p><p className="loveDirectSafety">{localize("ui.ced4630baaf4f40c",{defaultValue:"Transfer reported. Receipt of funds still needs independent verification."})}</p>
      <p>{localize("ui.ad7e6bbc8130bd2c",{defaultValue:"Amount: "})}<strong>{money(amountPaise)}</strong></p>
      <p>{localize("ui.7a28ce0e55bdf916",{defaultValue:"Payment method: "})}<strong>{paymentMethod==='upi'?localize("ui.7b06e0160857f939",{defaultValue:"UPI"}):localize("ui.ee82ae2801cf9097",{defaultValue:"Bank Transfer"})}</strong></p>
      <p>{directStatus.split('Reference: ')[1] ? localize("ui.4205799edc57090d",{defaultValue:"Submission reference: {{value0}}",value0:(directStatus.split('Reference: ')[1])}) : ''}</p>
      <button type="button" onClick={()=>{setDirectStatus('');setTransferReference('');requestKey.current='';}}>{localize("ui.e382a36729887241",{defaultValue:"Make another contribution"})}</button>
    </div>:receipt?<div role="status" className="loveCheckoutReceipt"><h3>{localize("ui.a2a8d67d45871127",{defaultValue:"Your gift of love is confirmed ❤️"})}</h3><p>{localize("ui.dddc9242b436b8e8",{defaultValue:"Gift allocation: "})}{money(receipt.giftPaise)}</p><p>{localize("ui.18e4485dbf0d2bae",{defaultValue:"Event wallet: "})}{money(receipt.walletPaise)}</p><p>{localize("ui.1e029b2aa4518972",{defaultValue:"Payment ID: "})}{receipt.paymentId}</p><button type="button" onClick={()=>{setReceipt(null);requestKey.current='';setDirectStatus('');}}>{localize("ui.e382a36729887241",{defaultValue:"Make another contribution"})}</button></div>:
    <form onSubmit={submit} className="loveCheckoutForm">
      {destinationsLoading&&<p role="status">{localize("ui.8479e3a7bd313b71",{defaultValue:"Loading UPI and bank details…"})}</p>}
      {destinationsError&&<p role="alert">{destinationsError} <button type="button" onClick={()=>setDestinationRetry(v=>v+1)}>{localize("ui.942087cc2d41e013",{defaultValue:"Retry"})}</button></p>}
      <fieldset className="lovePaymentMethods" aria-label={localize("ui.b948ac04b854394f",{defaultValue:"Payment method"})} style={{border:'1px solid #ded6cc',borderRadius:12,padding:14,marginBottom:16}}>
        <legend style={{fontWeight:700,padding:'0 6px'}}>{localize("ui.d9d980b02f122917",{defaultValue:"Choose how to send your gift"})}</legend>
        {guestPaymentMethodChoices.filter(method=>method.id!=='razorpay'||razorpayAllowed).map(method=><label key={method.id} style={{display:'flex',alignItems:'flex-start',gap:10,margin:'10px 0',opacity:method.available||availableDirect.some(d=>d.method===method.id.toUpperCase())?1:0.65}}>
          <input type="radio" name="guestPaymentMethod" value={method.id} checked={paymentMethod===method.id} disabled={busy||(!method.available&&!availableDirect.some(d=>d.method===method.id.toUpperCase()))} onChange={()=>{setPaymentMethod(method.id);setDirectDestinationId('');setDirectStatus('');requestKey.current=''}}/>
          <span><strong>{method.label}</strong>{!method.available&&!availableDirect.some(d=>d.method===method.id.toUpperCase())&&localize("ui.77649d652d5dfc7d",{defaultValue:" · Unavailable"})}<small style={{display:'block'}}>{method.description}</small></span>
        </label>)}
      </fieldset>
      {!selectedGiftId&&<label className="loveCheckoutField">{localize("ui.293d404a500f5f9a",{defaultValue:"Destination"})}<select value={giftId} onChange={e=>{setGiftId(e.target.value);requestKey.current=''}}><option value="">{localize("ui.0ebda8b7a27b9d41",{defaultValue:"Celebration wallet"})}</option>{gifts.map(g=><option key={g.id} value={g.id}>{g.name}</option>)}</select></label>}
      <div className="loveCheckoutMainFields"><label className="loveCheckoutField">{localize("ui.fcbb0bc384eed802",{defaultValue:"Amount (₹)"})}<input type="number" inputMode="decimal" min="1" max="1000000" step="0.01" required value={amount} onChange={e=>{setAmount(e.target.value);requestKey.current=''}}/></label>
      <label className="loveCheckoutField">{localize("ui.7204645b7341d714",{defaultValue:"Your name *"})}<input required autoComplete="name" maxLength={200} value={name} onChange={e=>{setName(e.target.value);requestKey.current=''}} placeholder={localize("ui.2c6b2e253c01fb2e",{defaultValue:"Your name"})}/></label></div>
      <button className="loveCheckoutContactToggle" type="button" aria-expanded={showContact} onClick={()=>setShowContact(v=>!v)}>{showContact?localize("ui.42485bdfe008e57e",{defaultValue:"− Hide optional contact details"}):localize("ui.958860832f4ffd8d",{defaultValue:"+ Add email or phone (optional)"})}</button>
      {showContact&&<div className="loveCheckoutContactFields"><label className="loveCheckoutField">{localize("ui.6eea8608c452711b",{defaultValue:"Email (optional)"})}<input type="email" autoComplete="email" maxLength={320} value={email} onChange={e=>{setEmail(e.target.value);requestKey.current=''}}/></label><label className="loveCheckoutField">{localize("ui.35878f0bd6c133b3",{defaultValue:"Phone (optional)"})}<input type="tel" autoComplete="tel" maxLength={30} value={phone} onChange={e=>{setPhone(e.target.value);requestKey.current=''}}/></label></div>}
      {paymentMethod!=='razorpay'&&<div className="loveCheckoutField">
        <GuestVerifiedDestination amountPaise={amountPaise} token={token} method={paymentMethod} destinationId={directDestinationId}
          onSelect={id=>{setDirectDestinationId(id);requestKey.current='';setDirectStatus('')}} />
        <label>{localize("ui.0c0b7cca57d23288",{defaultValue:"UPI transaction ID / Bank UTR"})}<input value={transferReference} maxLength={40} aria-invalid={!!referenceValue&&!!referenceError} aria-describedby="transfer-reference-help" onChange={e=>{setTransferReference(e.target.value);requestKey.current='';setError('')}} placeholder={localize("ui.11f7fb5414400a9b",{defaultValue:"Enter the reference after paying"})} />
          <span id="transfer-reference-help" role={referenceValue&&referenceError?'alert':undefined}>{referenceValue&&referenceError?referenceError:localize("ui.633b081a01d30bd6",{defaultValue:"Use 8–40 letters, numbers, or hyphens. No spaces or symbols."})}</span>
        </label>
        <p className="loveDirectSafety">{localize("ui.da9afac5ebcfe91e",{defaultValue:"After paying, enter your transaction reference and tap “I have done the payment.”"})}</p>
        {directStatus&&<p role="status">{directStatus}</p>}
        <button type="button" className="loveCheckoutPay"
          disabled={busy||!canUseDirect(paymentMethod==='upi'?'upi':'bank')||!directDestinationId||!referenceValue||!validAmount||!name.trim()}
          onClick={reportDirectPayment}>{localize("ui.6280d7748e32d3ac",{defaultValue:"I have done the payment"})}</button>
        {!directDestinationId&&<p>{localize("ui.f5f52a3394a90e27",{defaultValue:"Select the host’s preferred payment destination to continue."})}</p>}
      </div>}
      <p className="loveCheckoutDisclosure">{giftId?localize("ui.462374a04042068d",{defaultValue:"Your gift contribution may be split with the celebration wallet if the gift reaches its value. Any excess is handled in the celebration wallet when the payment is confirmed in our records."}):localize("ui.c7e71af4212581d7",{defaultValue:"Your contribution goes to the celebration wallet."})}</p>
      {error&&<p role="alert" className="loveCheckoutError">{localizeText(error)}</p>}
      {pricingNotice}
      <div className="loveCheckoutActions"><div className="loveCheckoutTotal"><span>{localize("ui.c9b3c38247f744e1",{defaultValue:"Total"})}</span><strong>{validAmount?money(paymentMethod==='razorpay'&&pricing?pricing.totalPaise:amountPaise):localize("ui.f7cda366969f0e98",{defaultValue:"Invalid amount"})}</strong></div><button className="loveCheckoutPay" type="submit" style={{display:paymentMethod==='razorpay'?undefined:'none'}} disabled={paymentMethod!=='razorpay'||!razorpayAllowed||!eventId||!validAmount||!name.trim()||busy||!quoteReady}>{busy?localize("ui.42074396a441a347",{defaultValue:"Processing…"}):localize("ui.0999a2d14c3594b1",{defaultValue:"Pay {{value0}} (Test Mode)",value0:(validAmount&&pricing?money(pricing.totalPaise):'')})}</button></div>
    </form>}
  </section>;
}
