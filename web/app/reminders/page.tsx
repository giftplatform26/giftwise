'use client';
import {useCallback,useEffect,useState} from 'react';
import Link from 'next/link';
import {localizeText} from '../../lib/localization.mjs';
import {useLocalizationRender} from '../../components/LocalizationProvider';
import {hostRequest} from '../../lib/host-api.mjs';
import {inr} from '../../lib/focused.mjs';
import {whenLabel,OCCASION_CHOICES} from '../../lib/reminders.mjs';
import '../../components/gift-books.css';
import '../../components/growth-pages.css';

type Reminder={id:string;personName:string;occasion:string;month:number;day:number;sinceYear:number|null;note:string|null;next:string;daysAway:number;turning:number|null;lastGivenPaise:number|null;lastGivenOn:string|null};
type Feed={upcoming:Reminder[];all:Reminder[]};
const MONTHS=Array.from({length:12},(_,i)=>new Intl.DateTimeFormat(undefined,{month:'long'}).format(new Date(2000,i,1)));

export default function Reminders(){useLocalizationRender();
 const [feed,setFeed]=useState<Feed|null>(null),[error,setError]=useState(''),[open,setOpen]=useState(false),[busy,setBusy]=useState(false);
 const [f,setF]=useState({personName:'',occasion:'Birthday',month:'',day:'',sinceYear:'',note:''});
 const load=useCallback(async()=>{try{setFeed(await hostRequest(fetch,'/v1/me/reminders') as Feed);setError('')}catch(e){setError(e instanceof Error?e.message:'Could not load')}},[]);
 useEffect(()=>{void load()},[load]);
 async function add(e:React.FormEvent){e.preventDefault();setBusy(true);setError('');
  try{await hostRequest(fetch,'/v1/me/reminders',{method:'POST',payload:{personName:f.personName,occasion:f.occasion,month:Number(f.month)||null,day:Number(f.day)||null,sinceYear:f.sinceYear?Number(f.sinceYear):null,note:f.note||null}});
   setF({personName:'',occasion:f.occasion,month:'',day:'',sinceYear:'',note:''});setOpen(false);await load()}catch(err){setError(err instanceof Error?err.message:'Could not save')}finally{setBusy(false)}}
 async function remove(r:Reminder){if(!confirm(localizeText('Forget {{name}}’s {{occasion}}?').replace('{{name}}',r.personName).replace('{{occasion}}',r.occasion.toLowerCase())))return;try{await hostRequest(fetch,`/v1/me/reminders/${r.id}/delete`,{method:'POST'});await load()}catch(err){setError(err instanceof Error?err.message:'Could not remove')}}
 const card=(r:Reminder,big=false)=><li key={r.id} className={big?'rmCard':''}><div>
  <span className="gbWho"><strong>{r.personName}</strong><small>{localizeText(r.occasion)}{r.turning?` · ${localizeText('turns {{n}}').replace('{{n}}',String(r.turning))}`:''} · {new Intl.DateTimeFormat(undefined,{day:'numeric',month:'long'}).format(new Date(`${r.next}T12:00:00`))}</small>
   {r.lastGivenPaise!=null&&<small className="rmLast">{localizeText('Last time you gave {{amount}}').replace('{{amount}}',inr(r.lastGivenPaise))}</small>}</span>
  <span className="rmRight"><span className={`rmWhen${r.daysAway<=7?' rmWhen--soon':''}`}>{localizeText(whenLabel(r.daysAway))}</span><button type="button" className="gbPlain" aria-label={localizeText('Remove')} onClick={()=>void remove(r)}>✕</button></span></div></li>;

 return <main className="focusPage gbPage">
  <header className="focusPageHeading"><div><p className="focusEyebrow">{localizeText('Never miss a day')}</p><h1>{localizeText('Dates to remember')}</h1>
   <p>{localizeText('Birthdays and anniversaries of the people you love, with what you gave last time from your gift ledger.')}</p></div>
   <button type="button" className="focusPrimary" onClick={()=>setOpen(true)}>{localizeText('Add a date')}</button></header>
  {error&&<p className="focusError" role="alert">{localizeText(error)}</p>}
  {open&&<form className="gbEntry" onSubmit={add}><h2>{localizeText('Add a date')}</h2>
   <div className="gbEntryRow"><label className="gbGrow">{localizeText('Whose?')}<input autoFocus value={f.personName} maxLength={120} onChange={e=>setF({...f,personName:e.target.value})} placeholder={localizeText('e.g. Ravi Kumar')}/></label>
    <label>{localizeText('Occasion')}<input list="rm-occasions" value={f.occasion} maxLength={40} onChange={e=>setF({...f,occasion:e.target.value})}/></label></div>
   <div className="gbEntryRow"><label>{localizeText('Month')}<select value={f.month} onChange={e=>setF({...f,month:e.target.value})}><option value="">—</option>{MONTHS.map((m,i)=><option key={m} value={i+1}>{m}</option>)}</select></label>
    <label>{localizeText('Day')}<input inputMode="numeric" value={f.day} maxLength={2} onChange={e=>setF({...f,day:e.target.value.replace(/\D/g,'')})}/></label>
    <label>{localizeText('Year (optional)')}<input inputMode="numeric" value={f.sinceYear} maxLength={4} onChange={e=>setF({...f,sinceYear:e.target.value.replace(/\D/g,'')})} placeholder="1990"/><small>{localizeText('So we can say “turns 35”')}</small></label></div>
   <div className="gbEntryActions"><button className="focusPrimary" disabled={busy}>{localizeText('Save')}</button><button type="button" className="gbPlain" onClick={()=>setOpen(false)}>{localizeText('Cancel')}</button></div>
   <datalist id="rm-occasions">{OCCASION_CHOICES.map((o:string)=><option key={o} value={localizeText(o)}/>)}</datalist></form>}
  {feed&&<>
   <h2 className="gbSubhead">{localizeText('Coming up')}</h2>
   {feed.upcoming.length===0?<p className="gbEmpty">{localizeText('Nothing in the next six weeks.')}</p>:<ul className="gbRows">{feed.upcoming.map(r=>card(r,true))}</ul>}
   {feed.all.length>feed.upcoming.length&&<><h2 className="gbSubhead">{localizeText('All dates')}</h2><ul className="gbRows">{feed.all.filter(r=>!feed.upcoming.includes(r)).map(r=>card(r))}</ul></>}
   <p className="gbFine">{localizeText('Planning something for them?')} <Link href="/events/new">{localizeText('Create a celebration')}</Link></p></>}
 </main>;
}
