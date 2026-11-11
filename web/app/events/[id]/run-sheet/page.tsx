'use client';
import {useCallback,useEffect,useState} from 'react';
import {useParams} from 'next/navigation';
import {localizeText} from '../../../../lib/localization.mjs';
import {useLocalizationRender} from '../../../../components/LocalizationProvider';
import {hostRequest} from '../../../../lib/host-api.mjs';
import {clock} from '../../../../lib/eventops.mjs';
import '../../../../components/gift-books.css';
import '../../../../components/eventops.css';

type Item={id:string;runDate:string|null;startsAt:string;title:string;ownerName:string|null;location:string|null;note:string|null;functionId:string|null;functionName:string|null;done:boolean;version:number};
type Sheet={items:Item[];shared:boolean;shareToken:string|null;functions:{id:string;name:string;date:string|null}[];eventDate:string|null};
type Draft={runDate:string;startsAt:string;title:string;ownerName:string;location:string;note:string;functionId:string};
const empty:Draft={runDate:'',startsAt:'',title:'',ownerName:'',location:'',note:'',functionId:''};
const dayTitle=(d:string|null)=>d?new Intl.DateTimeFormat(undefined,{weekday:'long',day:'numeric',month:'long'}).format(new Date(`${d}T12:00:00`)):localizeText('On the day');

export default function RunSheet(){useLocalizationRender();
 const {id}=useParams();const eventId=String(id??'');const route=`/v1/events/${eventId}/run-sheet`;
 const [s,setS]=useState<Sheet|null>(null),[error,setError]=useState(''),[notice,setNotice]=useState(''),[busy,setBusy]=useState(false),[draft,setDraft]=useState<Draft>(empty),[editing,setEditing]=useState<Item|null>(null);
 const load=useCallback(async()=>{try{setS(await hostRequest(fetch,route) as Sheet);setError('')}catch(e){setError(e instanceof Error?e.message:'Could not load the run sheet')}},[route]);
 useEffect(()=>{if(/^[0-9a-f-]{36}$/i.test(eventId))void load()},[eventId,load]);
 const set=(k:keyof Draft,v:string)=>setDraft(d=>({...d,[k]:v}));
 async function save(e:React.FormEvent){e.preventDefault();if(!draft.startsAt||!draft.title.trim()){setError('Add a time and what happens');return}setBusy(true);setError('');
  const body={runDate:draft.runDate||null,startsAt:draft.startsAt,title:draft.title,ownerName:draft.ownerName||null,location:draft.location||null,note:draft.note||null,functionId:draft.functionId||null,...(editing?{version:editing.version}:{})};
  try{await hostRequest(fetch,editing?`${route}/items/${editing.id}`:`${route}/items`,{method:editing?'PUT':'POST',payload:body});setDraft({...empty,runDate:draft.runDate,functionId:draft.functionId});setEditing(null);await load()}
  catch(err){setError(err instanceof Error?err.message:'Could not save')}finally{setBusy(false)}}
 async function toggle(i:Item){try{await hostRequest(fetch,`${route}/items/${i.id}/done`,{method:'POST',payload:{done:!i.done}});await load()}catch(err){setError(err instanceof Error?err.message:'Could not update')}}
 async function remove(i:Item){if(!confirm(localizeText('Delete “{{t}}”?').replace('{{t}}',i.title)))return;try{await hostRequest(fetch,`${route}/items/${i.id}/delete`,{method:'POST'});setEditing(null);setDraft(empty);await load()}catch(err){setError(err instanceof Error?err.message:'Could not delete')}}
 async function share(on:boolean){try{await hostRequest(fetch,`${route}/share`,{method:'POST',payload:{shared:on}});setNotice(on?'Vendors can now open the read-only link.':'Sharing stopped.');await load()}catch(err){setError(err instanceof Error?err.message:'Could not change sharing')}}
 async function rotate(){if(!confirm(localizeText('Create a new link? The old link stops working.')))return;try{await hostRequest(fetch,`${route}/share/rotate`,{method:'POST'});setNotice('New link created.');await load()}catch(err){setError(err instanceof Error?err.message:'Could not create a link')}}
 const link=s?.shareToken&&typeof window!=='undefined'?`${window.location.origin}/run/${s.shareToken}`:'';
 const days=[...new Set((s?.items??[]).map(i=>i.runDate??''))];

 return <main className="focusPage gbPage">
  <header className="focusPageHeading"><div><p className="focusEyebrow">{localizeText('The day, minute by minute')}</p><h1>{localizeText('Run sheet')}</h1>
   <p>{localizeText('Who does what, when and where. Share a read-only copy with your caterer, decorator and photographer.')}</p></div>
   <button type="button" className="focusSecondary" onClick={()=>window.print()}>{localizeText('Print')}</button></header>
  {error&&<p className="focusError" role="alert">{localizeText(error)}</p>}
  {notice&&<p className="gbNotice" role="status">{localizeText(notice)}</p>}
  {s&&<section className="focusPanel eoShare no-print"><label className="gbCheck"><input type="checkbox" checked={s.shared} onChange={e=>void share(e.target.checked)}/><span><strong>{localizeText('Share with vendors')}</strong> <small>{localizeText('Anyone with the link can view, not edit.')}</small></span></label>
   {s.shared&&link&&<div className="lwCopy"><input readOnly value={link} onFocus={e=>e.currentTarget.select()} aria-label={localizeText('Vendor link')}/><button type="button" className="focusSecondary" onClick={()=>void navigator.clipboard?.writeText(link).then(()=>setNotice('Link copied.'))}>{localizeText('Copy')}</button>
    <a className="focusSecondary gbButtonLink" href={`https://wa.me/?text=${encodeURIComponent(link)}`} target="_blank" rel="noopener noreferrer">{localizeText('WhatsApp')}</a><button type="button" className="gbPlain" onClick={()=>void rotate()}>{localizeText('New link')}</button></div>}</section>}
  {s&&<form className={`gbEntry no-print${editing?' gbEntry--editing':''}`} onSubmit={save}><h2>{editing?localizeText('Edit item'):localizeText('Add to the run sheet')}</h2>
   <div className="gbEntryRow"><label>{localizeText('Time')}<input type="time" value={draft.startsAt} onChange={e=>set('startsAt',e.target.value)}/></label>
    <label className="gbGrow">{localizeText('What happens')}<input value={draft.title} maxLength={140} onChange={e=>set('title',e.target.value)} placeholder={localizeText('e.g. Baraat arrives at the gate')}/></label></div>
   <div className="gbEntryRow"><label className="gbGrow">{localizeText('Who is responsible')}<input value={draft.ownerName} maxLength={80} onChange={e=>set('ownerName',e.target.value)} placeholder={localizeText('e.g. Ravi (cousin)')}/></label>
    <label className="gbGrow">{localizeText('Where')}<input value={draft.location} maxLength={120} onChange={e=>set('location',e.target.value)}/></label></div>
   <div className="gbEntryRow"><label>{localizeText('Date')}<input type="date" value={draft.runDate} onChange={e=>set('runDate',e.target.value)}/></label>
    {s.functions.length>0&&<label className="gbGrow">{localizeText('Function')}<select value={draft.functionId} onChange={e=>{set('functionId',e.target.value);const f=s.functions.find(x=>x.id===e.target.value);if(f?.date)set('runDate',f.date)}}><option value="">—</option>{s.functions.map(f=><option key={f.id} value={f.id}>{f.name}</option>)}</select></label>}</div>
   <label>{localizeText('Note')}<input value={draft.note} maxLength={400} onChange={e=>set('note',e.target.value)}/></label>
   <div className="gbEntryActions"><button className="focusPrimary" disabled={busy}>{editing?localizeText('Save'):localizeText('Add')}</button>
    {editing&&<><button type="button" className="gbPlain" onClick={()=>{setEditing(null);setDraft(empty)}}>{localizeText('Cancel')}</button><button type="button" className="gbDanger" onClick={()=>void remove(editing)}>{localizeText('Delete')}</button></>}</div></form>}
  {s&&s.items.length===0&&<p className="gbEmpty">{localizeText('Start with the first thing that happens — often the decor check or the vendor arrival.')}</p>}
  {s&&days.map(d=><section key={d||'none'} className="eoRunDay"><h2 className="gbSubhead">{dayTitle(d||null)}</h2><ol className="eoRun">{s.items.filter(i=>(i.runDate??'')===d).map(i=><li key={i.id} className={i.done?'is-done':''}>
   <span className="eoRunTime">{clock(i.startsAt)}</span>
   <button type="button" className="eoTaskBody" onClick={()=>{setEditing(i);setDraft({runDate:i.runDate??'',startsAt:i.startsAt.slice(0,5),title:i.title,ownerName:i.ownerName??'',location:i.location??'',note:i.note??'',functionId:i.functionId??''});window.scrollTo({top:0,behavior:'smooth'})}}>
    <strong>{i.title}</strong><small>{[i.ownerName,i.location,i.functionName,i.note].filter(Boolean).join(' · ')}</small></button>
   <label className="eoTick no-print"><input type="checkbox" checked={i.done} onChange={()=>void toggle(i)} aria-label={localizeText('Done')}/></label></li>)}</ol></section>)}
 </main>;
}
