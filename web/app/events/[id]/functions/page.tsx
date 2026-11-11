'use client';
import {useCallback,useEffect,useState} from 'react';
import {useParams} from 'next/navigation';
import {localizeText} from '../../../../lib/localization.mjs';
import {useLocalizationRender} from '../../../../components/LocalizationProvider';
import {hostRequest} from '../../../../lib/host-api.mjs';
import {FUNCTION_PRESETS,functionWhen,functionProblem} from '../../../../lib/functions.mjs';
import '../../../../components/gift-books.css';
import '../../../../components/functions.css';

type Fn={id:string;name:string;functionDate:string|null;startTime:string|null;endTime:string|null;venueName:string|null;venueAddress:string|null;mapUrl:string|null;dressCode:string|null;notes:string|null;rsvpEnabled:boolean;sortOrder:number;version:number};
type Row={function:Fn;counts:{yes:number;maybe:number;no:number;guests:number};responses:{guestName:string;guestEmail:string;attendance:string;partySize:number}[]};
type Draft={name:string;functionDate:string;startTime:string;endTime:string;venueName:string;venueAddress:string;mapUrl:string;dressCode:string;notes:string;rsvpEnabled:boolean};
const blank=():Draft=>({name:'',functionDate:'',startTime:'',endTime:'',venueName:'',venueAddress:'',mapUrl:'',dressCode:'',notes:'',rsvpEnabled:true});
const fromFn=(f:Fn):Draft=>({name:f.name,functionDate:f.functionDate??'',startTime:f.startTime?.slice(0,5)??'',endTime:f.endTime?.slice(0,5)??'',venueName:f.venueName??'',venueAddress:f.venueAddress??'',mapUrl:f.mapUrl??'',dressCode:f.dressCode??'',notes:f.notes??'',rsvpEnabled:f.rsvpEnabled});
const ATT:Record<string,string>={YES:'Coming',MAYBE:'Maybe',NO:'Not coming'};

export default function Functions(){useLocalizationRender();
 const {id}=useParams();const eventId=String(id??'');const route=`/v1/events/${eventId}/functions`;
 const [rows,setRows]=useState<Row[]|null>(null),[error,setError]=useState(''),[editing,setEditing]=useState<Fn|'new'|null>(null),[draft,setDraft]=useState<Draft>(blank()),[formError,setFormError]=useState(''),[busy,setBusy]=useState(false),[open,setOpen]=useState<string|null>(null),[notice,setNotice]=useState('');
 const load=useCallback(async()=>{try{setRows(await hostRequest(fetch,route) as Row[]);setError('')}catch(e){setError(e instanceof Error?e.message:'Could not load the schedule')}},[route]);
 useEffect(()=>{if(/^[0-9a-f-]{36}$/i.test(eventId))void load()},[eventId,load]);
 const set=<K extends keyof Draft>(k:K,v:Draft[K])=>setDraft(d=>({...d,[k]:v}));
 function start(f:Fn|'new',preset?:string){setEditing(f);setFormError('');setDraft(f==='new'?{...blank(),name:preset??''}:fromFn(f));setTimeout(()=>document.getElementById('fn-name')?.focus(),50)}
 async function save(e:React.FormEvent){e.preventDefault();const problem=functionProblem(draft);if(problem){setFormError(problem);return}
  const body={...draft,functionDate:draft.functionDate||null,startTime:draft.startTime||null,endTime:draft.endTime||null,...(editing&&editing!=='new'?{version:editing.version}:{})};
  setBusy(true);setFormError('');try{await hostRequest(fetch,editing==='new'?route:`${route}/${(editing as Fn).id}`,{method:editing==='new'?'POST':'PUT',payload:body});
   setNotice(editing==='new'?'Function added to your schedule.':'Function updated.');setEditing(null);await load()}catch(err){setFormError(err instanceof Error?err.message:'Could not save')}finally{setBusy(false)}}
 async function remove(f:Fn){if(!confirm(localizeText('Delete {{name}}? Guest answers for it will be removed too.').replace('{{name}}',f.name)))return;
  try{await hostRequest(fetch,`${route}/${f.id}/delete`,{method:'POST'});setEditing(null);setNotice('Function deleted.');await load()}catch(err){setError(err instanceof Error?err.message:'Could not delete')}}
 async function move(i:number,dir:-1|1){if(!rows)return;const order=rows.map(r=>r.function.id);const j=i+dir;if(j<0||j>=order.length)return;[order[i],order[j]]=[order[j],order[i]];
  try{await hostRequest(fetch,`${route}/order`,{method:'POST',payload:order});await load()}catch(err){setError(err instanceof Error?err.message:'Could not reorder');void load()}}
 const used=new Set(rows?.map(r=>r.function.name.toLowerCase()));

 const form=<form className="gbEntry fnForm" onSubmit={save} noValidate>
  <h2>{editing==='new'?localizeText('Add a function'):localizeText('Edit function')}</h2>
  <label>{localizeText('Name')}<input id="fn-name" value={draft.name} maxLength={80} onChange={e=>set('name',e.target.value)} placeholder={localizeText('e.g. Mehendi')}/></label>
  <div className="gbEntryRow"><label>{localizeText('Date')}<input type="date" value={draft.functionDate} onChange={e=>set('functionDate',e.target.value)}/></label>
   <label>{localizeText('Starts')}<input type="time" value={draft.startTime} onChange={e=>set('startTime',e.target.value)}/></label>
   <label>{localizeText('Ends')}<input type="time" value={draft.endTime} onChange={e=>set('endTime',e.target.value)}/></label></div>
  <div className="gbEntryRow"><label className="gbGrow">{localizeText('Venue')}<input value={draft.venueName} maxLength={120} onChange={e=>set('venueName',e.target.value)} placeholder={localizeText('e.g. Lotus Lawns')}/></label>
   <label className="gbGrow">{localizeText('Map link')}<input type="url" inputMode="url" value={draft.mapUrl} maxLength={500} onChange={e=>set('mapUrl',e.target.value)} placeholder="https://maps.google.com/…"/></label></div>
  <label>{localizeText('Address')}<input value={draft.venueAddress} maxLength={300} onChange={e=>set('venueAddress',e.target.value)}/></label>
  <div className="gbEntryRow"><label className="gbGrow">{localizeText('Dress code')}<input value={draft.dressCode} maxLength={120} onChange={e=>set('dressCode',e.target.value)} placeholder={localizeText('e.g. Shades of yellow')}/></label></div>
  <label>{localizeText('Note for guests')}<textarea rows={2} value={draft.notes} maxLength={600} onChange={e=>set('notes',e.target.value)}/></label>
  <label className="gbCheck"><input type="checkbox" checked={draft.rsvpEnabled} onChange={e=>set('rsvpEnabled',e.target.checked)}/>{localizeText('Ask guests whether they are coming to this function')}</label>
  {formError&&<p className="focusError" role="alert">{localizeText(formError)}</p>}
  <div className="gbEntryActions"><button className="focusPrimary" disabled={busy}>{busy?localizeText('Saving…'):localizeText('Save function')}</button><button type="button" className="gbPlain" onClick={()=>setEditing(null)}>{localizeText('Cancel')}</button>
   {editing&&editing!=='new'&&<button type="button" className="gbDanger" onClick={()=>void remove(editing)}>{localizeText('Delete')}</button>}</div>
 </form>;

 return <main className="focusPage gbPage">
  <header className="focusPageHeading"><div><p className="focusEyebrow">{localizeText('Your celebration, function by function')}</p><h1>{localizeText('Schedule & functions')}</h1>
   <p>{localizeText('Add each function with its own time, venue and dress code. Guests see the schedule on your invitation and answer for each one.')}</p></div>
   {editing===null&&<button type="button" className="focusPrimary" onClick={()=>start('new')}>{localizeText('Add a function')}</button>}</header>
  {error&&<p className="focusError" role="alert">{localizeText(error)} <button type="button" onClick={()=>void load()}>{localizeText('Retry')}</button></p>}
  {notice&&<p className="gbNotice" role="status">{localizeText(notice)}</p>}
  {editing==='new'&&form}
  {rows&&rows.length===0&&editing===null&&<section className="gbEmpty fnStart"><h2>{localizeText('Start with a function')}</h2><p>{localizeText('Pick one to begin. You can rename it and add as many as you need.')}</p>
   <div className="gbChips">{FUNCTION_PRESETS.map((p:string)=><button key={p} type="button" onClick={()=>start('new',localizeText(p))}>{localizeText(p)}</button>)}</div></section>}
  {rows&&rows.length>0&&<ol className="fnTimeline">{rows.map((r,i)=>{const f=r.function;const when=functionWhen(f);return <li key={f.id}>
   {editing!==null&&editing!=='new'&&editing.id===f.id?form:<article className="fnCard">
    <div className="fnMain"><p className="fnWhen">{when||localizeText('Date to be announced')}</p><h2>{f.name}</h2>
     {(f.venueName||f.venueAddress)&&<p className="fnVenue">{[f.venueName,f.venueAddress].filter(Boolean).join(' · ')}{f.mapUrl&&<> · <a href={f.mapUrl} target="_blank" rel="noopener noreferrer">{localizeText('Map')}</a></>}</p>}
     {f.dressCode&&<p className="fnDress">{localizeText('Dress code')}: {f.dressCode}</p>}</div>
    <div className="fnSide">{f.rsvpEnabled?<button type="button" className="fnCounts" aria-expanded={open===f.id} onClick={()=>setOpen(open===f.id?null:f.id)}><strong>{r.counts.guests}</strong><span>{localizeText('guests coming')}</span><small>{r.counts.yes} {localizeText('yes')} · {r.counts.maybe} {localizeText('maybe')} · {r.counts.no} {localizeText('no')}</small></button>:<p className="fnNoRsvp">{localizeText('No RSVP')}</p>}
     <div className="fnTools"><button type="button" aria-label={localizeText('Move up')} disabled={i===0} onClick={()=>void move(i,-1)}>↑</button><button type="button" aria-label={localizeText('Move down')} disabled={i===rows.length-1} onClick={()=>void move(i,1)}>↓</button><button type="button" onClick={()=>start(f)}>{localizeText('Edit')}</button></div></div>
    {open===f.id&&<div className="fnResponses">{r.responses.length===0?<p className="gbFine">{localizeText('No answers yet.')}</p>:<ul>{r.responses.map(x=><li key={x.guestEmail}><span>{x.guestName}</span><small>{localizeText(ATT[x.attendance])}{x.attendance!=='NO'?` · ${x.partySize}`:''}</small></li>)}</ul>}</div>}
   </article>}</li>})}</ol>}
  {rows&&rows.length>0&&editing===null&&<div className="gbChips fnMore">{FUNCTION_PRESETS.filter((p:string)=>!used.has(localizeText(p).toLowerCase())).slice(0,5).map((p:string)=><button key={p} type="button" onClick={()=>start('new',localizeText(p))}>+ {localizeText(p)}</button>)}</div>}
 </main>;
}
