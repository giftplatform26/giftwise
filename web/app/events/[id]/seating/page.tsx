'use client';
import {useCallback,useEffect,useMemo,useState} from 'react';
import {useParams} from 'next/navigation';
import {localizeText} from '../../../../lib/localization.mjs';
import {useLocalizationRender} from '../../../../components/LocalizationProvider';
import {hostRequest} from '../../../../lib/host-api.mjs';
import {searchGuests} from '../../../../lib/eventops.mjs';
import '../../../../components/gift-books.css';
import '../../../../components/seating.css';

type Seated={key:string;name:string;party:number};
type Table={id:string;name:string;capacity:number;shape:string;seated:number;guests:Seated[]};
type Unseated={key:string;name:string;party:number;source:string};
type Plan={tables:Table[];unseated:Unseated[];seatedPeople:number;expectedPeople:number;guestLookup:boolean};

/** Tap a family, then tap a table — works the same with a mouse or on a phone. Drag and drop also works on desktop. */
export default function Seating(){useLocalizationRender();
 const {id}=useParams();const eventId=String(id??'');const route=`/v1/events/${eventId}/seating`;
 const [plan,setPlan]=useState<Plan|null>(null),[picked,setPicked]=useState<{name:string;party:number;key:string}|null>(null),[error,setError]=useState(''),[q,setQ]=useState(''),[newTable,setNewTable]=useState({name:'',capacity:'8',shape:'ROUND'}),[walk,setWalk]=useState({name:'',party:'1'});
 const load=useCallback(async()=>{try{setPlan(await hostRequest(fetch,route) as Plan);setError('')}catch(e){setError(e instanceof Error?e.message:'Could not load the plan')}},[route]);
 useEffect(()=>{if(/^[0-9a-f-]{36}$/i.test(eventId))void load()},[eventId,load]);
 async function seat(tableId:string,g:{name:string;party:number}){setError('');try{await hostRequest(fetch,`${route}/assign`,{method:'POST',payload:{guestName:g.name,partySize:g.party,tableId}});setPicked(null);await load()}catch(e){setError(e instanceof Error?e.message:'Could not seat')}}
 async function unseat(key:string){try{await hostRequest(fetch,`${route}/unassign`,{method:'POST',payload:{key}});await load()}catch(e){setError(e instanceof Error?e.message:'Could not move')}}
 async function addTable(e:React.FormEvent){e.preventDefault();try{await hostRequest(fetch,`${route}/tables`,{method:'POST',payload:{name:newTable.name||`Table ${(plan?.tables.length??0)+1}`,capacity:Number(newTable.capacity)||8,shape:newTable.shape}});setNewTable(t=>({...t,name:''}));await load()}catch(err){setError(err instanceof Error?err.message:'Could not add')}}
 async function removeTable(t:Table){if(!confirm(localizeText('Delete {{t}}? Its guests go back to “Not seated”.').replace('{{t}}',t.name)))return;try{await hostRequest(fetch,`${route}/tables/${t.id}/delete`,{method:'POST'});await load()}catch(err){setError(err instanceof Error?err.message:'Could not delete')}}
 async function lookup(on:boolean){try{await hostRequest(fetch,`${route}/settings`,{method:'PUT',payload:{guestLookup:on}});await load()}catch(err){setError(err instanceof Error?err.message:'Could not save')}}
 const unseated=useMemo(()=>searchGuests((plan?.unseated??[]).map(u=>({...u,arrived:false})),q) as (Unseated&{arrived:boolean})[],[plan,q]);
 const drop=(e:React.DragEvent,tableId:string)=>{e.preventDefault();try{const g=JSON.parse(e.dataTransfer.getData('application/json'));void seat(tableId,g)}catch{}};

 return <main className="focusPage gbPage stPage">
  <header className="focusPageHeading"><div><p className="focusEyebrow">{localizeText('Everyone in the right seat')}</p><h1>{localizeText('Seating plan')}</h1>
   <p>{localizeText('Tap a family, then tap their table. On a computer you can also drag them. Tables never get overfilled.')}</p></div>
   <button type="button" className="focusSecondary" onClick={()=>window.print()}>{localizeText('Print chart')}</button></header>
  {error&&<p className="focusError" role="alert">{localizeText(error)}</p>}
  {plan&&<section className="gbStats gbStats--three no-print"><div><span>{localizeText('Seated')}</span><strong>{plan.seatedPeople}</strong></div><div><span>{localizeText('Expected')}</span><strong>{plan.expectedPeople}</strong></div><div><span>{localizeText('Seats')}</span><strong>{plan.tables.reduce((n,t)=>n+t.capacity,0)}</strong></div></section>}
  {plan&&<label className="gbCheck no-print stLookup"><input type="checkbox" checked={plan.guestLookup} onChange={e=>void lookup(e.target.checked)}/>{localizeText('Let guests find their table on the invitation (they type their name)')}</label>}
  {picked&&<p className="stPicked no-print" role="status">{localizeText('Choose a table for {{n}} ({{p}})').replace('{{n}}',picked.name).replace('{{p}}',String(picked.party))} <button type="button" className="gbPlain" onClick={()=>setPicked(null)}>{localizeText('Cancel')}</button></p>}
  <div className="stGrid">
   <aside className="stGuests no-print"><h2>{localizeText('Not seated')} <small>({plan?.unseated.length??0})</small></h2>
    <input type="search" value={q} onChange={e=>setQ(e.target.value)} placeholder={localizeText('Search guests')} aria-label={localizeText('Search guests')}/>
    <ul>{unseated.map(g=><li key={g.key}><button type="button" draggable onDragStart={e=>e.dataTransfer.setData('application/json',JSON.stringify({name:g.name,party:g.party}))} aria-pressed={picked?.key===g.key} onClick={()=>setPicked({name:g.name,party:g.party,key:g.key})}><span>{g.name}</span><em>{g.party}</em></button></li>)}</ul>
    <form className="stWalk" onSubmit={e=>{e.preventDefault();if(walk.name.trim()){setPicked({name:walk.name.trim(),party:Number(walk.party)||1,key:walk.name});setWalk({name:'',party:'1'})}}}>
     <input value={walk.name} maxLength={120} onChange={e=>setWalk({...walk,name:e.target.value})} placeholder={localizeText('Add someone not on the list')}/><input inputMode="numeric" value={walk.party} onChange={e=>setWalk({...walk,party:e.target.value.replace(/\D/g,'')})} aria-label={localizeText('People')}/><button type="submit">{localizeText('Pick')}</button></form>
    <form className="stAdd" onSubmit={addTable}><h3>{localizeText('Add a table')}</h3><input value={newTable.name} maxLength={60} onChange={e=>setNewTable({...newTable,name:e.target.value})} placeholder={localizeText('e.g. Family 1')}/>
     <div><input inputMode="numeric" value={newTable.capacity} onChange={e=>setNewTable({...newTable,capacity:e.target.value.replace(/\D/g,'')})} aria-label={localizeText('Seats')}/><select value={newTable.shape} onChange={e=>setNewTable({...newTable,shape:e.target.value})}><option value="ROUND">{localizeText('Round')}</option><option value="LONG">{localizeText('Long')}</option></select><button className="focusPrimary">{localizeText('Add')}</button></div></form>
   </aside>
   <section className="stTables" aria-label={localizeText('Tables')}>{plan?.tables.length===0&&<p className="gbEmpty">{localizeText('Add your first table to start seating.')}</p>}
    {plan?.tables.map(t=>{const full=t.seated>=t.capacity;return <article key={t.id} className={`stTable stTable--${t.shape.toLowerCase()}${picked&&!full?' is-target':''}${full?' is-full':''}`} onDragOver={e=>e.preventDefault()} onDrop={e=>drop(e,t.id)}>
     <button type="button" className="stTableHead" disabled={!picked} onClick={()=>picked&&void seat(t.id,picked)} aria-label={picked?localizeText('Seat {{n}} at {{t}}').replace('{{n}}',picked.name).replace('{{t}}',t.name):t.name}>
      <strong>{t.name}</strong><span className="stSeats">{t.seated}/{t.capacity}</span></button>
     <div className="stBar" aria-hidden="true"><i style={{width:`${Math.min(100,t.seated/t.capacity*100)}%`}}/></div>
     <ul>{t.guests.map(g=><li key={g.key}><span>{g.name} <em>{g.party}</em></span><button type="button" className="gbPlain no-print" aria-label={localizeText('Move {{n}} back to not seated').replace('{{n}}',g.name)} onClick={()=>void unseat(g.key)}>✕</button></li>)}</ul>
     <button type="button" className="gbPlain no-print stDel" onClick={()=>void removeTable(t)}>{localizeText('Delete table')}</button></article>})}</section>
  </div>
 </main>;
}
