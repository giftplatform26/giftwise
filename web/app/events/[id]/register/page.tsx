'use client';
import {useCallback,useEffect,useMemo,useRef,useState} from 'react';
import {useParams} from 'next/navigation';
import {localizeText} from '../../../../lib/localization.mjs';
import {useLocalizationRender} from '../../../../components/LocalizationProvider';
import {hostRequest} from '../../../../lib/host-api.mjs';
import {inr} from '../../../../lib/focused.mjs';
import {GIFT_KINDS,KIND_LABEL,QUICK_AMOUNTS,parseRupees,rupeesInput,entryProblem,newClientKey,filterEntries} from '../../../../lib/register.mjs';
import '../../../../components/gift-books.css';

type Entry={id:string;giverName:string;giverPhone:string|null;side:string|null;kind:string;amountPaise:number|null;itemDescription:string|null;note:string|null;createdAt:string;version:number};
type Online={id:string;giverName:string;amountPaise:number;kind:string;createdAt:string};
type Register={entries:Entry[];online:Online[];totals:{inPersonPaise:number;onlinePaise:number;itemGifts:number;givers:number};sides:{side:string|null;amountPaise:number;entries:number}[];sideSuggestions:string[];nameSuggestions:string[];canManage:boolean};
type Draft={giverName:string;giverPhone:string;side:string;kind:string;amount:string;itemDescription:string;note:string};
type Queued={clientKey:string;payload:Record<string,unknown>};
const blank=(side=''):Draft=>({giverName:'',giverPhone:'',side,kind:'ENVELOPE',amount:'',itemDescription:'',note:''});
const time=(v:string)=>new Intl.DateTimeFormat(undefined,{day:'numeric',month:'short',hour:'numeric',minute:'2-digit'}).format(new Date(v));

export default function GiftRegister(){useLocalizationRender();
 const {id}=useParams();const eventId=String(id??'');const route=`/v1/events/${eventId}/register`;const queueKey=`gift.register.queue.${eventId}`;
 const [data,setData]=useState<Register|null>(null),[error,setError]=useState(''),[formError,setFormError]=useState(''),[busy,setBusy]=useState(false);
 const [draft,setDraft]=useState<Draft>(blank()),[more,setMore]=useState(false),[editing,setEditing]=useState<Entry|null>(null);
 const [query,setQuery]=useState(''),[view,setView]=useState<'ALL'|'IN_PERSON'|'ONLINE'>('ALL'),[toast,setToast]=useState<{text:string;undo?:()=>void}|null>(null),[queued,setQueued]=useState<Queued[]>([]);
 const nameRef=useRef<HTMLInputElement>(null);

 const load=useCallback(async()=>{try{setData(await hostRequest(fetch,route) as Register);setError('')}catch(e){setError(e instanceof Error?e.message:'Could not load the register')}},[route]);
 useEffect(()=>{if(/^[0-9a-f-]{36}$/i.test(eventId))void load();else setError('Event not found')},[eventId,load]);
 useEffect(()=>{try{setQueued(JSON.parse(localStorage.getItem(queueKey)||'[]'))}catch{}},[queueKey]);
 const saveQueue=(q:Queued[])=>{setQueued(q);try{localStorage.setItem(queueKey,JSON.stringify(q))}catch{}};

 // Entries saved while offline are retried with the same client key, so the server never records them twice.
 const flush=useCallback(async()=>{let q:Queued[];try{q=JSON.parse(localStorage.getItem(queueKey)||'[]')}catch{return}if(!q.length)return;
  const left:Queued[]=[];for(const item of q){try{await hostRequest(fetch,route,{method:'POST',payload:item.payload})}catch(e){if(e instanceof TypeError)left.push(item)}}
  saveQueue(left);if(left.length<q.length){setToast({text:localizeText('Saved entries that were waiting for a connection.')});void load()}},[queueKey,route,load]);
 useEffect(()=>{void flush();const t=setInterval(()=>{if(navigator.onLine)void flush()},20000);window.addEventListener('online',flush);return()=>{clearInterval(t);window.removeEventListener('online',flush)}},[flush]);
 useEffect(()=>{if(!toast)return;const t=setTimeout(()=>setToast(null),7000);return()=>clearTimeout(t)},[toast]);

 function payload(d:Draft){const amountPaise=parseRupees(d.amount);const problem=entryProblem({name:d.giverName,amountPaise,item:d.itemDescription,phone:d.giverPhone});if(problem)throw Error(problem);
  return {giverName:d.giverName,giverPhone:d.giverPhone||null,side:d.side||null,kind:d.kind,amountPaise,itemDescription:d.itemDescription||null,note:d.note||null};}

 async function add(e:React.FormEvent){e.preventDefault();setFormError('');let body:Record<string,unknown>;
  try{body={...payload(draft),clientKey:newClientKey()}}catch(err){setFormError(err instanceof Error?err.message:'Check the entry');return}
  const label=`${draft.giverName.trim()} · ${draft.amount?inr(body.amountPaise as number):draft.itemDescription}`;
  setBusy(true);
  try{const saved=await hostRequest(fetch,route,{method:'POST',payload:body}) as Entry;
   setToast({text:localizeText('Added {{entry}}').replace('{{entry}}',label),undo:data?.canManage===false?undefined:()=>void remove(saved,false)});void load()}
  catch(err){if(err instanceof TypeError){saveQueue([...queued,{clientKey:body.clientKey as string,payload:body}]);setToast({text:localizeText('No connection. {{entry}} is saved on this device and will sync automatically.').replace('{{entry}}',label)})}
   else{setFormError(err instanceof Error?err.message:'Could not add the entry');setBusy(false);return}}
  setDraft(blank(draft.side));setMore(false);setBusy(false);nameRef.current?.focus();}

 async function update(e:React.FormEvent){e.preventDefault();if(!editing)return;setFormError('');let body:Record<string,unknown>;
  try{body={...payload(draft),version:editing.version}}catch(err){setFormError(err instanceof Error?err.message:'Check the entry');return}
  setBusy(true);try{await hostRequest(fetch,`${route}/${editing.id}`,{method:'PUT',payload:body});setEditing(null);setDraft(blank(draft.side));setToast({text:localizeText('Entry updated.')});await load()}
  catch(err){setFormError(err instanceof Error?err.message:'Could not update the entry')}finally{setBusy(false)}}

 async function remove(entry:Entry,offerUndo=true){try{await hostRequest(fetch,`${route}/${entry.id}/delete`,{method:'POST'});if(editing?.id===entry.id){setEditing(null);setDraft(blank())}
  setToast(offerUndo?{text:localizeText('Removed {{name}}.').replace('{{name}}',entry.giverName),undo:()=>void restore(entry)}:{text:localizeText('Entry removed.')});await load()}
  catch(err){setError(err instanceof Error?err.message:'Could not remove the entry')}}
 async function restore(entry:Entry){try{await hostRequest(fetch,`${route}/${entry.id}/restore`,{method:'POST'});setToast({text:localizeText('Entry restored.')});await load()}catch(err){setError(err instanceof Error?err.message:'Could not restore')}}

 function startEdit(entry:Entry){setEditing(entry);setFormError('');setMore(Boolean(entry.giverPhone||entry.itemDescription||entry.note));
  setDraft({giverName:entry.giverName,giverPhone:entry.giverPhone??'',side:entry.side??'',kind:entry.kind,amount:rupeesInput(entry.amountPaise),itemDescription:entry.itemDescription??'',note:entry.note??''});
  window.scrollTo({top:0,behavior:'smooth'});setTimeout(()=>nameRef.current?.focus(),300)}

 const entries=useMemo(()=>filterEntries(data?.entries??[],query) as Entry[],[data,query]);
 const online=useMemo(()=>(data?.online??[]).filter(o=>!query||filterEntries([o],query).length),[data,query]);
 const set=(k:keyof Draft,v:string)=>setDraft(d=>({...d,[k]:v}));

 return <main className="focusPage gbPage">
  <header className="focusPageHeading"><div><p className="focusEyebrow">{localizeText('Shagun register')}</p><h1>{localizeText('Gift register')}</h1>
   <p>{localizeText('Note down envelopes, cash and gifts as they are handed to you. Confirmed online gifts appear here automatically.')}</p></div>
   {data?.canManage&&<a className="focusSecondary gbButtonLink" href={`/backend/api${route}/export.csv`} download>{localizeText('Download CSV')}</a>}</header>
  {error&&<p className="focusError" role="alert">{localizeText(error)} <button type="button" onClick={()=>void load()}>{localizeText('Retry')}</button></p>}
  {queued.length>0&&<p className="gbQueued" role="status">{localizeText('{{n}} entries are waiting for a connection and will sync automatically.').replace('{{n}}',String(queued.length))} <button type="button" onClick={()=>void flush()}>{localizeText('Try now')}</button></p>}
  {data&&<section className="gbStats" aria-label={localizeText('Totals')}>
   <div><span>{localizeText('In person')}</span><strong>{inr(data.totals.inPersonPaise)}</strong></div>
   <div><span>{localizeText('Confirmed online')}</span><strong>{inr(data.totals.onlinePaise)}</strong></div>
   <div><span>{localizeText('Givers')}</span><strong>{data.totals.givers}</strong></div>
   <div><span>{localizeText('Gift items')}</span><strong>{data.totals.itemGifts}</strong></div></section>}

  <form className={`gbEntry${editing?' gbEntry--editing':''}`} onSubmit={editing?update:add} aria-label={editing?localizeText('Edit entry'):localizeText('Add an entry')} noValidate>
   <h2>{editing?localizeText('Edit entry'):localizeText('Add an entry')}</h2>
   <div className="gbEntryRow">
    <label className="gbGrow">{localizeText('Name')}<input ref={nameRef} list="gb-names" value={draft.giverName} maxLength={120} autoComplete="off" required onChange={e=>set('giverName',e.target.value)} placeholder={localizeText('Who gave it?')}/></label>
    <label className="gbAmount">{localizeText('Amount (₹)')}<input inputMode="decimal" value={draft.amount} onChange={e=>set('amount',e.target.value)} placeholder="2100"/></label>
   </div>
   <div className="gbChips" role="group" aria-label={localizeText('Quick amounts')}>{QUICK_AMOUNTS.map(a=><button type="button" key={a} aria-pressed={parseSafe(draft.amount)===a*100} onClick={()=>set('amount',String(a))}>₹{a.toLocaleString('en-IN')}</button>)}</div>
   <div className="gbSegment" role="radiogroup" aria-label={localizeText('What was given')}>{GIFT_KINDS.map(([k,label])=><label key={k}><input type="radio" name="kind" checked={draft.kind===k} onChange={()=>{set('kind',k);if(k==='GIFT_ITEM')setMore(true)}}/><span>{localizeText(label)}</span></label>)}</div>
   <label>{localizeText('Side or family group')}<input list="gb-sides" value={draft.side} maxLength={60} onChange={e=>set('side',e.target.value)} placeholder={localizeText('e.g. Bride side, Office friends')}/><small>{localizeText('Kept for the next entry, so you can record a whole family quickly.')}</small></label>
   <button type="button" className="gbMore" aria-expanded={more} onClick={()=>setMore(m=>!m)}>{more?localizeText('Fewer details'):localizeText('Phone, gift item or note')}</button>
   {more&&<div className="gbMoreFields">
    <label>{localizeText('Gift item')}<input value={draft.itemDescription} maxLength={200} onChange={e=>set('itemDescription',e.target.value)} placeholder={localizeText('e.g. Silver lamp')}/></label>
    <label>{localizeText('Phone (optional)')}<input type="tel" inputMode="tel" value={draft.giverPhone} maxLength={20} onChange={e=>set('giverPhone',e.target.value)}/></label>
    <label className="gbFull">{localizeText('Note')}<input value={draft.note} maxLength={300} onChange={e=>set('note',e.target.value)}/></label></div>}
   {formError&&<p className="focusError" role="alert">{localizeText(formError)}</p>}
   <div className="gbEntryActions"><button className="focusPrimary" disabled={busy}>{busy?localizeText('Saving…'):editing?localizeText('Save changes'):localizeText('Add to register')}</button>
    {editing&&<><button type="button" className="focusTextLink gbPlain" onClick={()=>{setEditing(null);setDraft(blank(draft.side));setFormError('')}}>{localizeText('Cancel')}</button>
     <button type="button" className="gbDanger" onClick={()=>void remove(editing)}>{localizeText('Remove entry')}</button></>}</div>
   <datalist id="gb-names">{data?.nameSuggestions.map(n=><option key={n} value={n}/>)}</datalist>
   <datalist id="gb-sides">{data?.sideSuggestions.map(n=><option key={n} value={n}/>)}</datalist>
  </form>

  {data&&<section className="gbList" aria-label={localizeText('Register entries')}>
   <div className="gbListTools"><input type="search" value={query} onChange={e=>setQuery(e.target.value)} placeholder={localizeText('Search name, side or gift')} aria-label={localizeText('Search the register')}/>
    <div className="gbTabs" role="tablist">{(['ALL','IN_PERSON','ONLINE'] as const).map(v=><button key={v} role="tab" type="button" aria-selected={view===v} onClick={()=>setView(v)}>{localizeText(v==='ALL'?'All':v==='IN_PERSON'?'In person':'Online')}</button>)}</div></div>
   {data.sides.length>1&&<div className="gbSides">{data.sides.map(s=><button type="button" key={s.side??'-'} onClick={()=>setQuery(s.side??'')}><span>{s.side??localizeText('No side')}</span><strong>{inr(s.amountPaise)}</strong><small>{s.entries}</small></button>)}</div>}
   {view!=='ONLINE'&&(entries.length===0?<p className="gbEmpty">{query?localizeText('No entries match your search.'):localizeText('Nothing recorded yet. The first envelope is the hardest!')}</p>:
    <ul className="gbRows">{entries.map(e=><li key={e.id}><button type="button" disabled={!data.canManage} onClick={()=>startEdit(e)} aria-label={data.canManage?localizeText('Edit {{name}}').replace('{{name}}',e.giverName):e.giverName}>
     <span className="gbWho"><strong>{e.giverName}</strong><small>{[e.side,KIND_LABEL[e.kind]&&localizeText(KIND_LABEL[e.kind]),time(e.createdAt)].filter(Boolean).join(' · ')}</small></span>
     <span className="gbWhat">{e.amountPaise!=null&&<strong>{inr(e.amountPaise)}</strong>}{e.itemDescription&&<small>{e.itemDescription}</small>}</span></button></li>)}</ul>)}
   {view!=='IN_PERSON'&&online.length>0&&<><h3 className="gbSubhead">{localizeText('Confirmed online gifts')}</h3><ul className="gbRows gbRows--online">{online.map(o=><li key={o.id}><div>
     <span className="gbWho"><strong>{o.giverName}</strong><small>{localizeText(KIND_LABEL[o.kind]??'Online gift')} · {time(o.createdAt)}</small></span>
     <span className="gbWhat"><strong>{inr(o.amountPaise)}</strong></span></div></li>)}</ul></>}
   {view==='ONLINE'&&online.length===0&&<p className="gbEmpty">{localizeText('No confirmed online gifts yet.')}</p>}
  </section>}
  <p className="gbFine">{localizeText('This register is your private record. It does not confirm or move any money.')}</p>
  {toast&&<div className="gbToast" role="status">{toast.text}{toast.undo&&<button type="button" onClick={()=>{toast.undo?.();setToast(null)}}>{localizeText('Undo')}</button>}</div>}
 </main>;
}
function parseSafe(v:string){try{return parseRupees(v)}catch{return null}}
