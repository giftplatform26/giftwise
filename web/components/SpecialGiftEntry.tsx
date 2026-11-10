'use client';
/* localization:instrumented */
import {localize,localizeText} from '../lib/localization.mjs';
import {useLocalizationRender} from './LocalizationProvider';

import {useEffect,useRef,useState} from 'react';
import GroupContribution from './GroupContribution';
import IndividualHostContribution from './IndividualHostContribution';
import type {DeliveryAddress} from './DeliveryAddressFields';
import {notifyGuestGifts} from './GuestGiftManager';
import './group-contribution.css';

type Gift={id:string;name:string;description?:string;product_url?:string;disabled?:boolean};
type SavedGift=Gift&{giving_method?:'LINK_AND_CONTRIBUTION'|'DIRECT_DELIVERY';delivery_status?:string};
const empty={name:'',guestName:'',email:'',mobile:'',description:'',productUrl:'',amount:''};
export default function SpecialGiftEntry({token}:{token:string}){useLocalizationRender();
 const [step,setStep]=useState<'closed'|'choice'|'individual'|'group'>('closed');
 const [form,setForm]=useState(empty),[method,setMethod]=useState<'LINK_AND_CONTRIBUTION'|'DIRECT_DELIVERY'>('LINK_AND_CONTRIBUTION');
 const [gifts,setGifts]=useState<Gift[]>([]),[source,setSource]=useState(''),[saved,setSaved]=useState<SavedGift|null>(null),[previous,setPrevious]=useState<SavedGift[]>([]);
 const [address,setAddress]=useState<DeliveryAddress|null>(null),[tracking,setTracking]=useState(''),[notice,setNotice]=useState(''),[error,setError]=useState(''),[busy,setBusy]=useState(false);
 const dialog=useRef<HTMLDivElement>(null),launch=useRef<HTMLButtonElement>(null);
 const base=`/backend/api/public/groups/${encodeURIComponent(token)}/guest-gifts`;
 async function call(path:string,body?:unknown){
  if(body!==undefined){const csrf=await fetch('/backend/api/auth/csrf',{credentials:'include'});if(!csrf.ok)throw Error('Could not start a secure request. Please try again.');}
  const csrf=decodeURIComponent(document.cookie.split('; ').find(v=>v.startsWith('XSRF-TOKEN='))?.slice('XSRF-TOKEN='.length)||'');
  const response=await fetch(base+path,{method:body===undefined?'GET':'POST',credentials:'include',cache:'no-store',headers:body===undefined?{}:{'Content-Type':'application/json','X-XSRF-TOKEN':csrf},body:body===undefined?undefined:JSON.stringify(body)});
  if(!response.ok){let message=response.status===400?'Please check your contact details, gift link and contribution amount.':response.status===403?'This gifting option is unavailable for this invitation.':'Could not save your gift. Please try again.';try{const problem=await response.json();message=problem.detail||problem.message||message}catch{}throw Error(message)}
  return response.json();
 }
 function close(){if(busy)return;setStep('closed');setError('');launch.current?.focus()}
 useEffect(()=>{
  if(step==='closed'||step==='group')return;
  const previousOverflow=document.body.style.overflow;document.body.style.overflow='hidden';
  dialog.current?.querySelector<HTMLElement>('[data-autofocus]')?.focus();
  function keys(event:KeyboardEvent){
   if(event.key==='Escape'){event.preventDefault();close()}
   if(event.key==='Tab'){
    const nodes=dialog.current?.querySelectorAll<HTMLElement>('button:not([disabled]),input:not([disabled]),textarea:not([disabled]),select:not([disabled]),a[href]');
    if(!nodes?.length)return;const first=nodes[0],last=nodes[nodes.length-1];
    if(event.shiftKey&&document.activeElement===first){event.preventDefault();last.focus()}
    else if(!event.shiftKey&&document.activeElement===last){event.preventDefault();first.focus()}
   }
  }
  document.addEventListener('keydown',keys);return()=>{document.body.style.overflow=previousOverflow;document.removeEventListener('keydown',keys)};
 },[step,busy]);
 async function individual(){
  setStep('individual');setError('');setNotice('');
  try{const [available,owned]=await Promise.all([call(''),call('/individual-entry')]);setGifts(available.filter((g:Gift)=>!g.disabled));setPrevious(owned)}catch(e){setError(e instanceof Error?e.message:'Could not load gifts. You can still add a new gift.')}
 }
 function choose(id:string){setSource(id);const gift=gifts.find(g=>g.id===id);setForm(value=>({...value,name:gift?.name||'',description:gift?.description||'',productUrl:gift?.product_url||''}))}
 async function showAddress(id:string){setAddress(await call(`/individual-entry/${id}/delivery-address`))}
 async function save(event:React.FormEvent){
  event.preventDefault();setBusy(true);setError('');setNotice('');
  try{
   if(!form.productUrl.trim()&&!form.description.trim())throw Error('Please add a product link or gift details.');
   if(method==='LINK_AND_CONTRIBUTION'&&(!/^\d+(\.\d{1,2})?$/.test(form.amount)||Number(form.amount)<=0))throw Error('Enter a contribution amount greater than zero, with up to two decimal places.');
   const result=await call('/individual-entry',{sourceGiftId:source||null,name:form.name.trim(),guestName:form.guestName.trim(),email:form.email.trim(),mobile:form.mobile.trim(),description:form.description.trim()||null,productUrl:form.productUrl.trim()||null,givingMethod:method,contributionPaise:method==='LINK_AND_CONTRIBUTION'?Math.round(Number(form.amount)*100):null});
   const gift:SavedGift={id:result.id,name:form.name,product_url:form.productUrl,giving_method:method};setSaved(gift);setPrevious(value=>[gift,...value]);notifyGuestGifts(token);
   setNotice('Your special gift has been added. The host can see your details; other guests only see the gift.');
   if(method==='DIRECT_DELIVERY')await showAddress(result.id);
  }catch(e){setError(e instanceof Error?e.message:'Could not save gift.')}finally{setBusy(false)}
 }
 async function updateDelivery(status:'GIFT_PURCHASED'|'TRACKING_SHARED'){
  if(!saved)return;setBusy(true);setError('');
  try{await call(`/individual-entry/${saved.id}/delivery`,{status,trackingDetails:tracking.trim()||null});setNotice(status==='TRACKING_SHARED'?'Tracking details shared with the host.':'Gift marked as purchased.');setSaved(value=>value?{...value,delivery_status:status}:value)}catch(e){setError(e instanceof Error?e.message:'Could not update delivery.')}finally{setBusy(false)}
 }
 return <><div className="gc-entry gc-compact"><button ref={launch} type="button" className="gc-launch" onClick={()=>{setStep('choice');setError('')}}>{localize("ui.72c7b5708f440207",{defaultValue:"Add a Special Gift"})}</button></div>
 {step==='group'&&<GroupContribution token={token} startOpen independentGift onClose={()=>{setStep('closed');launch.current?.focus()}}/>}
 {(step==='choice'||step==='individual')&&<div className="gc-overlay" role="presentation" onMouseDown={e=>{if(e.target===e.currentTarget)close()}}><div ref={dialog} className="gc-dialog" role="dialog" aria-modal="true" aria-labelledby="special-gift-title">
 <button type="button" className="gc-close" aria-label={localize("ui.7692afd4497f2118",{defaultValue:"Close special gift"})} disabled={busy} onClick={close}>×</button><span className="gc-kicker">{localize("ui.2cb1ba9d3eeee1cd",{defaultValue:"✦ SHARE SOMETHING SPECIAL ✦"})}</span>
 {step==='choice'?<><h2 id="special-gift-title">{localize("ui.9ead2ac60e7db3f4",{defaultValue:"How would you like to give?"})}</h2><p>{localize("ui.a282a41c377c540a",{defaultValue:"Plan a thoughtful gift on your own or bring your favourite people together."})}</p><div className="special-gift-choices"><button data-autofocus type="button" className="gc-secondary" onClick={()=>void individual()}><strong>{localize("ui.c14c2c732437af98",{defaultValue:"Individual Gift"})}</strong><span>{localize("ui.1661cf99f1c904d8",{defaultValue:"Add your details and choose how to share your gift."})}</span><small>{localize("ui.8870479736489e78",{defaultValue:"No account or group needed"})}</small></button><button type="button" className="gc-secondary" onClick={()=>{setError('');setStep('group')}}><strong>{localize("ui.62d0a830e2756168",{defaultValue:"Group Gift"})}</strong><span>{localize("ui.3ab6895ab8f19cc9",{defaultValue:"Create a group and collect contributions together."})}</span><small>{localize("ui.e89f3bfbd1e4e4c0",{defaultValue:"Sign in or create an account to get started"})}</small></button></div></>:<>
 <button type="button" className="gc-secondary" disabled={busy} onClick={()=>setStep('choice')}>{localize("ui.2f60681f8b63e96a",{defaultValue:"← Giving options"})}</button><h2 id="special-gift-title">{localize("ui.57037bdffdc5602d",{defaultValue:"Your special gift"})}</h2>
 {saved?<><p role="status">{notice||localize("ui.18be7e148c04c0fa",{defaultValue:"Manage {{value0}}",value0:(saved.name)})}</p>{saved.giving_method==='LINK_AND_CONTRIBUTION'&&<IndividualHostContribution token={token} giftId={saved.id} call={call} onBusyChange={setBusy} onReported={()=>notifyGuestGifts(token)}/>} {saved.giving_method==='DIRECT_DELIVERY'&&<section className="gc-panel"><h3>{localize("ui.02968357c8dc9617",{defaultValue:"Deliver your gift to the host"})}</h3><p>{localize("ui.ecf0f6235c0d7d99",{defaultValue:"Purchase the item from the retailer and use the host’s address at checkout."})}</p>{saved.product_url&&<a href={saved.product_url} target="_blank" rel="noopener noreferrer">{localize("ui.a1fc818d679ea436",{defaultValue:"Purchase gift from the retailer ↗"})}</a>}{address?<address>{address.addressLine1||localize("ui.34e92463e22d5ad8",{defaultValue:"Please contact the host to confirm the delivery address."})}<br/>{address.city}, {address.state} {address.postalCode}<br/>{address.country}</address>:<button type="button" disabled={busy} onClick={async()=>{setBusy(true);setError('');try{await showAddress(saved.id)}catch(e){setError(e instanceof Error?e.message:'Address unavailable')}finally{setBusy(false)}}}>{localize("ui.0e1504cb20baa605",{defaultValue:"View host delivery address"})}</button>}<button type="button" className="gc-secondary" disabled={busy||saved.delivery_status==='TRACKING_SHARED'||saved.delivery_status==='GIFT_PURCHASED'} onClick={()=>void updateDelivery('GIFT_PURCHASED')}>{saved.delivery_status==='GIFT_PURCHASED'||saved.delivery_status==='TRACKING_SHARED'?localize("ui.e20ac06e7a6a8b8e",{defaultValue:"Gift purchased"}):localize("ui.9c788397748c304a",{defaultValue:"I have purchased the gift"})}</button><form className="gc-form" onSubmit={e=>{e.preventDefault();void updateDelivery('TRACKING_SHARED')}}><label>{localize("ui.f296b67a545cd756",{defaultValue:"Shipment tracking details"})}<textarea required maxLength={1000} placeholder={localize("ui.a9da12665e488803",{defaultValue:"Carrier name, tracking number or tracking link"})} value={tracking} onChange={e=>setTracking(e.target.value)}/></label><button className="gc-launch" disabled={busy}>{localize("ui.15858678da15e73d",{defaultValue:"Share tracking with host"})}</button></form></section>}<button type="button" className="gc-secondary" disabled={busy} onClick={()=>{setSaved(null);setSource('');setForm(empty);setAddress(null);setTracking('');setNotice('');void individual()}}>{localize("ui.dabf10e2128f6ebd",{defaultValue:"Add another special gift"})}</button></>:<form className="gc-form" onSubmit={save}>
 {gifts.length>0&&<label>{localize("ui.af7e58d035aad83e",{defaultValue:"Choose a gift"})}<select disabled={busy} value={source} onChange={e=>choose(e.target.value)}><option value="">{localize("ui.cb480b3aa513d781",{defaultValue:"Add my own special gift"})}</option>{gifts.map(g=><option key={g.id} value={g.id}>{g.name}</option>)}</select></label>}
 <fieldset disabled={busy}><legend>{localize("ui.c0bcd4c0f8e70784",{defaultValue:"Your details"})}</legend><label>{localize("ui.2c6b2e253c01fb2e",{defaultValue:"Your name"})}<input data-autofocus required autoComplete="name" maxLength={120} value={form.guestName} onChange={e=>setForm({...form,guestName:e.target.value})}/></label><label>{localize("ui.f2488fd4ef4adbc6",{defaultValue:"Email address"})}<input required type="email" autoComplete="email" maxLength={254} value={form.email} onChange={e=>setForm({...form,email:e.target.value})}/></label><label>{localize("ui.187e80ba4d8a22bb",{defaultValue:"Mobile number"})}<input required type="tel" autoComplete="tel" pattern="\+?[0-9][0-9 \-]{7,23}" maxLength={25} value={form.mobile} onChange={e=>setForm({...form,mobile:e.target.value})}/></label></fieldset>
 <fieldset disabled={busy}><legend>{localize("ui.f2da19dd48c937be",{defaultValue:"Gift details"})}</legend><label>{localize("ui.64b4ff26c07d838e",{defaultValue:"Gift name"})}<input required maxLength={160} value={form.name} onChange={e=>setForm({...form,name:e.target.value})}/></label><label>{localize("ui.a06890de939bdcac",{defaultValue:"Product link"})}{method==='DIRECT_DELIVERY'?'':localize("ui.418e319e80da27d9",{defaultValue:" · optional if you add gift details"})}<input type="url" required={method==='DIRECT_DELIVERY'} pattern="https://[^\s]+" placeholder="https://…" maxLength={1000} value={form.productUrl} onChange={e=>setForm({...form,productUrl:e.target.value})}/></label><label>{localize("ui.f2da19dd48c937be",{defaultValue:"Gift details"})}<textarea maxLength={500} value={form.description} onChange={e=>setForm({...form,description:e.target.value})}/></label></fieldset>
 <fieldset className="special-gift-methods" disabled={busy}><legend>{localize("ui.5d7ced5b37aaf867",{defaultValue:"How would you like to share your gift?"})}</legend><label><input type="radio" name="special-gift-method" checked={method==='LINK_AND_CONTRIBUTION'} onChange={()=>setMethod('LINK_AND_CONTRIBUTION')}/><span>{localize("ui.466b0173b3baaea4",{defaultValue:"Share a gift link and contribute to the host"})}</span></label><label><input type="radio" name="special-gift-method" checked={method==='DIRECT_DELIVERY'} onChange={()=>setMethod('DIRECT_DELIVERY')}/><span>{localize("ui.764968df837ae88e",{defaultValue:"I’ll purchase and deliver the gift myself"})}</span></label></fieldset>
 {method==='LINK_AND_CONTRIBUTION'?<label>{localize("ui.7fc910558a4cc82b",{defaultValue:"Contribution amount (₹)"})}<input required type="number" inputMode="decimal" min="1" max="1000000" step="0.01" value={form.amount} onChange={e=>setForm({...form,amount:e.target.value})}/><small>{localize("ui.30e62cf816e2cf0b",{defaultValue:"Continue to send this amount directly to the host’s UPI or bank account."})}</small></label>:<p>{localize("ui.b04dd28ab478baae",{defaultValue:"After adding your gift, you can view the host delivery address, purchase from the retailer and share tracking details here."})}</p>}
 <p className="finePrint">{localize("ui.ada703e1b6b59c56",{defaultValue:"Your contact details and contribution amount are shared only with the host."})}</p><button className="gc-launch" disabled={busy}>{busy?localize("ui.2e3b4a7d1f13e7c3",{defaultValue:"Saving your gift…"}):method==='LINK_AND_CONTRIBUTION'?localize("ui.75bda07643c2766a",{defaultValue:"Continue to host payment"}):localize("ui.0580e20a82366727",{defaultValue:"Add my special gift"})}</button>
 </form>}
 {!saved&&previous.length>0&&<section className="gc-panel"><h3>{localize("ui.76d4f865d4b5307f",{defaultValue:"Your gifts in this browser"})}</h3>{previous.map(g=><button key={g.id} type="button" className="gc-secondary" disabled={busy} onClick={()=>{setSaved(g);setAddress(null);setTracking('');setNotice('')}}>{localize("ui.d1f3b7c8d7b5ce7f",{defaultValue:"Manage "})}{g.name}</button>)}</section>}
 </>}{error&&<p role="alert" className="gc-error">{localizeText(error)}</p>}</div></div>}</>;
}
