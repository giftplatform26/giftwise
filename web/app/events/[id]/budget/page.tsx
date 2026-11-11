'use client';
import {useCallback,useEffect,useState} from 'react';
import {useParams} from 'next/navigation';
import {localizeText} from '../../../../lib/localization.mjs';
import {useLocalizationRender} from '../../../../components/LocalizationProvider';
import {hostRequest} from '../../../../lib/host-api.mjs';
import {inr} from '../../../../lib/focused.mjs';
import {parseRupees,rupeesInput} from '../../../../lib/register.mjs';
import {budgetHealth,percent} from '../../../../lib/eventops.mjs';
import '../../../../components/gift-books.css';
import '../../../../components/eventops.css';

type Line={id:string;category:string;item:string;vendorName:string|null;plannedPaise:number;committedPaise:number;paidPaise:number;balancePaise:number;dueDate:string|null;dueSoon:boolean;overdue:boolean;note:string|null;version:number};
type Budget={summary:{totalBudgetPaise:number|null;plannedPaise:number;committedPaise:number;paidPaise:number;balanceDuePaise:number;remainingPaise:number|null};lines:Line[];categories:{category:string;plannedPaise:number;committedPaise:number;paidPaise:number}[];upcoming:Line[];categoryChoices:string[]};
type Draft={category:string;item:string;vendorName:string;planned:string;committed:string;paid:string;dueDate:string;note:string};
const empty:Draft={category:'Food',item:'',vendorName:'',planned:'',committed:'',paid:'',dueDate:'',note:''};
const day=(d:string)=>new Intl.DateTimeFormat(undefined,{day:'numeric',month:'short'}).format(new Date(`${d}T12:00:00`));

export default function EventBudget(){useLocalizationRender();
 const {id}=useParams();const eventId=String(id??'');const route=`/v1/events/${eventId}/budget`;
 const [b,setB]=useState<Budget|null>(null),[error,setError]=useState(''),[busy,setBusy]=useState(false),[draft,setDraft]=useState<Draft>(empty),[editing,setEditing]=useState<Line|null>(null),[open,setOpen]=useState(false),[total,setTotal]=useState('');
 const load=useCallback(async()=>{try{const v=await hostRequest(fetch,route) as Budget;setB(v);setTotal(rupeesInput(v.summary.totalBudgetPaise));setError('')}catch(e){setError(e instanceof Error?e.message:'Could not load the budget')}},[route]);
 useEffect(()=>{if(/^[0-9a-f-]{36}$/i.test(eventId))void load()},[eventId,load]);
 const set=(k:keyof Draft,v:string)=>setDraft(d=>({...d,[k]:v}));
 async function saveTotal(){try{await hostRequest(fetch,`${route}/total`,{method:'PUT',payload:{totalPaise:parseRupees(total)}});await load()}catch(e){setError(e instanceof Error?e.message:'Could not save')}}
 async function save(e:React.FormEvent){e.preventDefault();setError('');let body;
  try{body={category:draft.category,item:draft.item,vendorName:draft.vendorName||null,plannedPaise:parseRupees(draft.planned)??0,committedPaise:parseRupees(draft.committed)??0,paidPaise:parseRupees(draft.paid)??0,dueDate:draft.dueDate||null,note:draft.note||null,...(editing?{version:editing.version}:{})}}
  catch(err){setError(err instanceof Error?err.message:'Check the amounts');return}
  setBusy(true);try{await hostRequest(fetch,editing?`${route}/lines/${editing.id}`:`${route}/lines`,{method:editing?'PUT':'POST',payload:body});setDraft({...empty,category:draft.category});setEditing(null);setOpen(false);await load()}
  catch(err){setError(err instanceof Error?err.message:'Could not save')}finally{setBusy(false)}}
 async function remove(l:Line){if(!confirm(localizeText('Delete “{{item}}”?').replace('{{item}}',l.item)))return;try{await hostRequest(fetch,`${route}/lines/${l.id}/delete`,{method:'POST'});setEditing(null);setOpen(false);await load()}catch(e){setError(e instanceof Error?e.message:'Could not delete')}}
 function edit(l:Line){setEditing(l);setOpen(true);setDraft({category:l.category,item:l.item,vendorName:l.vendorName??'',planned:rupeesInput(l.plannedPaise||null),committed:rupeesInput(l.committedPaise||null),paid:rupeesInput(l.paidPaise||null),dueDate:l.dueDate??'',note:l.note??''});window.scrollTo({top:0,behavior:'smooth'})}
 const s=b?.summary;const health=budgetHealth(s);

 return <main className="focusPage gbPage">
  <header className="focusPageHeading"><div><p className="focusEyebrow">{localizeText('Private to you')}</p><h1>{localizeText('Budget')}</h1>
   <p>{localizeText('What you planned, what you have booked and what you have paid, with the payments coming up. Only you can see this page.')}</p></div>
   <button type="button" className="focusPrimary" onClick={()=>{setEditing(null);setDraft(empty);setOpen(true)}}>{localizeText('Add an expense')}</button></header>
  {error&&<p className="focusError" role="alert">{localizeText(error)}</p>}
  {s&&<section className="eoBudgetTop">
   <div className="eoTotal"><label>{localizeText('Total budget (₹)')}<input inputMode="decimal" value={total} onChange={e=>setTotal(e.target.value)} onBlur={()=>void saveTotal()} placeholder={localizeText('e.g. 10,00,000')}/></label>
    <p className={`eoHealth eoHealth--${health.tone}`}>{localizeText(health.label)}</p>
    {s.totalBudgetPaise?<div className="eoBar" aria-hidden="true"><span className="eoBar--paid" style={{width:`${percent(s.paidPaise,s.totalBudgetPaise)}%`}}/><span className="eoBar--booked" style={{width:`${Math.max(0,percent(s.committedPaise,s.totalBudgetPaise)-percent(s.paidPaise,s.totalBudgetPaise))}%`}}/></div>:null}</div>
   <div className="gbStats"><div><span>{localizeText('Planned')}</span><strong>{inr(s.plannedPaise)}</strong></div><div><span>{localizeText('Booked')}</span><strong>{inr(s.committedPaise)}</strong></div>
    <div><span>{localizeText('Paid')}</span><strong>{inr(s.paidPaise)}</strong></div><div><span>{localizeText('Still to pay')}</span><strong>{inr(s.balanceDuePaise)}</strong></div></div></section>}
  {b&&b.upcoming.length>0&&<section className="eoDues"><h2 className="gbSubhead">{localizeText('Payments due in the next 2 weeks')}</h2><ul className="gbRows">{b.upcoming.map(l=><li key={l.id}><button type="button" onClick={()=>edit(l)}>
   <span className="gbWho"><strong>{l.vendorName||l.item}</strong><small>{l.item} · {localizeText(l.category)}</small></span><span className="gbWhat"><strong>{inr(l.balancePaise)}</strong><small className={l.overdue?'eoBad':''}>{l.dueDate&&day(l.dueDate)}</small></span></button></li>)}</ul></section>}
  {open&&b&&<form className={`gbEntry${editing?' gbEntry--editing':''}`} onSubmit={save}><h2>{editing?localizeText('Edit expense'):localizeText('Add an expense')}</h2>
   <div className="gbEntryRow"><label>{localizeText('Category')}<select value={draft.category} onChange={e=>set('category',e.target.value)}>{b.categoryChoices.map(c=><option key={c} value={c}>{localizeText(c)}</option>)}</select></label>
    <label className="gbGrow">{localizeText('What')}<input value={draft.item} maxLength={120} onChange={e=>set('item',e.target.value)} placeholder={localizeText('e.g. Reception dinner')}/></label>
    <label className="gbGrow">{localizeText('Vendor')}<input value={draft.vendorName} maxLength={120} onChange={e=>set('vendorName',e.target.value)}/></label></div>
   <div className="gbEntryRow"><label>{localizeText('Planned (₹)')}<input inputMode="decimal" value={draft.planned} onChange={e=>set('planned',e.target.value)}/></label>
    <label>{localizeText('Booked / quoted (₹)')}<input inputMode="decimal" value={draft.committed} onChange={e=>set('committed',e.target.value)}/></label>
    <label>{localizeText('Paid so far (₹)')}<input inputMode="decimal" value={draft.paid} onChange={e=>set('paid',e.target.value)}/></label>
    <label>{localizeText('Next payment due')}<input type="date" value={draft.dueDate} onChange={e=>set('dueDate',e.target.value)}/></label></div>
   <label>{localizeText('Note')}<input value={draft.note} maxLength={300} onChange={e=>set('note',e.target.value)}/></label>
   <div className="gbEntryActions"><button className="focusPrimary" disabled={busy}>{localizeText('Save')}</button><button type="button" className="gbPlain" onClick={()=>{setOpen(false);setEditing(null)}}>{localizeText('Cancel')}</button>
    {editing&&<button type="button" className="gbDanger" onClick={()=>void remove(editing)}>{localizeText('Delete')}</button>}</div></form>}
  {b&&(b.lines.length===0?<p className="gbEmpty">{localizeText('Add your first expense — venue, food and decor are usually the biggest.')}</p>:
   b.categories.map(c=><section key={c.category} className="eoCat"><h2 className="gbSubhead">{localizeText(c.category)} <small>{inr(Math.max(c.plannedPaise,c.committedPaise))}</small></h2>
    <ul className="gbRows">{b.lines.filter(l=>l.category===c.category).map(l=><li key={l.id}><button type="button" onClick={()=>edit(l)}>
     <span className="gbWho"><strong>{l.item}</strong><small>{[l.vendorName,l.plannedPaise?localizeText('planned {{a}}').replace('{{a}}',inr(l.plannedPaise)):null].filter(Boolean).join(' · ')}</small></span>
     <span className="gbWhat"><strong>{inr(l.committedPaise||l.plannedPaise)}</strong><small>{l.committedPaise?(l.balancePaise>0?localizeText('{{a}} to pay').replace('{{a}}',inr(l.balancePaise)):localizeText('Fully paid')):localizeText('Not booked yet')}</small></span></button></li>)}</ul></section>))}
  <p className="gbFine">{localizeText('A planning aid only — GiftWise does not pay vendors.')}</p>
 </main>;
}
