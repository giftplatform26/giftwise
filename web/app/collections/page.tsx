'use client';
import {useCallback,useEffect,useState} from 'react';
import Link from 'next/link';
import {localizeText} from '../../lib/localization.mjs';
import {useLocalizationRender} from '../../components/LocalizationProvider';
import {hostRequest} from '../../lib/host-api.mjs';
import {inr} from '../../lib/focused.mjs';
import {parseRupees} from '../../lib/register.mjs';
import {percent} from '../../lib/eventops.mjs';
import '../../components/gift-books.css';
import '../../components/eventops.css';
import '../../components/acquisition.css';

type Account={id:string;kind:'UPI'|'BANK';label:string};
type Mine={eventId:string;publicToken:string;title:string;recipientName:string;occasionLabel:string;targetPaise:number|null;confirmedPaise:number;pendingPaise:number;contributors:number;closesOn:string|null;status:string};
const OCC:[string,string][]=[['FAREWELL','Farewell'],['BIRTHDAY','Birthday'],['RETIREMENT','Retirement'],['WEDDING','Wedding'],['BABY_SHOWER','New baby'],['OTHER','Something else']];
const STEPS=['Who is it for?','How much and until when?','Where should the money go?'];

export default function Collections(){useLocalizationRender();
 const [list,setList]=useState<Mine[]|null>(null),[accounts,setAccounts]=useState<Account[]>([]),[error,setError]=useState(''),[open,setOpen]=useState(false),[step,setStep]=useState(0),[busy,setBusy]=useState(false),[created,setCreated]=useState<string|null>(null);
 const [f,setF]=useState({recipientName:'',occasion:'FAREWELL',occasionLabel:'',message:'',target:'',closesOn:'',showProgress:true,account:''});
 const load=useCallback(async()=>{try{const [l,a]=await Promise.all([hostRequest(fetch,'/v1/me/collections'),hostRequest(fetch,'/v1/me/collections/accounts')]);setList(l as Mine[]);setAccounts(a as Account[]);setError('')}catch(e){setError(e instanceof Error?e.message:'Could not load')}},[]);
 useEffect(()=>{void load()},[load]);
 const set=(k:string,v:string|boolean)=>setF(x=>({...x,[k]:v}));
 async function create(){setError('');let target:number|null;try{target=parseRupees(f.target)}catch(e){setError(e instanceof Error?e.message:'Check the amount');return}
  const acc=accounts.find(a=>a.id===f.account);if(!acc){setError('Choose where the money should go');return}
  setBusy(true);try{const r=await hostRequest(fetch,'/v1/me/collections',{method:'POST',payload:{recipientName:f.recipientName,occasion:f.occasion,occasionLabel:f.occasionLabel||null,message:f.message||null,targetPaise:target,closesOn:f.closesOn||null,showProgress:f.showProgress,accountKind:acc.kind,accountId:acc.id}}) as {publicToken:string};
   setCreated(`${window.location.origin}/e/${r.publicToken}`);setOpen(false);setStep(0);await load()}catch(e){setError(e instanceof Error?e.message:'Could not create the collection')}finally{setBusy(false)}}
 const canNext=step===0?Boolean(f.recipientName.trim()):step===1?Boolean(f.target.trim()):Boolean(f.account);
 const share=(url:string,name:string)=>`https://wa.me/?text=${encodeURIComponent(localizeText('Let’s all chip in for {{name}}’s gift: {{url}}').replace('{{name}}',name).replace('{{url}}',url))}`;

 return <main className="focusPage gbPage">
  <header className="focusPageHeading"><div><p className="focusEyebrow">{localizeText('No event needed')}</p><h1>{localizeText('Group gifts')}</h1>
   <p>{localizeText('Collect money for a colleague’s farewell, a friend’s birthday or a teacher’s gift. Everyone pays you directly by UPI or bank, gets a receipt, and can add a message to the card.')}</p></div>
   {!open&&<button type="button" className="focusPrimary" onClick={()=>{setOpen(true);setCreated(null)}}>{localizeText('Start a group gift')}</button>}</header>
  {error&&<p className="focusError" role="alert">{localizeText(error)}</p>}
  {created&&<section className="gbNotice cxCreated" role="status"><strong>{localizeText('Your group gift is live.')}</strong> {localizeText('Share the link with everyone:')}
   <div className="lwCopy"><input readOnly value={created} onFocus={e=>e.currentTarget.select()}/><a className="focusPrimary" href={share(created,f.recipientName)} target="_blank" rel="noopener noreferrer">{localizeText('Share on WhatsApp')}</a></div></section>}
  {open&&<section className="gbEntry"><div className="tySteps" aria-hidden="true">{STEPS.map((s,i)=><span key={s} className={i<=step?'on':''}/>)}</div><h2>{localizeText(STEPS[step])}</h2>
   {step===0&&<><label>{localizeText('Name')}<input autoFocus value={f.recipientName} maxLength={120} onChange={e=>set('recipientName',e.target.value)} placeholder={localizeText('e.g. Ravi from Finance')}/></label>
    <div className="gbChips" role="radiogroup" aria-label={localizeText('Occasion')}>{OCC.map(([k,l])=><button type="button" key={k} aria-pressed={f.occasion===k} onClick={()=>set('occasion',k)}>{localizeText(l)}</button>)}</div>
    {f.occasion==='OTHER'&&<label>{localizeText('Occasion name')}<input value={f.occasionLabel} maxLength={60} onChange={e=>set('occasionLabel',e.target.value)} placeholder={localizeText('e.g. Teacher’s day')}/></label>}
    <label>{localizeText('Message to contributors (optional)')}<textarea rows={3} maxLength={2000} value={f.message} onChange={e=>set('message',e.target.value)} placeholder={localizeText('Ravi is moving to Pune after 8 wonderful years…')}/></label></>}
   {step===1&&<><div className="gbEntryRow"><label className="gbAmount">{localizeText('Target (₹)')}<input autoFocus inputMode="decimal" value={f.target} onChange={e=>set('target',e.target.value)} placeholder="15000"/></label>
    <label>{localizeText('Collection closes on')}<input type="date" value={f.closesOn} onChange={e=>set('closesOn',e.target.value)}/></label></div>
    <label className="gbCheck"><input type="checkbox" checked={f.showProgress} onChange={e=>set('showProgress',e.target.checked)}/>{localizeText('Show how much has been collected (individual amounts stay private)')}</label></>}
   {step===2&&(accounts.length===0?<p className="gbQueued">{localizeText('Add and confirm a UPI ID or bank account first, then come back.')} <Link href="/host/payment-settings">{localizeText('Receiving accounts')}</Link></p>:
    <div className="chRoles">{accounts.map(a=><label key={a.id} className="chRole"><input type="radio" name="acc" checked={f.account===a.id} onChange={()=>set('account',a.id)}/><span><strong>{a.kind==='UPI'?'UPI':localizeText('Bank account')}</strong><small>{a.label}</small></span></label>)}</div>)}
   <div className="gbEntryActions">{step>0&&<button type="button" className="gbPlain" onClick={()=>setStep(s=>s-1)}>{localizeText('Back')}</button>}
    {step<2?<button type="button" className="focusPrimary" disabled={!canNext} onClick={()=>setStep(s=>s+1)}>{localizeText('Next')}</button>:<button type="button" className="focusPrimary" disabled={busy||!canNext} onClick={()=>void create()}>{busy?localizeText('Creating…'):localizeText('Create & get the link')}</button>}
    <button type="button" className="gbPlain" onClick={()=>setOpen(false)}>{localizeText('Cancel')}</button></div></section>}
  {list&&list.length===0&&!open&&<p className="gbEmpty">{localizeText('No group gifts yet. Start one in under a minute.')}</p>}
  {list&&list.length>0&&<div className="hpGrid">{list.map(c=>{const pct=c.targetPaise?percent(c.confirmedPaise,c.targetPaise):0;const url=typeof window!=='undefined'?`${window.location.origin}/e/${c.publicToken}`:'';
   return <article key={c.eventId} className="hpCard"><p className="focusEyebrow">{localizeText(c.occasionLabel)}{c.closesOn?` · ${localizeText('closes {{d}}').replace('{{d}}',new Intl.DateTimeFormat(undefined,{day:'numeric',month:'short'}).format(new Date(`${c.closesOn}T12:00:00`)))}`:''}</p>
    <h2>{c.recipientName}</h2><div className="eoBar" aria-hidden="true"><span className="eoBar--paid" style={{width:`${pct}%`}}/></div>
    <p className="gbFine"><strong>{inr(c.confirmedPaise)}</strong> {localizeText('confirmed')}{c.pendingPaise>0?` · ${inr(c.pendingPaise)} ${localizeText('waiting for you to confirm')}`:''}{c.targetPaise?` · ${localizeText('target {{t}}').replace('{{t}}',inr(c.targetPaise))}`:''} · {c.contributors} {localizeText('people')}</p>
    <nav className="hpTools"><Link href={`/events/${c.eventId}/transactions`}>{localizeText('Confirm payments')} →</Link><Link href={`/events/${c.eventId}/contributions`}>{localizeText('Who has paid')} →</Link>
     <a href={share(url,c.recipientName)} target="_blank" rel="noopener noreferrer">{localizeText('Share again on WhatsApp')} →</a><a href={url} target="_blank" rel="noopener noreferrer">{localizeText('Open the page')} →</a></nav></article>})}</div>}
 </main>;
}
