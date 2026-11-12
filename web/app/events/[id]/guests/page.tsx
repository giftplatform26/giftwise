'use client';
import {useCallback,useEffect,useMemo,useState} from 'react';
import {useParams} from 'next/navigation';
import {localizeText} from '../../../../lib/localization.mjs';
import {useLocalizationRender} from '../../../../components/LocalizationProvider';
import {hostRequest} from '../../../../lib/host-api.mjs';
import {AGES,IMPORT_TEMPLATE,MEALS,RSVP_LABELS,SIDES,blankMember,filterHouseholds,partyOf,sideLabel} from '../../../../lib/households.mjs';
import '../../../../components/gift-books.css';
import '../../../../components/households.css';

type Member={id?:string;name:string;ageGroup:string;meal:string|null;attending:boolean};
type Household={id:string;name:string;side:string|null;group:string|null;phone:string|null;email:string|null;city:string|null;outstation:boolean;expectedSize:number;party:number;adults:number;children:number;infants:number;inviteStatus:string;rsvp:string;note:string|null;version:number;members:Member[]};
type Totals={households:number;people:number;adults:number;children:number;infants:number;yes:number;no:number;maybe:number;pending:number;invited:number;outstation:number;meals:Record<string,number>;yesPeople:number};
type Book={households:Household[];totals:Totals;sides:{side:string;households:number;people:number}[];groups:string[];sideChoices:string[]};
type Draft={name:string;side:string;group:string;phone:string;email:string;city:string;outstation:boolean;expectedSize:string;inviteStatus:string;rsvp:string;note:string;members:{name:string;ageGroup:string;meal:string;attending:boolean}[]};
const empty:Draft={name:'',side:'',group:'',phone:'',email:'',city:'',outstation:false,expectedSize:'1',inviteStatus:'NOT_SENT',rsvp:'PENDING',note:'',members:[]};

/** Guest list by household: one row per family, with members, side, RSVP and invite status. Feeds seating, passes and check-in. */
export default function GuestList(){useLocalizationRender();
 const {id}=useParams();const eventId=String(id??'');const route=`/v1/events/${eventId}/households`;
 const [book,setBook]=useState<Book|null>(null),[error,setError]=useState(''),[notice,setNotice]=useState(''),[busy,setBusy]=useState(false);
 const [filter,setFilter]=useState({q:'',side:'',rsvp:''}),[editing,setEditing]=useState<Household|null>(null),[open,setOpen]=useState(false),[draft,setDraft]=useState<Draft>(empty);
 const [importing,setImporting]=useState(false),[paste,setPaste]=useState(''),[expanded,setExpanded]=useState<string|null>(null);
 const load=useCallback(async()=>{try{setBook(await hostRequest(fetch,route) as Book);setError('')}catch(e){setError(e instanceof Error?e.message:'Could not load the guest list')}},[route]);
 useEffect(()=>{if(/^[0-9a-f-]{36}$/i.test(eventId))void load()},[eventId,load]);
 const shown=useMemo(()=>filterHouseholds(book?.households??[],filter) as Household[],[book,filter]);
 const set=<K extends keyof Draft>(k:K,v:Draft[K])=>setDraft(d=>({...d,[k]:v}));
 const setMember=(i:number,patch:Partial<Draft['members'][number]>)=>setDraft(d=>({...d,members:d.members.map((m,j)=>j===i?{...m,...patch}:m)}));

 function startAdd(){setEditing(null);setDraft(empty);setOpen(true);setImporting(false)}
 function startEdit(h:Household){setEditing(h);setImporting(false);setOpen(true);setDraft({name:h.name,side:h.side??'',group:h.group??'',phone:h.phone??'',email:h.email??'',city:h.city??'',outstation:h.outstation,expectedSize:String(h.expectedSize),inviteStatus:h.inviteStatus,rsvp:h.rsvp,note:h.note??'',members:h.members.map(m=>({name:m.name,ageGroup:m.ageGroup,meal:m.meal??'',attending:m.attending}))});window.scrollTo({top:0,behavior:'smooth'})}
 async function save(e:React.FormEvent){e.preventDefault();setBusy(true);setError('');
  const members=draft.members.filter(m=>m.name.trim()).map(m=>({name:m.name.trim(),ageGroup:m.ageGroup,meal:m.meal||null,attending:m.attending}));
  const body={name:draft.name,side:draft.side||null,group:draft.group||null,phone:draft.phone||null,email:draft.email||null,city:draft.city||null,outstation:draft.outstation,expectedSize:Number(draft.expectedSize)||1,inviteStatus:draft.inviteStatus,rsvp:draft.rsvp,note:draft.note||null,members,...(editing?{version:editing.version}:{})};
  try{await hostRequest(fetch,editing?`${route}/${editing.id}`:route,{method:editing?'PUT':'POST',payload:body});setOpen(false);setEditing(null);setNotice(localizeText('Saved.'));await load()}
  catch(err){setError(err instanceof Error?err.message:'Could not save')}finally{setBusy(false)}}
 async function remove(h:Household){if(!confirm(localizeText('Remove {{n}} from the guest list?').replace('{{n}}',h.name)))return;try{await hostRequest(fetch,`${route}/${h.id}/delete`,{method:'POST'});setOpen(false);setEditing(null);await load()}catch(e){setError(e instanceof Error?e.message:'Could not remove')}}
 async function quick(h:Household,patch:{inviteStatus?:string;rsvp?:string}){try{await hostRequest(fetch,`${route}/${h.id}/status`,{method:'POST',payload:patch});await load()}catch(e){setError(e instanceof Error?e.message:'Could not update')}}
 async function runImport(){setBusy(true);setError('');try{const r=await hostRequest(fetch,`${route}/import`,{method:'POST',payload:{text:paste}}) as {created:number;skipped:number;errors:{line:number;message:string}[]};
  setNotice(localizeText('{{c}} added, {{s}} already on the list.').replace('{{c}}',String(r.created)).replace('{{s}}',String(r.skipped))+(r.errors.length?' '+r.errors.map(x=>localizeText('Line {{l}}: {{m}}').replace('{{l}}',String(x.line)).replace('{{m}}',localizeText(x.message))).join(' · '):''));
  if(!r.errors.length){setPaste('');setImporting(false)}await load()}catch(e){setError(e instanceof Error?e.message:'Could not import')}finally{setBusy(false)}}
 async function importExisting(){setBusy(true);setError('');try{const r=await hostRequest(fetch,`${route}/import-existing`,{method:'POST'}) as {created:number;skipped:number};setNotice(localizeText('{{c}} added from your RSVPs and WhatsApp list.').replace('{{c}}',String(r.created)));await load()}catch(e){setError(e instanceof Error?e.message:'Could not import')}finally{setBusy(false)}}
 const t=book?.totals;

 return <main className="focusPage gbPage hhPage">
  <header className="focusPageHeading"><div><p className="focusEyebrow">{localizeText('Who is invited')}</p><h1>{localizeText('Guest list')}</h1>
   <p>{localizeText('One row per family or household. Add the people in it, which side they are from, and whether they are coming. Seating, guest passes and check-in use this list.')}</p></div>
   <div className="hhHeadActions"><button type="button" className="focusPrimary" onClick={startAdd}>{localizeText('Add a household')}</button>
    <button type="button" className="focusSecondary" onClick={()=>{setImporting(v=>!v);setOpen(false)}}>{localizeText('Paste from a spreadsheet')}</button></div></header>
  {error&&<p className="focusError" role="alert">{localizeText(error)}</p>}
  {notice&&<p className="gbNotice" role="status">{notice}</p>}
  {t&&<section className="gbStats hhStats"><div><span>{localizeText('Households')}</span><strong>{t.households}</strong></div><div><span>{localizeText('People')}</span><strong>{t.people}</strong><small>{localizeText('{{a}} adults · {{c}} children').replace('{{a}}',String(t.adults)).replace('{{c}}',String(t.children+t.infants))}</small></div>
   <div><span>{localizeText('Coming')}</span><strong>{t.yesPeople}</strong><small>{localizeText('{{n}} households').replace('{{n}}',String(t.yes))}</small></div><div><span>{localizeText('Awaiting reply')}</span><strong>{t.pending+t.maybe}</strong></div>
   <div><span>{localizeText('Invites sent')}</span><strong>{t.invited}/{t.households}</strong></div><div><span>{localizeText('Outstation')}</span><strong>{t.outstation}</strong></div></section>}
  {t&&Object.keys(t.meals).length>0&&<p className="hhMeals">{localizeText('Meals')}: {Object.entries(t.meals).map(([k,v])=>`${localizeText(MEALS[k as keyof typeof MEALS]??k)} ${v}`).join(' · ')}</p>}
  {book&&book.sides.length>1&&<p className="hhSides">{book.sides.map(s=><span key={s.side}>{localizeText(sideLabel(s.side))}: <b>{s.people}</b></span>)}</p>}

  {importing&&<section className="gbEntry"><h2>{localizeText('Paste from a spreadsheet')}</h2>
   <p className="gbFine">{localizeText('Copy rows from Excel or Google Sheets and paste them here. Columns, in order: Name, People, Phone, Side, Group, City, Email, Outstation. Only the name is required. Names already on the list are skipped.')}</p>
   <textarea className="hhPaste" rows={8} value={paste} onChange={e=>setPaste(e.target.value)} placeholder={IMPORT_TEMPLATE} aria-label={localizeText('Rows to import')}/>
   <div className="gbEntryActions"><button type="button" className="focusPrimary" disabled={busy||!paste.trim()} onClick={()=>void runImport()}>{localizeText('Import')}</button>
    <button type="button" className="focusSecondary" disabled={busy} onClick={()=>void importExisting()}>{localizeText('Add everyone from RSVPs & WhatsApp list')}</button>
    <button type="button" className="gbPlain" onClick={()=>setImporting(false)}>{localizeText('Close')}</button></div></section>}

  {open&&book&&<form className={`gbEntry${editing?' gbEntry--editing':''}`} onSubmit={save}><h2>{editing?localizeText('Edit household'):localizeText('Add a household')}</h2>
   <div className="gbEntryRow"><label className="gbGrow">{localizeText('Household name')}<input required value={draft.name} maxLength={120} onChange={e=>set('name',e.target.value)} placeholder={localizeText('e.g. Sharma family')}/></label>
    <label>{localizeText('Side')}<select value={draft.side} onChange={e=>set('side',e.target.value)}><option value="">{localizeText('—')}</option>{SIDES.map(s=><option key={s} value={s}>{localizeText(sideLabel(s))}</option>)}</select></label>
    <label>{localizeText('Group')}<input value={draft.group} maxLength={40} list="hh-groups" onChange={e=>set('group',e.target.value)} placeholder={localizeText('e.g. College friends')}/></label>
    <datalist id="hh-groups">{book.groups.map(g=><option key={g} value={g}/>)}</datalist></div>
   <div className="gbEntryRow"><label>{localizeText('Phone (WhatsApp)')}<input type="tel" value={draft.phone} maxLength={20} onChange={e=>set('phone',e.target.value)} placeholder="+91…"/></label>
    <label className="gbGrow">{localizeText('Email')}<input type="email" value={draft.email} maxLength={160} onChange={e=>set('email',e.target.value)}/></label>
    <label>{localizeText('City')}<input value={draft.city} maxLength={80} onChange={e=>set('city',e.target.value)}/></label>
    <label className="gbCheck hhOut"><input type="checkbox" checked={draft.outstation} onChange={e=>set('outstation',e.target.checked)}/>{localizeText('Coming from out of town')}</label></div>
   <div className="gbEntryRow"><label>{localizeText('Invite')}<select value={draft.inviteStatus} onChange={e=>set('inviteStatus',e.target.value)}><option value="NOT_SENT">{localizeText('Not sent yet')}</option><option value="SENT">{localizeText('Sent')}</option></select></label>
    <label>{localizeText('RSVP')}<select value={draft.rsvp} onChange={e=>set('rsvp',e.target.value)}>{Object.entries(RSVP_LABELS).map(([k,v])=><option key={k} value={k}>{localizeText(v)}</option>)}</select></label>
    {draft.members.length===0&&<label>{localizeText('How many people')}<input inputMode="numeric" value={draft.expectedSize} onChange={e=>set('expectedSize',e.target.value.replace(/\D/g,'').slice(0,2))}/></label>}</div>
   <fieldset className="hhMembers"><legend>{localizeText('People in this household')} <small>({localizeText('optional — add names for meal choices and an exact count')})</small></legend>
    {draft.members.map((m,i)=><div className="hhMember" key={i}><input value={m.name} maxLength={120} onChange={e=>setMember(i,{name:e.target.value})} placeholder={localizeText('Name')} aria-label={localizeText('Name')}/>
     <select value={m.ageGroup} onChange={e=>setMember(i,{ageGroup:e.target.value})} aria-label={localizeText('Age group')}>{Object.entries(AGES).map(([k,v])=><option key={k} value={k}>{localizeText(v)}</option>)}</select>
     <select value={m.meal} onChange={e=>setMember(i,{meal:e.target.value})} aria-label={localizeText('Meal')}><option value="">{localizeText('Meal —')}</option>{Object.entries(MEALS).map(([k,v])=><option key={k} value={k}>{localizeText(v)}</option>)}</select>
     <label className="gbCheck"><input type="checkbox" checked={m.attending} onChange={e=>setMember(i,{attending:e.target.checked})}/>{localizeText('Coming')}</label>
     <button type="button" className="gbPlain" aria-label={localizeText('Remove {{n}}').replace('{{n}}',m.name||localizeText('person'))} onClick={()=>setDraft(d=>({...d,members:d.members.filter((_,j)=>j!==i)}))}>✕</button></div>)}
    {draft.members.length<30&&<button type="button" className="gbPlain" onClick={()=>setDraft(d=>({...d,members:[...d.members,blankMember()]}))}>+ {localizeText('Add a person')}</button>}
    <p className="gbFine">{localizeText('People counted: {{n}}').replace('{{n}}',String(partyOf({...draft,expectedSize:Number(draft.expectedSize)})))}</p></fieldset>
   <label>{localizeText('Note')}<input value={draft.note} maxLength={300} onChange={e=>set('note',e.target.value)} placeholder={localizeText('e.g. Needs wheelchair access')}/></label>
   <div className="gbEntryActions"><button className="focusPrimary" disabled={busy}>{localizeText('Save')}</button><button type="button" className="gbPlain" onClick={()=>{setOpen(false);setEditing(null)}}>{localizeText('Cancel')}</button>
    {editing&&<button type="button" className="gbDanger" onClick={()=>void remove(editing)}>{localizeText('Remove')}</button>}</div></form>}

  {book&&book.households.length>0&&<div className="gbListTools hhFilters"><input type="search" value={filter.q} onChange={e=>setFilter({...filter,q:e.target.value})} placeholder={localizeText('Search names, city or phone')} aria-label={localizeText('Search guests')}/>
   <select value={filter.side} onChange={e=>setFilter({...filter,side:e.target.value})} aria-label={localizeText('Side')}><option value="">{localizeText('All sides')}</option>{[...SIDES,'UNASSIGNED'].map(s=><option key={s} value={s}>{localizeText(sideLabel(s))}</option>)}</select>
   <select value={filter.rsvp} onChange={e=>setFilter({...filter,rsvp:e.target.value})} aria-label={localizeText('RSVP')}><option value="">{localizeText('All replies')}</option>{Object.entries(RSVP_LABELS).map(([k,v])=><option key={k} value={k}>{localizeText(v)}</option>)}</select>
   <a className="focusSecondary gbButtonLink" href={`/backend/api${route}/export.csv`} download>{localizeText('Download CSV')}</a></div>}

  {book&&(book.households.length===0?<div className="gbEmpty"><p>{localizeText('Your guest list is empty. Add households one by one, paste them from a spreadsheet, or bring in everyone who already replied.')}</p>
   <button type="button" className="focusSecondary" disabled={busy} onClick={()=>void importExisting()}>{localizeText('Add everyone from RSVPs & WhatsApp list')}</button></div>:
   <ul className="gbRows hhRows">{shown.map(h=><li key={h.id} className={`hhRow hhRow--${h.rsvp.toLowerCase()}`}><div>
    <button type="button" className="hhMain" onClick={()=>setExpanded(x=>x===h.id?null:h.id)} aria-expanded={expanded===h.id}>
     <span className="gbWho"><strong>{h.name}</strong><small>{[h.side?localizeText(sideLabel(h.side)):null,h.group,h.city,h.outstation?localizeText('outstation'):null].filter(Boolean).join(' · ')}</small></span>
     <span className="gbWhat"><strong>{h.party}</strong><small>{localizeText(h.party===1?'person':'people')}</small></span></button>
    <span className="hhQuick"><select value={h.rsvp} onChange={e=>void quick(h,{rsvp:e.target.value})} aria-label={localizeText('RSVP for {{n}}').replace('{{n}}',h.name)}>{Object.entries(RSVP_LABELS).map(([k,v])=><option key={k} value={k}>{localizeText(v)}</option>)}</select>
     <label className="gbCheck"><input type="checkbox" checked={h.inviteStatus==='SENT'} onChange={e=>void quick(h,{inviteStatus:e.target.checked?'SENT':'NOT_SENT'})}/>{localizeText('Invited')}</label>
     <button type="button" className="gbPlain" onClick={()=>startEdit(h)}>{localizeText('Edit')}</button></span></div>
    {expanded===h.id&&<div className="hhDetail">{h.members.length>0?<ul>{h.members.map(m=><li key={m.id}>{m.name} <small>{[localizeText(AGES[m.ageGroup as keyof typeof AGES]??m.ageGroup),m.meal?localizeText(MEALS[m.meal as keyof typeof MEALS]??m.meal):null,m.attending?null:localizeText('not coming')].filter(Boolean).join(' · ')}</small></li>)}</ul>:<p className="gbFine">{localizeText('No names added — counted as {{n}}.').replace('{{n}}',String(h.expectedSize))}</p>}
     {(h.phone||h.email)&&<p className="gbFine">{[h.phone,h.email].filter(Boolean).join(' · ')}</p>}{h.note&&<p className="gbFine">{h.note}</p>}</div>}</li>)}
    {shown.length===0&&<li className="gbEmpty">{localizeText('Nobody matches these filters.')}</li>}</ul>)}
 </main>;
}
