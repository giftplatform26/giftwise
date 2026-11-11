'use client';
import {useCallback,useEffect,useMemo,useState} from 'react';
import {useParams} from 'next/navigation';
import {localizeText} from '../../../../lib/localization.mjs';
import {useLocalizationRender} from '../../../../components/LocalizationProvider';
import {hostRequest} from '../../../../lib/host-api.mjs';
import {audienceCount,STATUS_LABEL} from '../../../../lib/whatsapp.mjs';
import '../../../../components/gift-books.css';
import '../../../../components/whatsapp.css';

type Contact={id:string;name:string;phone:string;optedOut:boolean;replyAttendance:string|null;replyPartySize:number|null;repliedAt:string|null;lastKind:string|null;lastStatus:string|null;lastAt:string|null};
type Stats={contacts:number;optedOut:number;yes:number;maybe:number;no:number;noReply:number;guestsComing:number;sent:number;delivered:number;read:number;failed:number;queued:number};
type Overview={provider:string;live:boolean;published:boolean;contacts:Contact[];stats:Stats;previews:Record<string,string>};
const KINDS:[string,string][]=[['INVITATION','Invitation'],['REMINDER','Reminder'],['THANK_YOU','Thank you']];
const AUDIENCES:[string,string][]=[['ALL','Everyone'],['NO_REPLY','Not replied yet'],['ATTENDING','Coming']];
const REPLY:Record<string,string>={YES:'Coming',MAYBE:'Maybe',NO:'Not coming'};

export default function WhatsApp(){useLocalizationRender();
 const {id}=useParams();const eventId=String(id??'');const route=`/v1/events/${eventId}/whatsapp`;
 const [data,setData]=useState<Overview|null>(null),[error,setError]=useState(''),[notice,setNotice]=useState(''),[busy,setBusy]=useState(false);
 const [paste,setPaste]=useState(''),[skipped,setSkipped]=useState<string[]>([]),[kind,setKind]=useState('INVITATION'),[audience,setAudience]=useState('ALL'),[query,setQuery]=useState(''),[showImport,setShowImport]=useState(false);
 const load=useCallback(async()=>{try{const d=await hostRequest(fetch,route) as Overview;setData(d);setError('');if(d.contacts.length===0)setShowImport(true)}catch(e){setError(e instanceof Error?e.message:'Could not load')}},[route]);
 useEffect(()=>{if(/^[0-9a-f-]{36}$/i.test(eventId))void load()},[eventId,load]);
 // While messages are going out, refresh so statuses and replies appear without reloading.
 useEffect(()=>{if(!data||data.stats.queued===0)return;const t=setTimeout(()=>void load(),5000);return()=>clearTimeout(t)},[data,load]);
 async function importList(e:React.FormEvent){e.preventDefault();setBusy(true);setError('');setSkipped([]);
  try{const r=await hostRequest(fetch,`${route}/contacts/import`,{method:'POST',payload:{text:paste}}) as {added:number;updated:number;skipped:string[]};
   setNotice(localizeText('Added {{a}} guests, updated {{u}}.').replace('{{a}}',String(r.added)).replace('{{u}}',String(r.updated)));setSkipped(r.skipped);if(r.skipped.length===0){setPaste('');setShowImport(false)}await load()}
  catch(err){setError(err instanceof Error?err.message:'Could not import')}finally{setBusy(false)}}
 async function send(){if(!data)return;const n=audienceCount(data.contacts,audience);
  if(!confirm(localizeText(data.live?'Send this {{kind}} to {{n}} guests on WhatsApp?':'Practice mode: record this {{kind}} for {{n}} guests? Nothing will be sent.').replace('{{kind}}',localizeText(KINDS.find(k=>k[0]===kind)![1]).toLowerCase()).replace('{{n}}',String(n))))return;
  setBusy(true);setError('');try{const r=await hostRequest(fetch,`${route}/send`,{method:'POST',payload:{kind,audience}}) as {queued:number;skippedOptedOut:number;skippedAlreadyToday:number};
   setNotice([localizeText('{{n}} messages on their way.').replace('{{n}}',String(r.queued)),r.skippedAlreadyToday?localizeText('{{n}} already received this today.').replace('{{n}}',String(r.skippedAlreadyToday)):'',r.skippedOptedOut?localizeText('{{n}} asked not to be messaged.').replace('{{n}}',String(r.skippedOptedOut)):''].filter(Boolean).join(' '));await load()}
  catch(err){setError(err instanceof Error?err.message:'Could not send')}finally{setBusy(false)}}
 async function remove(c:Contact){if(!confirm(localizeText('Remove {{name}} from the guest list?').replace('{{name}}',c.name)))return;try{await hostRequest(fetch,`${route}/contacts/${c.id}/delete`,{method:'POST'});await load()}catch(err){setError(err instanceof Error?err.message:'Could not remove')}}
 async function simulate(c:Contact,text:string){try{await hostRequest(fetch,`${route}/dev-reply`,{method:'POST',payload:{contactId:c.id,text}});await load()}catch(err){setError(err instanceof Error?err.message:'Could not simulate')}}
 const contacts=useMemo(()=>{const q=query.trim().toLowerCase();return (data?.contacts??[]).filter(c=>!q||c.name.toLowerCase().includes(q)||c.phone.includes(q))},[data,query]);
 const s=data?.stats;

 return <main className="focusPage gbPage">
  <header className="focusPageHeading"><div><p className="focusEyebrow">{localizeText('Where your guests already are')}</p><h1>{localizeText('WhatsApp messages')}</h1>
   <p>{localizeText('Send your invitation, a reminder and a thank-you on WhatsApp. Guests reply YES, NO or MAYBE and you see it here.')}</p></div></header>
  {data&&!data.live&&<p className="waPractice" role="note"><strong>{localizeText('Practice mode.')}</strong> {localizeText('Messages are recorded but not sent, so you can try everything safely. Live sending starts once GiftWise connects WhatsApp Business.')}</p>}
  {error&&<p className="focusError" role="alert">{localizeText(error)}</p>}
  {notice&&<p className="gbNotice" role="status">{notice}</p>}
  {s&&<section className="gbStats" aria-label={localizeText('Replies')}>
   <div><span>{localizeText('Coming')}</span><strong>{s.yes}</strong><small className="waSmall">{localizeText('{{n}} guests in total').replace('{{n}}',String(s.guestsComing))}</small></div>
   <div><span>{localizeText('Maybe')}</span><strong>{s.maybe}</strong></div>
   <div><span>{localizeText('Not coming')}</span><strong>{s.no}</strong></div>
   <div><span>{localizeText('No reply yet')}</span><strong>{s.noReply}</strong></div></section>}

  {data&&<div className="waGrid">
   <section className="gbEntry waCompose" aria-labelledby="wa-compose"><h2 id="wa-compose">{localizeText('Send a message')}</h2>
    <div className="gbTabs" role="tablist">{KINDS.map(([k,l])=><button key={k} type="button" role="tab" aria-selected={kind===k} onClick={()=>setKind(k)}>{localizeText(l)}</button>)}</div>
    <div className="waPhone" aria-label={localizeText('Preview')}><div className="waBubble">{data.previews[kind]}<span className="waTime">{localizeText('now')}</span></div></div>
    <p className="gbFine">{localizeText('Each guest sees their own name. Messages use templates approved by WhatsApp, so the wording is fixed.')}</p>
    <fieldset className="waAudience"><legend>{localizeText('Send to')}</legend>{AUDIENCES.map(([a,l])=><label key={a} className="gbCheck"><input type="radio" name="aud" checked={audience===a} onChange={()=>setAudience(a)}/>{localizeText(l)} <small>({audienceCount(data.contacts,a)})</small></label>)}</fieldset>
    {!data.published&&kind!=='THANK_YOU'&&<p className="gbQueued">{localizeText('Publish your invitation first.')}</p>}
    <button type="button" className="focusPrimary" disabled={busy||audienceCount(data.contacts,audience)===0||(!data.published&&kind!=='THANK_YOU')} onClick={()=>void send()}>{busy?localizeText('Sending…'):localizeText('Send to {{n}} guests').replace('{{n}}',String(audienceCount(data.contacts,audience)))}</button>
    {s&&(s.sent+s.delivered+s.read+s.failed+s.queued)>0&&<p className="waDelivery">{localizeText('Delivery')}: {s.queued>0&&<span>{s.queued} {localizeText('sending')} · </span>}{s.sent} {localizeText('sent')} · {s.delivered} {localizeText('delivered')} · {s.read} {localizeText('read')}{s.failed>0&&<> · <span className="waFail">{s.failed} {localizeText('failed')}</span></>}</p>}
   </section>

   <section className="gbList" aria-labelledby="wa-guests">
    <div className="waListHead"><h2 id="wa-guests">{localizeText('Guest list')} <small>({data.contacts.length})</small></h2><button type="button" className="focusSecondary" aria-expanded={showImport} onClick={()=>setShowImport(v=>!v)}>{localizeText('Add guests')}</button></div>
    {showImport&&<form className="gbEntry" onSubmit={importList}><label>{localizeText('Paste names and mobile numbers, one guest per line')}<textarea rows={6} value={paste} onChange={e=>setPaste(e.target.value)} placeholder={'Asha Rao, 98765 43210\nRavi Kumar, +91 91234 56789'}/>
     <small>{localizeText('Tip: copy two columns (name and phone) from a spreadsheet and paste them here. Indian numbers without +91 are fine.')}</small></label>
     {skipped.length>0&&<ul className="waSkipped" role="alert">{skipped.map(x=><li key={x}>{x}</li>)}</ul>}
     <div className="gbEntryActions"><button className="focusPrimary" disabled={busy||!paste.trim()}>{localizeText('Add to guest list')}</button></div></form>}
    {data.contacts.length>8&&<input type="search" value={query} onChange={e=>setQuery(e.target.value)} placeholder={localizeText('Search guests')} aria-label={localizeText('Search guests')}/>}
    {data.contacts.length===0?<p className="gbEmpty">{localizeText('Add your guests to start.')}</p>:<ul className="gbRows">{contacts.map(c=><li key={c.id}><div>
     <span className="gbWho"><strong>{c.name}</strong><small>{c.phone}{c.lastStatus&&<> · {localizeText((STATUS_LABEL as Record<string,string>)[c.lastStatus]??c.lastStatus)}</>}</small></span>
     <span className="waRight">{c.optedOut?<span className="waChip waChip--off">{localizeText('Opted out')}</span>:c.replyAttendance?<span className={`waChip waChip--${c.replyAttendance.toLowerCase()}`}>{localizeText(REPLY[c.replyAttendance])}{c.replyAttendance==='YES'&&c.replyPartySize?` · ${c.replyPartySize}`:''}</span>:null}
      {!data.live&&c.lastKind&&!c.optedOut&&<select aria-label={localizeText('Practice a reply from {{name}}').replace('{{name}}',c.name)} value="" onChange={e=>{if(e.target.value)void simulate(c,e.target.value)}}><option value="">{localizeText('Practise reply…')}</option><option value="YES 2">YES 2</option><option value="MAYBE">MAYBE</option><option value="NO">NO</option><option value="STOP">STOP</option></select>}
      <button type="button" className="gbPlain" aria-label={localizeText('Remove {{name}}').replace('{{name}}',c.name)} onClick={()=>void remove(c)}>✕</button></span></div></li>)}</ul>}
   </section>
  </div>}
 </main>;
}
