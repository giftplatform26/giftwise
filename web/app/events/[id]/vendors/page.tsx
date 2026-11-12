'use client';
import {useCallback,useEffect,useState} from 'react';
import {useParams} from 'next/navigation';
import {localizeText} from '../../../../lib/localization.mjs';
import {useLocalizationRender} from '../../../../components/LocalizationProvider';
import {hostRequest} from '../../../../lib/host-api.mjs';
import {inr} from '../../../../lib/focused.mjs';
import {parseRupees,rupeesInput} from '../../../../lib/register.mjs';
import '../../../../components/gift-books.css';
import '../../../../components/eventops.css';
import '../../../../components/households.css';

type Quote={id:string;amountPaise:number;details:string|null;validUntil:string|null;status:string;expired:boolean};
type Payment={id:string;label:string;amountPaise:number;dueDate:string|null;paidOn:string|null;note:string|null;overdue:boolean;dueSoon:boolean};
type Vendor={id:string;directoryVendorId:string|null;name:string;category:string;contactName:string|null;phone:string|null;email:string|null;status:string;bookedPaise:number;paidPaise:number;balancePaise:number;scheduledPaise:number;nextDue:string|null;overdue:boolean;note:string|null;budgetLinked:boolean;version:number;quotes:Quote[];payments:Payment[]};
type Board={vendors:Vendor[];totals:{vendors:number;booked:number;bookedPaise:number;paidPaise:number;balancePaise:number};upcoming:{vendorId:string;vendor:string;paymentId:string;label:string;amountPaise:number;dueDate:string;overdue:boolean}[];
 shortlist:{id:string;name:string;category:string;city:string;phone:string|null;priceHint:string|null}[];categories:string[];access:string;budgetOn:boolean};
type VDraft={name:string;category:string;contactName:string;phone:string;email:string;note:string;booked:string};
const STATUS:Record<string,string>={ENQUIRED:'Asked for a quote',QUOTED:'Quote received',BOOKED:'Booked',CANCELLED:'Cancelled'};
const day=(d:string)=>new Intl.DateTimeFormat(undefined,{day:'numeric',month:'short'}).format(new Date(`${d}T12:00:00`));
const blank:VDraft={name:'',category:'Food',contactName:'',phone:'',email:'',note:'',booked:''};

/** Vendor quotes & bookings: compare quotes, book one, then track the advance and instalments. Feeds the budget when it is on. */
export default function Vendors(){useLocalizationRender();
 const {id}=useParams();const eventId=String(id??'');const route=`/v1/events/${eventId}/vendors`;
 const [b,setB]=useState<Board|null>(null),[error,setError]=useState(''),[notice,setNotice]=useState(''),[busy,setBusy]=useState(false),[openId,setOpenId]=useState<string|null>(null);
 const [form,setForm]=useState<VDraft|null>(null),[editing,setEditing]=useState<Vendor|null>(null),[quote,setQuote]=useState({amount:'',details:'',validUntil:''}),[pay,setPay]=useState({label:'Advance',amount:'',dueDate:'',paid:false});
 const load=useCallback(async()=>{try{setB(await hostRequest(fetch,route) as Board);setError('')}catch(e){setError(e instanceof Error?e.message:'Could not load vendors')}},[route]);
 useEffect(()=>{if(/^[0-9a-f-]{36}$/i.test(eventId))void load()},[eventId,load]);
 const ro=b?.access==='VIEW';
 async function call(fn:()=>Promise<unknown>,ok?:string){setBusy(true);setError('');try{await fn();if(ok)setNotice(ok);await load()}catch(e){setError(e instanceof Error?e.message:'Something went wrong')}finally{setBusy(false)}}
 function saveVendor(e:React.FormEvent){e.preventDefault();if(!form)return;let booked:number|null=null;try{booked=editing&&form.booked?parseRupees(form.booked):null}catch(err){setError(err instanceof Error?err.message:'Check the amount');return}
  const body={name:form.name,category:form.category,contactName:form.contactName||null,phone:form.phone||null,email:form.email||null,note:form.note||null,...(editing?{version:editing.version,bookedPaise:booked}:{})};
  void call(async()=>{await hostRequest(fetch,editing?`${route}/${editing.id}`:route,{method:editing?'PUT':'POST',payload:body});setForm(null);setEditing(null)},localizeText('Saved.'))}
 function addQuote(v:Vendor){let amount;try{amount=parseRupees(quote.amount)}catch(err){setError(err instanceof Error?err.message:'Check the amount');return}if(amount==null){setError(localizeText('Add the quoted amount'));return}
  void call(async()=>{await hostRequest(fetch,`${route}/${v.id}/quotes`,{method:'POST',payload:{amountPaise:amount,details:quote.details||null,validUntil:quote.validUntil||null}});setQuote({amount:'',details:'',validUntil:''})})}
 function addPayment(v:Vendor){let amount;try{amount=parseRupees(pay.amount)}catch(err){setError(err instanceof Error?err.message:'Check the amount');return}if(!amount){setError(localizeText('Add the amount'));return}
  const today=new Date().toISOString().slice(0,10);
  void call(async()=>{await hostRequest(fetch,`${route}/${v.id}/payments`,{method:'POST',payload:{label:pay.label,amountPaise:amount,dueDate:pay.dueDate||null,paidOn:pay.paid?(pay.dueDate||today):null}});setPay({label:'Balance',amount:'',dueDate:'',paid:false})})}
 const t=b?.totals;

 return <main className="focusPage gbPage hhPage">
  <header className="focusPageHeading"><div><p className="focusEyebrow">{localizeText('Quotes, bookings and payments')}</p><h1>{localizeText('Vendors & bookings')}</h1>
   <p>{localizeText('Add each vendor you talk to, note their quotes, accept the one you choose, and keep track of the advance and the payments still due.')}{b?.budgetOn?' '+localizeText('Booked vendors are added to your budget automatically.'):''}</p></div>
   {!ro&&<button type="button" className="focusPrimary" onClick={()=>{setEditing(null);setForm(blank)}}>{localizeText('Add a vendor')}</button>}</header>
  {error&&<p className="focusError" role="alert">{localizeText(error)}</p>}
  {notice&&<p className="gbNotice" role="status">{notice}</p>}
  {ro&&<p className="gbNotice">{localizeText('You can view this page. Ask the host for edit access to make changes.')}</p>}
  {t&&<section className="gbStats"><div><span>{localizeText('Vendors')}</span><strong>{t.vendors}</strong><small>{localizeText('{{n}} booked').replace('{{n}}',String(t.booked))}</small></div>
   <div><span>{localizeText('Booked')}</span><strong>{inr(t.bookedPaise)}</strong></div><div><span>{localizeText('Paid')}</span><strong>{inr(t.paidPaise)}</strong></div><div><span>{localizeText('Still to pay')}</span><strong>{inr(t.balancePaise)}</strong></div></section>}
  {b&&b.upcoming.length>0&&<section className="eoDues"><h2 className="gbSubhead">{localizeText('Payments due in the next 2 weeks')}</h2><ul className="gbRows">{b.upcoming.map(u=><li key={u.paymentId}><div>
   <span className="gbWho"><strong>{u.vendor}</strong><small>{u.label}</small></span><span className="gbWhat"><strong>{inr(u.amountPaise)}</strong><small className={u.overdue?'eoBad':''}>{day(u.dueDate)}{u.overdue?` · ${localizeText('overdue')}`:''}</small></span>
   {!ro&&<button type="button" className="gbPlain" disabled={busy} onClick={()=>void call(()=>hostRequest(fetch,`${route}/${u.vendorId}/payments/${u.paymentId}/paid`,{method:'POST',payload:{}}),localizeText('Marked as paid.'))}>{localizeText('Mark paid')}</button>}</div></li>)}</ul></section>}
  {b&&!ro&&b.shortlist.length>0&&<p className="gbFine">{localizeText('From your vendor shortlist')}: {b.shortlist.map(s=><button key={s.id} type="button" className="gbPlain" disabled={busy} onClick={()=>void call(()=>hostRequest(fetch,route,{method:'POST',payload:{directoryVendorId:s.id}}),localizeText('Added.'))}>+ {s.name}</button>)}</p>}

  {form&&<form className={`gbEntry${editing?' gbEntry--editing':''}`} onSubmit={saveVendor}><h2>{editing?localizeText('Edit vendor'):localizeText('Add a vendor')}</h2>
   <div className="gbEntryRow"><label className="gbGrow">{localizeText('Vendor name')}<input required value={form.name} maxLength={120} onChange={e=>setForm({...form,name:e.target.value})} placeholder={localizeText('e.g. Annapurna Caterers')}/></label>
    <label>{localizeText('Category')}<select value={form.category} onChange={e=>setForm({...form,category:e.target.value})}>{(b?.categories??[]).map(c=><option key={c} value={c}>{localizeText(c)}</option>)}</select></label></div>
   <div className="gbEntryRow"><label>{localizeText('Contact person')}<input value={form.contactName} maxLength={80} onChange={e=>setForm({...form,contactName:e.target.value})}/></label>
    <label>{localizeText('Phone')}<input type="tel" value={form.phone} maxLength={20} onChange={e=>setForm({...form,phone:e.target.value})}/></label>
    <label className="gbGrow">{localizeText('Email')}<input type="email" value={form.email} maxLength={160} onChange={e=>setForm({...form,email:e.target.value})}/></label>
    {editing&&editing.status==='BOOKED'&&<label>{localizeText('Booked amount (₹)')}<input inputMode="decimal" value={form.booked} onChange={e=>setForm({...form,booked:e.target.value})}/></label>}</div>
   <label>{localizeText('Note')}<input value={form.note} maxLength={500} onChange={e=>setForm({...form,note:e.target.value})}/></label>
   <div className="gbEntryActions"><button className="focusPrimary" disabled={busy}>{localizeText('Save')}</button><button type="button" className="gbPlain" onClick={()=>{setForm(null);setEditing(null)}}>{localizeText('Cancel')}</button>
    {editing&&<button type="button" className="gbDanger" onClick={()=>{if(confirm(localizeText('Delete {{n}} and its quotes and payments? Its budget line stays.').replace('{{n}}',editing.name)))void call(async()=>{await hostRequest(fetch,`${route}/${editing.id}/delete`,{method:'POST'});setForm(null);setEditing(null)})}}>{localizeText('Delete')}</button>}</div></form>}

  {b&&(b.vendors.length===0?<p className="gbEmpty">{localizeText('No vendors yet. Add the caterer, decorator or photographer you are talking to, then record their quotes.')}</p>:
   <ul className="gbRows hhRows">{b.vendors.map(v=><li key={v.id} className={`hhRow hhRow--${v.status==='BOOKED'?'yes':v.status==='CANCELLED'?'no':'pending'}`}><div>
    <button type="button" className="hhMain" aria-expanded={openId===v.id} onClick={()=>setOpenId(o=>o===v.id?null:v.id)}><span className="gbWho"><strong>{v.name}</strong>
     <small>{[localizeText(v.category),localizeText(STATUS[v.status]??v.status),v.quotes.length?localizeText('{{n}} quotes').replace('{{n}}',String(v.quotes.length)):null,v.budgetLinked?localizeText('in budget'):null].filter(Boolean).join(' · ')}</small></span>
     <span className="gbWhat"><strong>{v.status==='BOOKED'?inr(v.bookedPaise):v.quotes.length?localizeText('from {{a}}').replace('{{a}}',inr(Math.min(...v.quotes.map(q=>q.amountPaise)))):'—'}</strong>
      <small className={v.overdue?'eoBad':''}>{v.status==='BOOKED'?(v.balancePaise>0?localizeText('{{a}} to pay').replace('{{a}}',inr(v.balancePaise))+(v.nextDue?` · ${day(v.nextDue)}`:''):localizeText('Fully paid')):''}</small></span></button>
    {!ro&&<span className="hhQuick"><button type="button" className="gbPlain" onClick={()=>{setEditing(v);setForm({name:v.name,category:v.category,contactName:v.contactName??'',phone:v.phone??'',email:v.email??'',note:v.note??'',booked:rupeesInput(v.bookedPaise||null)});window.scrollTo({top:0,behavior:'smooth'})}}>{localizeText('Edit')}</button></span>}</div>
    {openId===v.id&&<div className="hhDetail">
     {(v.phone||v.email||v.contactName)&&<p className="gbFine">{[v.contactName,v.phone&&<a key="p" href={`tel:${v.phone}`}>{v.phone}</a>,v.email].filter(Boolean).map((x,i)=><span key={i}>{i?' · ':''}{x}</span>)}</p>}
     {v.note&&<p className="gbFine">{v.note}</p>}
     <h3 className="gbSubhead">{localizeText('Quotes')}</h3>
     {v.quotes.length===0?<p className="gbFine">{localizeText('No quotes yet.')}</p>:<ul className="gbRows">{v.quotes.map(q=><li key={q.id}><div><span className="gbWho"><strong>{inr(q.amountPaise)}</strong>
      <small>{[q.details,q.validUntil?localizeText('valid until {{d}}').replace('{{d}}',day(q.validUntil)):null,q.expired?localizeText('expired'):null,q.status==='ACCEPTED'?localizeText('accepted'):q.status==='REJECTED'?localizeText('not chosen'):null].filter(Boolean).join(' · ')}</small></span>
      {!ro&&<span className="hhQuick">{q.status!=='ACCEPTED'&&v.status!=='CANCELLED'&&<button type="button" className="focusSecondary" disabled={busy} onClick={()=>{if(confirm(localizeText('Book {{n}} at {{a}}?').replace('{{n}}',v.name).replace('{{a}}',inr(q.amountPaise))))void call(()=>hostRequest(fetch,`${route}/${v.id}/quotes/${q.id}/accept`,{method:'POST'}),localizeText('Booked.'))}}>{localizeText('Accept & book')}</button>}
       <button type="button" className="gbPlain" onClick={()=>void call(()=>hostRequest(fetch,`${route}/${v.id}/quotes/${q.id}/delete`,{method:'POST'}))}>✕</button></span>}</div></li>)}</ul>}
     {!ro&&v.status!=='CANCELLED'&&<div className="gbEntryRow"><label>{localizeText('Quoted (₹)')}<input inputMode="decimal" value={quote.amount} onChange={e=>setQuote({...quote,amount:e.target.value})}/></label>
      <label className="gbGrow">{localizeText('What’s included')}<input value={quote.details} maxLength={600} onChange={e=>setQuote({...quote,details:e.target.value})} placeholder={localizeText('e.g. 500 plates, veg, 2 live counters')}/></label>
      <label>{localizeText('Valid until')}<input type="date" value={quote.validUntil} onChange={e=>setQuote({...quote,validUntil:e.target.value})}/></label><button type="button" className="focusSecondary" disabled={busy} onClick={()=>addQuote(v)}>{localizeText('Add quote')}</button></div>}
     <h3 className="gbSubhead">{localizeText('Payment schedule')}</h3>
     {v.payments.length===0?<p className="gbFine">{localizeText('No payments planned yet.')}</p>:<ul className="gbRows">{v.payments.map(p=><li key={p.id}><div><span className="gbWho"><strong>{p.label}</strong>
      <small className={p.overdue?'eoBad':''}>{p.paidOn?localizeText('paid {{d}}').replace('{{d}}',day(p.paidOn)):p.dueDate?localizeText('due {{d}}').replace('{{d}}',day(p.dueDate))+(p.overdue?` · ${localizeText('overdue')}`:''):localizeText('no date')}</small></span>
      <span className="gbWhat"><strong>{inr(p.amountPaise)}</strong></span>
      {!ro&&<span className="hhQuick">{!p.paidOn&&<button type="button" className="gbPlain" disabled={busy} onClick={()=>void call(()=>hostRequest(fetch,`${route}/${v.id}/payments/${p.id}/paid`,{method:'POST',payload:{}}))}>{localizeText('Mark paid')}</button>}
       <button type="button" className="gbPlain" onClick={()=>void call(()=>hostRequest(fetch,`${route}/${v.id}/payments/${p.id}/delete`,{method:'POST'}))}>✕</button></span>}</div></li>)}</ul>}
     {v.status==='BOOKED'&&v.scheduledPaise!==v.bookedPaise&&<p className="gbFine">{localizeText('Planned payments add up to {{s}} of the {{b}} booked.').replace('{{s}}',inr(v.scheduledPaise)).replace('{{b}}',inr(v.bookedPaise))}</p>}
     {!ro&&v.status!=='CANCELLED'&&<div className="gbEntryRow"><label>{localizeText('Payment')}<input value={pay.label} maxLength={60} onChange={e=>setPay({...pay,label:e.target.value})}/></label>
      <label>{localizeText('Amount (₹)')}<input inputMode="decimal" value={pay.amount} onChange={e=>setPay({...pay,amount:e.target.value})}/></label>
      <label>{localizeText('Due')}<input type="date" value={pay.dueDate} onChange={e=>setPay({...pay,dueDate:e.target.value})}/></label>
      <label className="gbCheck"><input type="checkbox" checked={pay.paid} onChange={e=>setPay({...pay,paid:e.target.checked})}/>{localizeText('Already paid')}</label>
      <button type="button" className="focusSecondary" disabled={busy} onClick={()=>addPayment(v)}>{localizeText('Add payment')}</button></div>}
     {!ro&&v.status!=='CANCELLED'&&v.status==='BOOKED'&&<button type="button" className="gbDanger" onClick={()=>{if(confirm(localizeText('Mark {{n}} as cancelled? Payments already made stay recorded.').replace('{{n}}',v.name)))void call(()=>hostRequest(fetch,`${route}/${v.id}/cancel`,{method:'POST'}))}}>{localizeText('Cancel booking')}</button>}
    </div>}</li>)}</ul>)}
  <p className="gbFine">{localizeText('A planning record only — GiftWise does not pay vendors or hold money.')}</p>
 </main>;
}
