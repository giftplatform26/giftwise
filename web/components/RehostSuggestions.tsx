'use client';
import {useEffect,useState} from 'react';
import {useRouter} from 'next/navigation';
import {localizeText} from '../lib/localization.mjs';
import {useLocalizationRender} from './LocalizationProvider';
import {hostRequest} from '../lib/host-api.mjs';
import {whenLabel} from '../lib/reminders.mjs';
import './growth-pages.css';

type Suggestion={eventId:string;title:string;occasion:string;lastDate:string;nextDate:string;daysAway:number;suggestedTitle:string;guests:number};

/** Dashboard nudge: last year's birthday or anniversary is coming up again. Renders nothing when the feature is off. */
export default function RehostSuggestions(){useLocalizationRender();
 const router=useRouter();const [list,setList]=useState<Suggestion[]>([]),[busy,setBusy]=useState(''),[error,setError]=useState('');
 useEffect(()=>{let active=true;fetch('/backend/api/v1/me/rehost',{credentials:'include',cache:'no-store'}).then(r=>r.ok?r.json():[]).then(v=>{if(active&&Array.isArray(v))setList(v)}).catch(()=>{});return()=>{active=false}},[]);
 if(list.length===0)return null;
 async function repeat(s:Suggestion){setBusy(s.eventId);setError('');try{const r=await hostRequest(fetch,`/v1/me/rehost/${s.eventId}`,{method:'POST',payload:{copyGuests:true}}) as {id:string};router.push(`/events/${r.id}/edit`)}
  catch(e){setError(e instanceof Error?e.message:'Could not create the draft');setBusy('')}}
 return <section className="rhCards" aria-label={localizeText('Coming up again')}>{list.map(s=><article key={s.eventId} className="rhCard">
  <p className="focusEyebrow">{localizeText(whenLabel(s.daysAway))} · {new Intl.DateTimeFormat(undefined,{day:'numeric',month:'long'}).format(new Date(`${s.nextDate}T12:00:00`))}</p>
  <h2>{localizeText('Time to celebrate {{title}} again?').replace('{{title}}',s.title)}</h2>
  <p>{s.guests>0?localizeText('We will copy last year’s details and your {{n}} guests into a new private draft.').replace('{{n}}',String(s.guests)):localizeText('We will copy last year’s details into a new private draft.')}</p>
  <button type="button" className="focusPrimary" disabled={busy===s.eventId} onClick={()=>void repeat(s)}>{busy===s.eventId?localizeText('Creating…'):localizeText('Plan “{{title}}”').replace('{{title}}',s.suggestedTitle)}</button>
 </article>)}{error&&<p className="focusError" role="alert">{localizeText(error)}</p>}</section>;
}
