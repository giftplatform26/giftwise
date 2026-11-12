'use client';
import {useEffect,useState} from 'react';
import Link from 'next/link';
import {localizeText} from '../lib/localization.mjs';
import {useLocalizationRender} from './LocalizationProvider';
import {inr} from '../lib/focused.mjs';
import {percent} from '../lib/eventops.mjs';
import {rememberOffer} from '../lib/acquisition.mjs';
import StoryCardMaker from './StoryCardMaker';
import './acquisition.css';

type Collection={recipientName:string;occasionLabel:string;organiserName:string|null;closesOn:string|null;closed:boolean;targetPaise:number|null;raisedPaise:number|null;contributors:number;showProgress:boolean};
type Credit={businessName:string;slug:string;kind:string;hasLogo:boolean};
const json=async<T,>(url:string):Promise<T|null>=>{try{const r=await fetch(url,{cache:'no-store'});return r.status===200?await r.json() as T:null}catch{return null}};

/** Header for office / group gift collections: who it's for, progress, deadline. Renders nothing for normal celebrations. */
export function CollectionHero({token}:{token:string}){useLocalizationRender();
 const [c,setC]=useState<Collection|null>(null);
 useEffect(()=>{void json<Collection>(`/backend/api/public/events/${encodeURIComponent(token)}/collection`).then(setC)},[token]);
 if(!c)return null;
 const pct=c.targetPaise&&c.raisedPaise!=null?percent(c.raisedPaise,c.targetPaise):null;
 const closes=c.closesOn?new Intl.DateTimeFormat(undefined,{day:'numeric',month:'long'}).format(new Date(`${c.closesOn}T12:00:00`)):null;
 return <section className="cxHero" aria-label={localizeText('Group gift')}><p className="focusEyebrow">{localizeText('{{occasion}} group gift').replace('{{occasion}}',c.occasionLabel)}</p>
  <h2>{localizeText('For {{name}}').replace('{{name}}',c.recipientName)}</h2>
  {c.organiserName&&<p className="cxBy">{localizeText('Organised by {{name}}').replace('{{name}}',c.organiserName)}</p>}
  {pct!=null&&<><div className="eoBar" role="progressbar" aria-valuenow={pct} aria-valuemin={0} aria-valuemax={100} aria-label={localizeText('Progress')}><span className="eoBar--paid" style={{width:`${pct}%`}}/></div>
   <p className="cxStats"><strong>{inr(c.raisedPaise??0)}</strong> {localizeText('of {{target}} confirmed').replace('{{target}}',inr(c.targetPaise??0))}</p></>}
  <p className="cxMeta">{localizeText('{{n}} people have chipped in').replace('{{n}}',String(c.contributors))}{closes?` · ${c.closed?localizeText('Closed on {{d}}').replace('{{d}}',closes):localizeText('Closes {{d}}').replace('{{d}}',closes)}`:''}</p>
  {!c.closed&&<a className="focusPrimary" href="#focused-gifts">{localizeText('Chip in')}</a>}<p className="gbFine">{localizeText('Leave a message below too — it goes into a card for {{name}}.').replace('{{name}}',c.recipientName)}</p></section>;
}

/** Bottom of the invitation: share card, planner credit, and the first-celebration-free offer. */
export function InvitationExtras({token,title,hostName,occasion,date,features}:{token:string;title:string;hostName?:string|null;occasion:string;date?:string|null;features?:Record<string,boolean>}){useLocalizationRender();
 const [credit,setCredit]=useState<Credit|null>(null),[offer,setOffer]=useState<{available:boolean;days:number}|null>(null),[sharing,setSharing]=useState(false);
 useEffect(()=>{if(features?.PARTNER_ACCOUNTS)void json<Credit>(`/backend/api/public/events/${encodeURIComponent(token)}/partner`).then(setCredit);
  if(features?.GUEST_FIRST_FREE)void json<{available:boolean;days:number}>(`/backend/api/public/events/${encodeURIComponent(token)}/first-free`).then(setOffer)},[token,features?.PARTNER_ACCOUNTS,features?.GUEST_FIRST_FREE]);
 const link=typeof window!=='undefined'?`${window.location.origin}/e/${token}`:'';
 if(!features?.STORY_CARDS&&!features?.SEATING_PLAN&&!credit&&!offer?.available)return null;
 return <section className="cxExtras">
  {features?.SEATING_PLAN&&<FindMyTable token={token}/>}
  {features?.STORY_CARDS&&<div className="cxShare">{sharing?<StoryCardMaker title={title} hostName={hostName} occasion={occasion} date={date} link={link} kinds={['INVITE']} eventToken={token}/>:
   <button type="button" className="focusSecondary" onClick={()=>setSharing(true)}>{localizeText('Share this invitation to your Status')}</button>}</div>}
  {credit&&<p className="cxCredit">{credit.hasLogo&&<img src={`/backend/api/public/partners/${encodeURIComponent(credit.slug)}/logo.png`} alt="" width={36} height={36}/>}
   <span>{localizeText('Planned by')} <Link href={`/p/${encodeURIComponent(credit.slug)}`}>{credit.businessName}</Link></span></p>}
  {offer?.available&&<aside className="cxOffer"><div><strong>{localizeText('Planning your own celebration?')}</strong><p>{localizeText('Guests of this celebration get premium free for {{n}} days on their first one.').replace('{{n}}',String(offer.days))}</p></div>
   <Link className="focusPrimary" href="/register" onClick={()=>{try{rememberOffer(token,localStorage)}catch{}}}>{localizeText('Start free')}</Link></aside>}
 </section>;
}

/** On a guest's receipt: the first-celebration-free offer, if the host allows it. */
export function ReceiptOffer({invitationPath}:{invitationPath:string|null}){useLocalizationRender();
 const token=invitationPath?.match(/^\/e\/([0-9a-f-]{36})$/i)?.[1]??null;
 const [offer,setOffer]=useState<{available:boolean;days:number}|null>(null);
 useEffect(()=>{if(token)void json<{available:boolean;days:number}>(`/backend/api/public/events/${encodeURIComponent(token)}/first-free`).then(setOffer)},[token]);
 if(!token||!offer?.available)return null;
 return <aside className="cxOffer"><div><strong>{localizeText('Planning your own celebration?')}</strong><p>{localizeText('Your first one comes with premium free for {{n}} days.').replace('{{n}}',String(offer.days))}</p></div>
  <Link className="focusPrimary" href="/register" onClick={()=>{try{rememberOffer(token,localStorage)}catch{}}}>{localizeText('Start free')}</Link></aside>;
}


/** Guests type their name to find their table, when the host allows it. Hidden when seating lookup is off. */
export function FindMyTable({token}:{token:string}){useLocalizationRender();
 const [name,setName]=useState(''),[results,setResults]=useState<{name:string;table:string}[]|null>(null),[error,setError]=useState(''),[available,setAvailable]=useState(true);
 async function search(e:React.FormEvent){e.preventDefault();setError('');
  try{const r=await fetch(`/backend/api/public/celebrations/${encodeURIComponent(token)}/find-table?name=${encodeURIComponent(name)}`,{cache:'no-store'});
   if(r.status===404){setAvailable(false);return}const body=await r.json().catch(()=>null);if(!r.ok)throw Error(body?.error||'Please try again');setResults(body)}
  catch(err){setError(err instanceof Error?err.message:'Please try again')}}
 if(!available)return null;
 return <section className="ftBox" aria-labelledby="ft-title"><h3 id="ft-title">{localizeText('Find your table')}</h3>
  <form onSubmit={search}><input value={name} maxLength={60} onChange={e=>setName(e.target.value)} placeholder={localizeText('Type your name')} aria-label={localizeText('Your name')}/><button className="focusPrimary">{localizeText('Find')}</button></form>
  {error&&<p className="focusError" role="alert">{localizeText(error)}</p>}
  {results&&(results.length===0?<p>{localizeText('We couldn’t find that name. Please ask at the entrance.')}</p>:<ul>{results.map(r=><li key={r.name}><strong>{r.name}</strong> — {r.table}</li>)}</ul>)}</section>;
}
