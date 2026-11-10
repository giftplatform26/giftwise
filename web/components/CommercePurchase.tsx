'use client';
import {beginAction,completeAction} from '../lib/interaction-analytics.mjs';
import {activeLocale} from '../lib/localization.mjs';

/* localization:instrumented */
import {localize,localizeText} from '../lib/localization.mjs';
import {useLocalizationRender} from './LocalizationProvider';

import {useEffect,useState} from 'react';
type Offer={stream:string;resourceId:string;title:string;amountPaise:number;durationDays:number;entitled:boolean};
type Order={id:string;providerOrderId:string|null;keyId:string;amountPaise:number;currency:string;status:string};
export const commercialMoney=(n:number)=>new Intl.NumberFormat(activeLocale("en-IN"),{style:'currency',currency:'INR'}).format(n/100);
export async function commercialApi<T>(path:string,body?:unknown,method='POST',extra:Record<string,string>={}):Promise<T>{
 const headers:Record<string,string>={...extra};if(body!==undefined){const init=await fetch('/backend/api/auth/csrf',{credentials:'include',cache:'no-store'});if(!init.ok)throw Error('Could not initialize secure request');const token=document.cookie.split(';').map(x=>x.trim()).find(x=>x.startsWith('XSRF-TOKEN='))?.slice(11);if(!token)throw Error('Secure session missing');headers['Content-Type']='application/json';headers['X-XSRF-TOKEN']=decodeURIComponent(token);}
 const r=await fetch('/backend'+path,{credentials:'include',cache:'no-store',headers,...(body!==undefined?{method,body:JSON.stringify(body)}:{})});
 const data=await r.json().catch(()=>({}));if(!r.ok)throw Error(data.detail||data.message||`Request failed (${r.status})`);return data;
}
async function loadProvider(){if(window.Razorpay)return;await new Promise<void>((resolve,reject)=>{const s=document.createElement('script');s.src='https://checkout.razorpay.com/v1/checkout.js';s.onload=()=>resolve();s.onerror=()=>reject(Error('Could not load checkout'));document.head.appendChild(s)});}
export default function CommercePurchase({stream,resourceId,groupBase,pageId,onComplete}:{stream:string;resourceId:string;groupBase?:string;pageId?:string;onComplete?:()=>void}){useLocalizationRender();
 const [offer,setOffer]=useState<Offer|null>(null),[error,setError]=useState(''),[busy,setBusy]=useState(false),[notice,setNotice]=useState(''),[retry,setRetry]=useState(0);
 const base=groupBase||'/api/v1/commerce';const extra:Record<string,string>=pageId?{'X-Group-Page-Id':pageId}:{};
 useEffect(()=>{let active=true;setOffer(null);setError('');commercialApi<Offer>(groupBase||`${base}/offer?stream=${encodeURIComponent(stream)}&resourceId=${encodeURIComponent(resourceId)}`,undefined,'GET',extra).then(x=>{if(active)setOffer(x)}).catch(e=>{if(active)setError(e.message)});return()=>{active=false}},[stream,resourceId,groupBase,pageId,retry]);
 async function pay(){if(!offer||busy)return;const interaction=beginAction('PAYMENT_CHECKOUT');setBusy(true);setError('');setNotice('');const storageKey=`commercial-checkout:${base}:${stream}:${resourceId}`;
 try{let key=sessionStorage.getItem(storageKey);if(!key){key=crypto.randomUUID();sessionStorage.setItem(storageKey,key)}
 const order=await commercialApi<Order>(`${base}/checkout`,{stream,resourceId,idempotencyKey:key,acceptedAmountPaise:offer.amountPaise},'POST',extra);
 const finish=()=>{completeAction(interaction,'SUCCESS',offer.amountPaise===0?'ACCESS_ACTIVATED':'PAYMENT_CONFIRMED');sessionStorage.removeItem(storageKey);setNotice('Purchase confirmed. Your access is ready.');setBusy(false);setRetry(v=>v+1);onComplete?.()};
 if(order.status==='CAPTURED'||order.status==='FREE'){finish();return;}
 if(order.currency!=='INR'||order.amountPaise!==offer.amountPaise||!order.providerOrderId||!order.keyId.startsWith('rzp_test_'))throw Error('Unexpected checkout response');
 await loadProvider();if(!window.Razorpay)throw Error('Checkout unavailable');
 const instance=new window.Razorpay({key:order.keyId,amount:order.amountPaise,currency:'INR',name:'GiftWise',description:offer.title,order_id:order.providerOrderId,prefill:{name:'',email:'',contact:''},modal:{ondismiss:()=>{completeAction(interaction,'CANCELLED','CHECKOUT_DISMISSED');setBusy(false)}},handler:async payment=>{
 try{await commercialApi(`${base}/confirm`,{orderId:order.id,providerOrderId:payment.razorpay_order_id,paymentId:payment.razorpay_payment_id,signature:payment.razorpay_signature},'POST',extra);finish()}catch{completeAction(interaction,'UNKNOWN','PAYMENT_CONFIRMATION_PENDING');setError(`Payment confirmation is pending. Keep payment ID ${payment.razorpay_payment_id}. Do not pay again.`);setBusy(false)}
 }});instance.on('payment.failed',()=>{completeAction(interaction,'FAILURE','PROVIDER_REPORTED_FAILURE');setError('Payment was not completed. Check your payment status before retrying.');setBusy(false)});instance.open();
 }catch(e){completeAction(interaction,'FAILURE','CHECKOUT_REQUEST_FAILED');setError(e instanceof Error?e.message:'Could not start purchase');setBusy(false)}}
 return <div style={{margin:'12px 0'}}>{offer&&<><p>{offer.title} · <strong>{commercialMoney(offer.amountPaise)}</strong>{offer.durationDays>0?localize("ui.d5ffb95fa822b834",{defaultValue:" for {{value0}} days",value0:(offer.durationDays)}):localize("ui.e161b367076f04bb",{defaultValue:" · one-time purchase"})}</p>{offer.entitled&&<p>{localize("ui.d3880fbf4379cc20",{defaultValue:"Access is active."})}</p>}{(!offer.entitled||['HOST_PLAN','VENDOR_LISTING','SPONSORED_GIFT'].includes(stream))&&<button type="button" disabled={busy} onClick={()=>void pay()}>{busy?localize("ui.42074396a441a347",{defaultValue:"Processing…"}):offer.amountPaise===0?localize("ui.850fbbaefb4057d0",{defaultValue:"Activate access"}):localize("ui.0e3afe9550a3bde6",{defaultValue:"{{value0}} · {{value1}} (test mode)",value0:(offer.entitled?'Renew':'Purchase'),value1:(commercialMoney(offer.amountPaise))})}</button>}</>}{!offer&&!error&&<p>{localize("ui.50992858fac06af3",{defaultValue:"Loading price…"})}</p>}{groupBase&&error.includes('switched off')?<p>{localize("ui.44b88976e4d39b72",{defaultValue:"No group coordination fee is enabled. You can publish without a purchase."})}</p>:error&&<p role="alert">{localizeText(error)} <button type="button" disabled={busy} onClick={()=>{setRetry(v=>v+1)}}>{localize("ui.087a9cc69283c135",{defaultValue:"Reload price"})}</button></p>}{notice&&<p role="status">{localizeText(notice)}</p>}</div>;
}
