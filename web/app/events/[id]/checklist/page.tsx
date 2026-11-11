'use client';
import {useCallback,useEffect,useMemo,useState} from 'react';
import {useParams} from 'next/navigation';
import {localizeText} from '../../../../lib/localization.mjs';
import {useLocalizationRender} from '../../../../components/LocalizationProvider';
import {hostRequest} from '../../../../lib/host-api.mjs';
import {groupTasks,percent} from '../../../../lib/eventops.mjs';
import '../../../../components/gift-books.css';
import '../../../../components/eventops.css';

type Task={id:string;title:string;category:string|null;dueDate:string|null;assigneeId:string|null;assigneeName:string|null;note:string|null;done:boolean;overdue:boolean;version:number};
type Member={userId:string;name:string;role:string};
type Checklist={eventDate:string|null;tasks:Task[];team:Member[];progress:{total:number;done:number;overdue:number;dueThisWeek:number};categories:string[];canAddStarter:boolean};
type Draft={title:string;category:string;dueDate:string;assigneeId:string;note:string};
const empty:Draft={title:'',category:'',dueDate:'',assigneeId:'',note:''};
const day=(d:string)=>new Intl.DateTimeFormat(undefined,{day:'numeric',month:'short'}).format(new Date(`${d}T12:00:00`));
const GROUPS:[keyof ReturnType<typeof groupTasks>,string][]=[['overdue','Overdue'],['week','This week'],['later','Coming up'],['undated','No date yet'],['done','Done']];

export default function PlanningChecklist(){useLocalizationRender();
 const {id}=useParams();const eventId=String(id??'');const route=`/v1/events/${eventId}/tasks`;
 const [data,setData]=useState<Checklist|null>(null),[error,setError]=useState(''),[busy,setBusy]=useState(false),[draft,setDraft]=useState<Draft>(empty),[editing,setEditing]=useState<Task|null>(null),[mine,setMine]=useState(false),[me,setMe]=useState<string>('');
 const load=useCallback(async()=>{try{setData(await hostRequest(fetch,route) as Checklist);setError('')}catch(e){setError(e instanceof Error?e.message:'Could not load the checklist')}},[route]);
 useEffect(()=>{if(/^[0-9a-f-]{36}$/i.test(eventId))void load()},[eventId,load]);
 useEffect(()=>{hostRequest(fetch,'/auth/me').then((u:{userId?:string})=>setMe(u?.userId??'')).catch(()=>{})},[]);
 const set=(k:keyof Draft,v:string)=>setDraft(d=>({...d,[k]:v}));
 async function save(e:React.FormEvent){e.preventDefault();if(!draft.title.trim()){setError('Add the task');return}setBusy(true);setError('');
  const body={title:draft.title,category:draft.category||null,dueDate:draft.dueDate||null,assigneeId:draft.assigneeId||null,note:draft.note||null,...(editing?{version:editing.version}:{})};
  try{await hostRequest(fetch,editing?`${route}/${editing.id}`:route,{method:editing?'PUT':'POST',payload:body});setDraft(empty);setEditing(null);await load()}catch(err){setError(err instanceof Error?err.message:'Could not save')}finally{setBusy(false)}}
 async function toggle(t:Task){setData(d=>d&&{...d,tasks:d.tasks.map(x=>x.id===t.id?{...x,done:!t.done}:x)});try{await hostRequest(fetch,`${route}/${t.id}/done`,{method:'POST',payload:{done:!t.done}});await load()}catch(err){setError(err instanceof Error?err.message:'Could not update');void load()}}
 async function remove(t:Task){if(!confirm(localizeText('Delete “{{task}}”?').replace('{{task}}',t.title)))return;try{await hostRequest(fetch,`${route}/${t.id}/delete`,{method:'POST'});setEditing(null);setDraft(empty);await load()}catch(err){setError(err instanceof Error?err.message:'Could not delete')}}
 async function starter(){setBusy(true);try{await hostRequest(fetch,`${route}/starter`,{method:'POST'});await load()}catch(err){setError(err instanceof Error?err.message:'Could not add tasks')}finally{setBusy(false)}}
 const tasks=useMemo(()=>(data?.tasks??[]).filter(t=>!mine||t.assigneeId===me),[data,mine,me]);
 const groups=useMemo(()=>groupTasks(tasks),[tasks]);
 const p=data?.progress;const pct=percent(p?.done??0,p?.total??0);
 const daysLeft=data?.eventDate?Math.ceil((new Date(`${data.eventDate}T12:00:00`).getTime()-Date.now())/86400000):null;

 return <main className="focusPage gbPage">
  <header className="focusPageHeading"><div><p className="focusEyebrow">{daysLeft!=null&&daysLeft>=0?localizeText('{{n}} days to go').replace('{{n}}',String(daysLeft)):localizeText('Plan together')}</p><h1>{localizeText('Planning checklist')}</h1>
   <p>{localizeText('Everything to do before the big day, with who is doing it. Co-hosts see and tick off the same list.')}</p></div></header>
  {error&&<p className="focusError" role="alert">{localizeText(error)}</p>}
  {p&&p.total>0&&<section className="eoProgress" aria-label={localizeText('Progress')}>
   <div className="eoRing" style={{'--pct':`${pct}%`} as React.CSSProperties} role="img" aria-label={localizeText('{{p}}% done').replace('{{p}}',String(pct))}><strong>{pct}%</strong></div>
   <div><strong>{localizeText('{{d}} of {{t}} tasks done').replace('{{d}}',String(p.done)).replace('{{t}}',String(p.total))}</strong>
    <p>{p.overdue>0?<span className="eoBad">{localizeText('{{n}} overdue').replace('{{n}}',String(p.overdue))}</span>:localizeText('Nothing overdue')} · {localizeText('{{n}} due this week').replace('{{n}}',String(p.dueThisWeek))}</p></div>
   {me&&<label className="gbCheck eoMine"><input type="checkbox" checked={mine} onChange={e=>setMine(e.target.checked)}/>{localizeText('Only my tasks')}</label>}</section>}
  {data?.canAddStarter&&<section className="gbEmpty eoStarter"><h2>{localizeText('Start with a ready-made plan')}</h2><p>{localizeText('We will add the usual tasks for this kind of celebration, dated back from your event. Edit or delete anything.')}</p>
   <div className="gbEntryActions"><button type="button" className="focusPrimary" disabled={busy} onClick={()=>void starter()}>{localizeText('Add starter tasks')}</button><span className="gbFine">{localizeText('or add your own below')}</span></div></section>}
  {data&&<form className={`gbEntry${editing?' gbEntry--editing':''}`} onSubmit={save}>
   <h2>{editing?localizeText('Edit task'):localizeText('Add a task')}</h2>
   <div className="gbEntryRow"><label className="gbGrow">{localizeText('Task')}<input value={draft.title} maxLength={160} onChange={e=>set('title',e.target.value)} placeholder={localizeText('e.g. Book the mehendi artist')}/></label>
    <label>{localizeText('Due')}<input type="date" value={draft.dueDate} onChange={e=>set('dueDate',e.target.value)}/></label></div>
   <div className="gbEntryRow"><label>{localizeText('Category')}<select value={draft.category} onChange={e=>set('category',e.target.value)}><option value="">—</option>{data.categories.map(c=><option key={c} value={c}>{localizeText(c)}</option>)}</select></label>
    <label className="gbGrow">{localizeText('Who')}<select value={draft.assigneeId} onChange={e=>set('assigneeId',e.target.value)}><option value="">{localizeText('Not assigned')}</option>{data.team.map(m=><option key={m.userId} value={m.userId}>{m.name}{m.role==='OWNER'?'':` (${localizeText(m.role==='CO_HOST'?'co-host':'gift desk')})`}</option>)}</select></label></div>
   <label>{localizeText('Note')}<input value={draft.note} maxLength={500} onChange={e=>set('note',e.target.value)}/></label>
   <div className="gbEntryActions"><button className="focusPrimary" disabled={busy}>{editing?localizeText('Save task'):localizeText('Add task')}</button>
    {editing&&<><button type="button" className="gbPlain" onClick={()=>{setEditing(null);setDraft(empty)}}>{localizeText('Cancel')}</button><button type="button" className="gbDanger" onClick={()=>void remove(editing)}>{localizeText('Delete')}</button></>}</div>
  </form>}
  {data&&GROUPS.map(([k,label])=>groups[k].length===0?null:<section key={k} className={`eoGroup eoGroup--${k}`}><h2 className="gbSubhead">{localizeText(label)} <small>({groups[k].length})</small></h2>
   <ul className="eoTasks">{groups[k].map((t:Task)=><li key={t.id} className={t.done?'is-done':''}>
    <label className="eoTick"><input type="checkbox" checked={t.done} onChange={()=>void toggle(t)} aria-label={localizeText(t.done?'Mark “{{t}}” not done':'Mark “{{t}}” done').replace('{{t}}',t.title)}/></label>
    <button type="button" className="eoTaskBody" onClick={()=>{setEditing(t);setDraft({title:t.title,category:t.category??'',dueDate:t.dueDate??'',assigneeId:t.assigneeId??'',note:t.note??''});window.scrollTo({top:0,behavior:'smooth'})}}>
     <strong>{t.title}</strong><small>{[t.category&&localizeText(t.category),t.assigneeName,t.note].filter(Boolean).join(' · ')}</small></button>
    {t.dueDate&&<span className={`eoDue${t.overdue?' eoDue--late':''}`}>{day(t.dueDate)}</span>}</li>)}</ul></section>)}
 </main>;
}
