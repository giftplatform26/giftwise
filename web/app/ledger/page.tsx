'use client';
import {useCallback,useEffect,useMemo,useState} from 'react';
import {localizeText} from '../../lib/localization.mjs';
import {useLocalizationRender} from '../../components/LocalizationProvider';
import {hostRequest} from '../../lib/host-api.mjs';
import {inr} from '../../lib/focused.mjs';
import {GIFT_KINDS,KIND_LABEL,QUICK_AMOUNTS,parseRupees,rupeesInput,entryProblem,personKey,balance} from '../../lib/register.mjs';
import '../../components/gift-books.css';

type Given={id:string;recipientName:string;recipientPhone:string|null;occasion:string|null;occasionDate:string|null;kind:string;amountPaise:number|null;itemDescription:string|null;note:string|null;version:number};
type Received={id:string;giverName:string;kind:string;amountPaise:number|null;itemDescription:string|null;eventTitle:string;eventDate:string|null;createdAt:string};
type Person={key:string;name:string;givenPaise:number;givenCount:number;receivedPaise:number;receivedCount:number;lastGiven:string|null;lastReceived:string|null};
type Ledger={given:Given[];givenPaise:number;receivedPaise:number;people:Person[]};
type History={name:string;given:Given[];received:Received[];givenPaise:number;receivedPaise:number};
type Draft={recipientName:string;recipientPhone:string;occasion:string;occasionDate:string;kind:string;amount:string;itemDescription:string;note:string};
const blank=():Draft=>({recipientName:'',recipientPhone:'',occasion:'',occasionDate:'',kind:'ENVELOPE',amount:'',itemDescription:'',note:''});
const day=(v:string|null)=>v?new Intl.DateTimeFormat(undefined,{dateStyle:'medium'}).format(new Date(v.length===10?`${v}T12:00:00`:v)):'';
const OCCASIONS=['Wedding','Birthday','Housewarming','Baby shower','Anniversary','Naming ceremony','Retirement'];

export default function GiftLedger(){useLocalizationRender();
 const [data,setData]=useState<Ledger|null>(null),[error,setError]=useState(''),[tab,setTab]=useState<'PEOPLE'|'GIVEN'>('PEOPLE');
 const [query,setQuery]=useState(''),[person,setPerson]=useState<History|null>(null),[open,setOpen]=useState(false),[editing,setEditing]=useState<Given|null>(null);
 const [draft,setDraft]=useState<Draft>(blank()),[formError,setFormError]=useState(''),[busy,setBusy]=useState(false),[toast,setToast]=useState<{text:string;undo?:()=>void}|null>(null);
 const load=useCallback(async()=>{try{setData(await hostRequest(fetch,'/v1/me/ledger') as Ledger);setError('')}catch(e){setError(e instanceof Error?e.message:'Could not load your ledger')}},[]);
 useEffect(()=>{void load()},[load]);
 useEffect(()=>{if(!toast)return;const t=setTimeout(()=>setToast(null),7000);return()=>clearTimeout(t)},[toast]);
 async function lookup(name:string){if(!personKey(name))return;try{setPerson(await hostRequest(fetch,`/v1/me/ledger/person?name=${encodeURIComponent(name)}`) as History)}catch(e){setError(e instanceof Error?e.message:'Could not look up this person')}}

 function startAdd(name=''){setEditing(null);setDraft({...blank(),recipientName:name});setFormError('');setOpen(true)}
 function startEdit(g:Given){setEditing(g);setFormError('');setOpen(true);
  setDraft({recipientName:g.recipientName,recipientPhone:g.recipientPhone??'',occasion:g.occasion??'',occasionDate:g.occasionDate??'',kind:g.kind,amount:rupeesInput(g.amountPaise),itemDescription:g.itemDescription??'',note:g.note??''})}
 async function save(e:React.FormEvent){e.preventDefault();setFormError('');let amountPaise:number|null;
  try{amountPaise=parseRupees(draft.amount)}catch(err){setFormError(err instanceof Error?err.message:'Check the amount');return}
  const problem=entryProblem({name:draft.recipientName,amountPaise,item:draft.itemDescription,phone:draft.recipientPhone});if(problem){setFormError(problem);return}
  const body={recipientName:draft.recipientName,recipientPhone:draft.recipientPhone||null,occasion:draft.occasion||null,occasionDate:draft.occasionDate||null,kind:draft.kind,amountPaise,itemDescription:draft.itemDescription||null,note:draft.note||null,...(editing?{version:editing.version}:{})};
  setBusy(true);try{await hostRequest(fetch,editing?`/v1/me/ledger/given/${editing.id}`:'/v1/me/ledger/given',{method:editing?'PUT':'POST',payload:body});
   setOpen(false);setToast({text:localizeText(editing?'Gift updated.':'Gift added to your ledger.')});await load();if(person)await lookup(person.name)}
  catch(err){setFormError(err instanceof Error?err.message:'Could not save')}finally{setBusy(false)}}
 async function remove(g:Given){try{await hostRequest(fetch,`/v1/me/ledger/given/${g.id}/delete`,{method:'POST'});setOpen(false);
  setToast({text:localizeText('Removed the gift to {{name}}.').replace('{{name}}',g.recipientName),undo:()=>void hostRequest(fetch,`/v1/me/ledger/given/${g.id}/restore`,{method:'POST'}).then(load)});await load();if(person)await lookup(person.name)}
  catch(err){setError(err instanceof Error?err.message:'Could not remove')}}

 const people=useMemo(()=>{const q=personKey(query);return (data?.people??[]).filter(p=>!q||p.key.includes(q))},[data,query]);
 const given=useMemo(()=>{const q=personKey(query);return (data?.given??[]).filter(g=>!q||personKey(`${g.recipientName} ${g.occasion??''}`).includes(q))},[data,query]);
 const set=(k:keyof Draft,v:string)=>setDraft(d=>({...d,[k]:v}));
 const max=Math.max(1,...people.map(p=>Math.max(p.givenPaise,p.receivedPaise)));

 return <main className="focusPage gbPage">
  <header className="focusPageHeading"><div><p className="focusEyebrow">{localizeText('Private to you')}</p><h1>{localizeText('Gift ledger')}</h1>
   <p>{localizeText('Remember what you gave and what you received, so the next invitation is never a guess.')}</p></div>
   <button type="button" className="focusPrimary" onClick={()=>startAdd()}>{localizeText('Add a gift I gave')}</button></header>
  {error&&<p className="focusError" role="alert">{localizeText(error)} <button type="button" onClick={()=>void load()}>{localizeText('Retry')}</button></p>}
  {data&&<section className="gbStats gbStats--three" aria-label={localizeText('Totals')}>
   <div><span>{localizeText('You gave')}</span><strong>{inr(data.givenPaise)}</strong></div>
   <div><span>{localizeText('You received')}</span><strong>{inr(data.receivedPaise)}</strong></div>
   <div><span>{localizeText('People')}</span><strong>{data.people.length}</strong></div></section>}

  {person&&<section className="gbPerson" aria-labelledby="gb-person">
   <header><div><p className="focusEyebrow">{localizeText('Between you and')}</p><h2 id="gb-person">{person.name}</h2></div><button type="button" className="gbPlain" onClick={()=>setPerson(null)} aria-label={localizeText('Close')}>✕</button></header>
   <p className="gbBalance">{(()=>{const b=balance(person);return b>0?localizeText('{{name}} has given you {{amount}} more than you have given them.').replace('{{name}}',person.name).replace('{{amount}}',inr(b)):b<0?localizeText('You have given {{name}} {{amount}} more than you have received.').replace('{{name}}',person.name).replace('{{amount}}',inr(-b)):localizeText('You are even.')})()}</p>
   <div className="gbPersonCols">
    <div><h3>{localizeText('They gave you')} · {inr(person.receivedPaise)}</h3>{person.received.length===0?<p className="gbEmpty">{localizeText('Nothing recorded at your celebrations yet.')}</p>:<ul>{person.received.map(r=><li key={r.id}><strong>{r.amountPaise!=null?inr(r.amountPaise):r.itemDescription}</strong><small>{r.eventTitle} · {day(r.eventDate??r.createdAt)} · {localizeText(KIND_LABEL[r.kind]??r.kind)}</small></li>)}</ul>}</div>
    <div><h3>{localizeText('You gave them')} · {inr(person.givenPaise)}</h3>{person.given.length===0?<p className="gbEmpty">{localizeText('No gifts recorded yet.')}</p>:<ul>{person.given.map(g=><li key={g.id}><button type="button" onClick={()=>startEdit(g)}><strong>{g.amountPaise!=null?inr(g.amountPaise):g.itemDescription}</strong><small>{[g.occasion,day(g.occasionDate),localizeText(KIND_LABEL[g.kind]??g.kind)].filter(Boolean).join(' · ')}</small></button></li>)}</ul>}
     <button type="button" className="focusSecondary" onClick={()=>startAdd(person.name)}>{localizeText('Add a gift to {{name}}').replace('{{name}}',person.name)}</button></div>
   </div></section>}

  {open&&<form className="gbEntry" onSubmit={save} noValidate aria-label={editing?localizeText('Edit gift'):localizeText('Add a gift I gave')}>
   <h2>{editing?localizeText('Edit gift'):localizeText('A gift I gave')}</h2>
   <div className="gbEntryRow"><label className="gbGrow">{localizeText('To')}<input autoFocus value={draft.recipientName} maxLength={120} list="gb-people" onChange={e=>set('recipientName',e.target.value)} placeholder={localizeText('Name of the person or family')}/></label>
    <label className="gbAmount">{localizeText('Amount (₹)')}<input inputMode="decimal" value={draft.amount} onChange={e=>set('amount',e.target.value)} placeholder="1100"/></label></div>
   <div className="gbChips" role="group" aria-label={localizeText('Quick amounts')}>{QUICK_AMOUNTS.map(a=><button type="button" key={a} onClick={()=>set('amount',String(a))}>₹{a.toLocaleString('en-IN')}</button>)}</div>
   <div className="gbSegment" role="radiogroup" aria-label={localizeText('What you gave')}>{GIFT_KINDS.map(([k,label])=><label key={k}><input type="radio" name="kind" checked={draft.kind===k} onChange={()=>set('kind',k)}/><span>{localizeText(label)}</span></label>)}</div>
   <div className="gbEntryRow"><label className="gbGrow">{localizeText('Occasion')}<input list="gb-occasions" value={draft.occasion} maxLength={80} onChange={e=>set('occasion',e.target.value)} placeholder={localizeText('e.g. Daughter’s wedding')}/></label>
    <label>{localizeText('Date')}<input type="date" value={draft.occasionDate} onChange={e=>set('occasionDate',e.target.value)}/></label></div>
   <label>{localizeText('Gift item')}<input value={draft.itemDescription} maxLength={200} onChange={e=>set('itemDescription',e.target.value)} placeholder={localizeText('Only if you gave something other than money')}/></label>
   <label>{localizeText('Note')}<input value={draft.note} maxLength={300} onChange={e=>set('note',e.target.value)}/></label>
   {formError&&<p className="focusError" role="alert">{localizeText(formError)}</p>}
   <div className="gbEntryActions"><button className="focusPrimary" disabled={busy}>{busy?localizeText('Saving…'):localizeText('Save')}</button><button type="button" className="focusTextLink gbPlain" onClick={()=>setOpen(false)}>{localizeText('Cancel')}</button>
    {editing&&<button type="button" className="gbDanger" onClick={()=>void remove(editing)}>{localizeText('Remove')}</button>}</div>
   <datalist id="gb-people">{data?.people.map(p=><option key={p.key} value={p.name}/>)}</datalist>
   <datalist id="gb-occasions">{OCCASIONS.map(o=><option key={o} value={localizeText(o)}/>)}</datalist>
  </form>}

  {data&&<section className="gbList">
   <div className="gbListTools"><input type="search" value={query} onChange={e=>setQuery(e.target.value)} onKeyDown={e=>{if(e.key==='Enter')void lookup(query)}} placeholder={localizeText('Look up a person')} aria-label={localizeText('Look up a person')}/>
    <div className="gbTabs" role="tablist"><button type="button" role="tab" aria-selected={tab==='PEOPLE'} onClick={()=>setTab('PEOPLE')}>{localizeText('People')}</button><button type="button" role="tab" aria-selected={tab==='GIVEN'} onClick={()=>setTab('GIVEN')}>{localizeText('Gifts I gave')}</button></div></div>
   {tab==='PEOPLE'?(people.length===0?<div className="gbEmpty"><p>{query?localizeText('No one by that name yet.'):localizeText('Your ledger fills up from your gift registers and the gifts you add here.')}</p>{query&&<button type="button" className="focusSecondary" onClick={()=>startAdd(query)}>{localizeText('Add a gift to {{name}}').replace('{{name}}',query)}</button>}</div>:
    <ul className="gbRows">{people.map(p=><li key={p.key}><button type="button" onClick={()=>void lookup(p.name)}>
     <span className="gbWho"><strong>{p.name}</strong><small>{[p.lastReceived&&localizeText('Gave you {{date}}').replace('{{date}}',day(p.lastReceived)),p.lastGiven&&localizeText('You gave {{date}}').replace('{{date}}',day(p.lastGiven))].filter(Boolean).join(' · ')}</small></span>
     <span className="gbBars" aria-label={localizeText('You gave {{a}}, received {{b}}').replace('{{a}}',inr(p.givenPaise)).replace('{{b}}',inr(p.receivedPaise))}>
      <span><i className="gbBarGiven" style={{width:`${Math.max(2,p.givenPaise/max*100)}%`}}/><small>{inr(p.givenPaise)}</small></span>
      <span><i className="gbBarReceived" style={{width:`${Math.max(2,p.receivedPaise/max*100)}%`}}/><small>{inr(p.receivedPaise)}</small></span></span></button></li>)}</ul>):
    (given.length===0?<p className="gbEmpty">{localizeText('No gifts added yet.')}</p>:<ul className="gbRows">{given.map(g=><li key={g.id}><button type="button" onClick={()=>startEdit(g)}>
     <span className="gbWho"><strong>{g.recipientName}</strong><small>{[g.occasion,day(g.occasionDate),localizeText(KIND_LABEL[g.kind]??g.kind)].filter(Boolean).join(' · ')}</small></span>
     <span className="gbWhat">{g.amountPaise!=null&&<strong>{inr(g.amountPaise)}</strong>}{g.itemDescription&&<small>{g.itemDescription}</small>}</span></button></li>)}</ul>)}
   {tab==='PEOPLE'&&people.length>0&&<p className="gbLegend"><i className="gbBarGiven"/>{localizeText('You gave')} <i className="gbBarReceived"/>{localizeText('You received')}</p>}
  </section>}
  {toast&&<div className="gbToast" role="status">{toast.text}{toast.undo&&<button type="button" onClick={()=>{toast.undo?.();setToast(null)}}>{localizeText('Undo')}</button>}</div>}
 </main>;
}
