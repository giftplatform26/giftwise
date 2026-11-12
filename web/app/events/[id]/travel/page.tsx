'use client';
import {useCallback,useEffect,useState} from 'react';
import {useParams} from 'next/navigation';
import {localizeText,activeLocale} from '../../../../lib/localization.mjs';
import {useLocalizationRender} from '../../../../components/LocalizationProvider';
import {hostRequest} from '../../../../lib/host-api.mjs';
import {useHostWorkspace} from '../../../../components/HostWorkspace';
import {MODES,clockOnly,dayLabel,legSummary,legTime,toLocalInput,travelShareLink} from '../../../../lib/travel.mjs';
import '../../../../components/gift-books.css';
import '../../../../components/households.css';
import '../../../../components/eventops.css';

type Leg={at:string|null;mode:string|null;ref:string|null;point:string|null};
type Guest={id:string;guestName:string;partySize:number;phone:string|null;formToken:string;arrival:Leg;departure:Leg;pickupNeeded:boolean;dropNeeded:boolean;stayId:string|null;stayName:string|null;roomLabel:string|null;rooms:number;guestNote:string|null;hostNote:string|null;submitted:boolean;version:number};
type Stay={id:string;name:string;address:string|null;phone:string|null;mapUrl:string|null;checkIn:string|null;checkOut:string|null;roomsTotal:number|null;roomsUsed:number;families:number;people:number;note:string|null};
type Movement={travelId:string;guestName:string;partySize:number;at:string;mode:string|null;ref:string|null;point:string|null;transport:boolean;stayName:string|null};
type Board={guests:Guest[];stays:Stay[];days:{date:string;arrivals:Movement[];departures:Movement[]}[];totals:{families:number;people:number;submitted:number;pickups:number;drops:number;withStay:number;withoutArrival:number};suggestions:string[]};
type LegDraft={at:string;mode:string;ref:string;point:string};
type Draft={guestName:string;partySize:string;phone:string;arrival:LegDraft;departure:LegDraft;pickupNeeded:boolean;dropNeeded:boolean;stayId:string;roomLabel:string;rooms:string;hostNote:string};
type StayDraft={name:string;address:string;phone:string;mapUrl:string;checkIn:string;checkOut:string;roomsTotal:string;note:string};
const blankLeg:LegDraft={at:'',mode:'',ref:'',point:''};
const blankStay:StayDraft={name:'',address:'',phone:'',mapUrl:'',checkIn:'',checkOut:'',roomsTotal:'',note:''};
const legDraft=(l:Leg):LegDraft=>({at:toLocalInput(l.at),mode:l.mode??'',ref:l.ref??'',point:l.point??''});
const legBody=(l:LegDraft)=>({at:l.at||null,mode:l.mode||null,ref:l.ref||null,point:l.point||null});

/** Travel & stay: who arrives when, pickups and drops by day, and which hotel and room each family has. */
export default function Travel(){useLocalizationRender();
 const {id}=useParams();const eventId=String(id??'');const route=`/v1/events/${eventId}/travel`;const {event}=useHostWorkspace();const locale=activeLocale();
 const [b,setB]=useState<Board|null>(null),[tab,setTab]=useState<'families'|'days'|'stays'>('families'),[error,setError]=useState(''),[notice,setNotice]=useState(''),[busy,setBusy]=useState(false);
 const [editing,setEditing]=useState<Guest|null>(null),[draft,setDraft]=useState<Draft|null>(null),[add,setAdd]=useState({name:'',party:'1',phone:''});
 const [stayEdit,setStayEdit]=useState<Stay|null>(null),[stay,setStay]=useState<StayDraft|null>(null);
 const load=useCallback(async()=>{try{setB(await hostRequest(fetch,route) as Board);setError('')}catch(e){setError(e instanceof Error?e.message:'Could not load travel')}},[route]);
 useEffect(()=>{if(/^[0-9a-f-]{36}$/i.test(eventId))void load()},[eventId,load]);
 const origin=typeof window!=='undefined'?window.location.origin:'';
 async function call(fn:()=>Promise<unknown>,ok?:string){setBusy(true);setError('');try{await fn();if(ok)setNotice(ok);await load()}catch(e){setError(e instanceof Error?e.message:'Something went wrong')}finally{setBusy(false)}}

 function startEdit(g:Guest){setEditing(g);setDraft({guestName:g.guestName,partySize:String(g.partySize),phone:g.phone??'',arrival:legDraft(g.arrival),departure:legDraft(g.departure),pickupNeeded:g.pickupNeeded,dropNeeded:g.dropNeeded,stayId:g.stayId??'',roomLabel:g.roomLabel??'',rooms:String(g.rooms),hostNote:g.hostNote??''});window.scrollTo({top:0,behavior:'smooth'})}
 const saveGuest=(e:React.FormEvent)=>{e.preventDefault();if(!editing||!draft)return;void call(async()=>{await hostRequest(fetch,`${route}/guests/${editing.id}`,{method:'PUT',payload:{guestName:draft.guestName,partySize:Number(draft.partySize)||1,phone:draft.phone||null,arrival:legBody(draft.arrival),departure:legBody(draft.departure),pickupNeeded:draft.pickupNeeded,dropNeeded:draft.dropNeeded,stayId:draft.stayId||null,roomLabel:draft.roomLabel||null,rooms:Number(draft.rooms)||0,hostNote:draft.hostNote||null,version:editing.version}});setEditing(null);setDraft(null)},localizeText('Saved.'))};
 const addGuest=(e:React.FormEvent)=>{e.preventDefault();if(!add.name.trim())return;void call(async()=>{await hostRequest(fetch,`${route}/guests`,{method:'POST',payload:{guestName:add.name,partySize:Number(add.party)||1,phone:add.phone||null}});setAdd({name:'',party:'1',phone:''})})};
 const saveStay=(e:React.FormEvent)=>{e.preventDefault();if(!stay)return;const body={name:stay.name,address:stay.address||null,phone:stay.phone||null,mapUrl:stay.mapUrl||null,checkIn:stay.checkIn||null,checkOut:stay.checkOut||null,roomsTotal:stay.roomsTotal?Number(stay.roomsTotal):null,note:stay.note||null};
  void call(async()=>{await hostRequest(fetch,stayEdit?`${route}/stays/${stayEdit.id}`:`${route}/stays`,{method:stayEdit?'PUT':'POST',payload:body});setStay(null);setStayEdit(null)},localizeText('Saved.'))};
 const setLeg=(which:'arrival'|'departure',k:keyof LegDraft,v:string)=>setDraft(d=>d&&({...d,[which]:{...d[which],[k]:v}}));
 const t=b?.totals;

 const legFields=(which:'arrival'|'departure')=>draft&&<fieldset className="hhMembers"><legend>{localizeText(which==='arrival'?'Arrival':'Departure')}</legend><div className="gbEntryRow">
  <label>{localizeText('Date & time')}<input type="datetime-local" value={draft[which].at} onChange={e=>setLeg(which,'at',e.target.value)}/></label>
  <label>{localizeText('By')}<select value={draft[which].mode} onChange={e=>setLeg(which,'mode',e.target.value)}><option value="">—</option>{Object.entries(MODES).map(([k,v])=><option key={k} value={k}>{localizeText(v)}</option>)}</select></label>
  <label>{localizeText('Flight / train no.')}<input value={draft[which].ref} maxLength={40} onChange={e=>setLeg(which,'ref',e.target.value)}/></label>
  <label className="gbGrow">{localizeText(which==='arrival'?'Arriving at':'Leaving from')}<input value={draft[which].point} maxLength={120} onChange={e=>setLeg(which,'point',e.target.value)} placeholder={localizeText('e.g. Airport T1, Central station')}/></label></div>
  <label className="gbCheck"><input type="checkbox" checked={which==='arrival'?draft.pickupNeeded:draft.dropNeeded} onChange={e=>setDraft(d=>d&&({...d,[which==='arrival'?'pickupNeeded':'dropNeeded']:e.target.checked}))}/>{localizeText(which==='arrival'?'Needs a pickup':'Needs a drop')}</label></fieldset>;

 return <main className="focusPage gbPage hhPage">
  <header className="focusPageHeading"><div><p className="focusEyebrow">{localizeText('For outstation guests')}</p><h1>{localizeText('Travel & stay')}</h1>
   <p>{localizeText('Keep track of when each family arrives and leaves, who needs a pickup, and where they are staying. Send each family their own link to fill in their travel plans.')}</p></div>
   <a className="focusSecondary gbButtonLink" href={`/backend/api${route}/manifest.csv`} download>{localizeText('Download for transport & hotel')}</a></header>
  {error&&<p className="focusError" role="alert">{localizeText(error)}</p>}
  {notice&&<p className="gbNotice" role="status">{notice}</p>}
  {t&&<section className="gbStats hhStats"><div><span>{localizeText('Families')}</span><strong>{t.families}</strong><small>{localizeText('{{n}} people').replace('{{n}}',String(t.people))}</small></div>
   <div><span>{localizeText('Replied with plans')}</span><strong>{t.submitted}/{t.families}</strong></div><div><span>{localizeText('Pickups')}</span><strong>{t.pickups}</strong></div>
   <div><span>{localizeText('Drops')}</span><strong>{t.drops}</strong></div><div><span>{localizeText('Stay assigned')}</span><strong>{t.withStay}/{t.families}</strong></div></section>}
  {b&&b.suggestions.length>0&&<p className="gbNotice">{localizeText('{{n}} outstation families on your guest list are not here yet.').replace('{{n}}',String(b.suggestions.length))} <button type="button" className="focusSecondary" disabled={busy} onClick={()=>void call(()=>hostRequest(fetch,`${route}/guests/from-households`,{method:'POST'}),localizeText('Added from your guest list.'))}>{localizeText('Add them')}</button></p>}

  {editing&&draft&&b&&<form className="gbEntry gbEntry--editing" onSubmit={saveGuest}><h2>{localizeText('Travel for {{n}}').replace('{{n}}',editing.guestName)}</h2>
   <div className="gbEntryRow"><label className="gbGrow">{localizeText('Family name')}<input required value={draft.guestName} maxLength={120} onChange={e=>setDraft({...draft,guestName:e.target.value})}/></label>
    <label>{localizeText('People')}<input inputMode="numeric" value={draft.partySize} onChange={e=>setDraft({...draft,partySize:e.target.value.replace(/\D/g,'').slice(0,2)})}/></label>
    <label>{localizeText('Phone (WhatsApp)')}<input type="tel" value={draft.phone} maxLength={20} onChange={e=>setDraft({...draft,phone:e.target.value})}/></label></div>
   {legFields('arrival')}{legFields('departure')}
   <fieldset className="hhMembers"><legend>{localizeText('Stay')}</legend><div className="gbEntryRow">
    <label className="gbGrow">{localizeText('Hotel / stay')}<select value={draft.stayId} onChange={e=>setDraft({...draft,stayId:e.target.value})}><option value="">{localizeText('Not staying with us / not decided')}</option>{b.stays.map(s=><option key={s.id} value={s.id}>{s.name}</option>)}</select></label>
    <label>{localizeText('Room')}<input value={draft.roomLabel} maxLength={40} onChange={e=>setDraft({...draft,roomLabel:e.target.value})} placeholder="204"/></label>
    <label>{localizeText('Rooms')}<input inputMode="numeric" value={draft.rooms} onChange={e=>setDraft({...draft,rooms:e.target.value.replace(/\D/g,'').slice(0,2)})}/></label></div>
    {b.stays.length===0&&<p className="gbFine">{localizeText('Add hotels in the “Stays” tab to assign rooms.')}</p>}</fieldset>
   {editing.guestNote&&<p className="gbFine">{localizeText('Note from the family')}: {editing.guestNote}</p>}
   <label>{localizeText('Private note (only hosts see this)')}<input value={draft.hostNote} maxLength={300} onChange={e=>setDraft({...draft,hostNote:e.target.value})}/></label>
   <div className="gbEntryActions"><button className="focusPrimary" disabled={busy}>{localizeText('Save')}</button><button type="button" className="gbPlain" onClick={()=>{setEditing(null);setDraft(null)}}>{localizeText('Cancel')}</button>
    <button type="button" className="gbDanger" onClick={()=>{if(confirm(localizeText('Remove {{n}} from the travel list?').replace('{{n}}',editing.guestName)))void call(async()=>{await hostRequest(fetch,`${route}/guests/${editing.id}/delete`,{method:'POST'});setEditing(null);setDraft(null)})}}>{localizeText('Remove')}</button></div></form>}

  <nav className="gbTabs" role="tablist">{(['families','days','stays'] as const).map(k=><button key={k} type="button" role="tab" aria-selected={tab===k} onClick={()=>setTab(k)}>{localizeText(k==='families'?'Families':k==='days'?'Arrivals & departures':'Stays')}</button>)}</nav>

  {b&&tab==='families'&&<>
   <form className="gbEntryRow" onSubmit={addGuest}><label className="gbGrow">{localizeText('Add a family')}<input value={add.name} maxLength={120} onChange={e=>setAdd({...add,name:e.target.value})} placeholder={localizeText('e.g. Iyer family')}/></label>
    <label>{localizeText('People')}<input inputMode="numeric" value={add.party} onChange={e=>setAdd({...add,party:e.target.value.replace(/\D/g,'').slice(0,2)})}/></label>
    <label>{localizeText('Phone')}<input type="tel" value={add.phone} maxLength={20} onChange={e=>setAdd({...add,phone:e.target.value})}/></label><button className="focusSecondary" disabled={busy}>{localizeText('Add')}</button></form>
   {b.guests.length===0?<p className="gbEmpty">{localizeText('Nobody on the travel list yet. Add families coming from out of town.')}</p>:
   <ul className="gbRows hhRows">{b.guests.map(g=><li key={g.id}><div>
    <button type="button" className="hhMain" onClick={()=>startEdit(g)}><span className="gbWho"><strong>{g.guestName}</strong>
     <small>{g.arrival.at?`${localizeText('Arrives')} ${legTime(g.arrival.at,locale)}${legSummary(g.arrival)?` · ${legSummary(g.arrival)}`:''}`:localizeText('Arrival not known yet')}{g.pickupNeeded?` · ${localizeText('pickup')}`:''}</small>
     <small>{g.stayName?`${g.stayName}${g.roomLabel?` · ${localizeText('room')} ${g.roomLabel}`:''}`:localizeText('No stay assigned')}{g.submitted?` · ✓ ${localizeText('family replied')}`:''}</small></span>
     <span className="gbWhat"><strong>{g.partySize}</strong><small>{localizeText('people')}</small></span></button>
    <span className="hhQuick"><a className="focusSecondary gbButtonLink" href={travelShareLink(origin,g,event?.title)} target="_blank" rel="noopener noreferrer">{localizeText('Send link')}</a>
     <button type="button" className="gbPlain" onClick={()=>{void navigator.clipboard?.writeText(`${origin}/t/${g.formToken}`);setNotice(localizeText('Link copied.'))}}>{localizeText('Copy link')}</button></span></div></li>)}</ul>}</>}

  {b&&tab==='days'&&(b.days.length===0?<p className="gbEmpty">{localizeText('Arrival and departure times will appear here, day by day.')}</p>:b.days.map(d=><section key={d.date} className="eoCat"><h2 className="gbSubhead">{dayLabel(d.date,locale)}</h2>
   {[['Arrivals',d.arrivals,'pickup'],['Departures',d.departures,'drop']].map(([label,list,need])=>(list as Movement[]).length>0&&<div key={label as string}><h3 className="gbFine">{localizeText(label as string)}</h3>
    <ul className="gbRows">{(list as Movement[]).map(m=><li key={m.travelId+label}><div><span className="gbWho"><strong>{clockOnly(m.at,locale)} · {m.guestName}</strong><small>{[legSummary(m),m.stayName].filter(Boolean).join(' · ')}</small></span>
     <span className="gbWhat"><strong>{m.partySize}</strong><small>{m.transport?localizeText(`needs ${need}`):''}</small></span></div></li>)}</ul></div>)}</section>))}

  {b&&tab==='stays'&&<>
   {stay?<form className="gbEntry" onSubmit={saveStay}><h2>{stayEdit?localizeText('Edit stay'):localizeText('Add a hotel or stay')}</h2>
    <div className="gbEntryRow"><label className="gbGrow">{localizeText('Name')}<input required value={stay.name} maxLength={120} onChange={e=>setStay({...stay,name:e.target.value})} placeholder={localizeText('e.g. Hotel Lotus')}/></label>
     <label>{localizeText('Rooms booked')}<input inputMode="numeric" value={stay.roomsTotal} onChange={e=>setStay({...stay,roomsTotal:e.target.value.replace(/\D/g,'').slice(0,4)})}/></label>
     <label>{localizeText('Front desk phone')}<input type="tel" value={stay.phone} maxLength={20} onChange={e=>setStay({...stay,phone:e.target.value})}/></label></div>
    <div className="gbEntryRow"><label className="gbGrow">{localizeText('Address')}<input value={stay.address} maxLength={300} onChange={e=>setStay({...stay,address:e.target.value})}/></label>
     <label className="gbGrow">{localizeText('Map link')}<input value={stay.mapUrl} maxLength={500} onChange={e=>setStay({...stay,mapUrl:e.target.value})} placeholder="https://maps…"/></label></div>
    <div className="gbEntryRow"><label>{localizeText('Check-in')}<input type="date" value={stay.checkIn} onChange={e=>setStay({...stay,checkIn:e.target.value})}/></label>
     <label>{localizeText('Check-out')}<input type="date" value={stay.checkOut} onChange={e=>setStay({...stay,checkOut:e.target.value})}/></label>
     <label className="gbGrow">{localizeText('Note for guests')}<input value={stay.note} maxLength={300} onChange={e=>setStay({...stay,note:e.target.value})} placeholder={localizeText('e.g. Breakfast included')}/></label></div>
    <div className="gbEntryActions"><button className="focusPrimary" disabled={busy}>{localizeText('Save')}</button><button type="button" className="gbPlain" onClick={()=>{setStay(null);setStayEdit(null)}}>{localizeText('Cancel')}</button>
     {stayEdit&&<button type="button" className="gbDanger" onClick={()=>{if(confirm(localizeText('Delete {{n}}? Families staying there will show “No stay assigned”.').replace('{{n}}',stayEdit.name)))void call(async()=>{await hostRequest(fetch,`${route}/stays/${stayEdit.id}/delete`,{method:'POST'});setStay(null);setStayEdit(null)})}}>{localizeText('Delete')}</button>}</div></form>
   :<button type="button" className="focusSecondary" onClick={()=>{setStayEdit(null);setStay(blankStay)}}>{localizeText('Add a hotel or stay')}</button>}
   {b.stays.length===0?<p className="gbEmpty">{localizeText('Add the hotels or guest houses you have booked for guests.')}</p>:
   <ul className="gbRows">{b.stays.map(s=><li key={s.id}><button type="button" onClick={()=>{setStayEdit(s);setStay({name:s.name,address:s.address??'',phone:s.phone??'',mapUrl:s.mapUrl??'',checkIn:s.checkIn??'',checkOut:s.checkOut??'',roomsTotal:s.roomsTotal==null?'':String(s.roomsTotal),note:s.note??''})}}>
    <span className="gbWho"><strong>{s.name}</strong><small>{[s.address,s.checkIn&&s.checkOut?`${dayLabel(s.checkIn,locale)} → ${dayLabel(s.checkOut,locale)}`:null].filter(Boolean).join(' · ')}</small></span>
    <span className="gbWhat"><strong>{s.roomsUsed}{s.roomsTotal!=null?`/${s.roomsTotal}`:''}</strong><small>{localizeText('rooms · {{f}} families, {{p}} people').replace('{{f}}',String(s.families)).replace('{{p}}',String(s.people))}</small></span></button></li>)}</ul>}</>}
 </main>;
}
