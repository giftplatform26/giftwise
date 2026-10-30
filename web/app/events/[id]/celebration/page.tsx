 'use client';
import {useEffect,useState} from 'react';
import {useParams} from 'next/navigation';
import Link from 'next/link';
import {csrfCookie} from '../../../../lib/celebration.mjs';
import './celebration-host.css';
import {useExperience} from '../../../../components/ExperienceProvider';
type Rsvp={id:string;guestName:string;guestEmail:string;attendance:string;partySize:number};
type Wish={id:string;guestName:string;message:string;status:string;photoAvailable?:boolean};
type Summary={yes:number;no:number;maybe:number;attendees:number};
const replyText:Record<string,string>={YES:'Attending',NO:'Not attending',MAYBE:'Maybe'};
export default function HostCelebration(){
 const {mode}=useExperience();const focused=mode==='FOCUSED';const [tab,setTab]=useState('responses'),[capabilities,setCapabilities]=useState<Record<string,boolean>>({});
 const {id}=useParams();const eventId=String(id||'');const [rsvps,setRsvps]=useState<Rsvp[]>([]);const [wishes,setWishes]=useState<Wish[]>([]);const [summary,setSummary]=useState<Summary|null>(null);const [error,setError]=useState('');const [loading,setLoading]=useState(true);const [updating,setUpdating]=useState<string|null>(null);const [filter,setFilter]=useState('ALL');
 async function reload(){try{
  const base=`/backend/api/v1/events/${encodeURIComponent(eventId)}`;
  let caps:Record<string,boolean>={RSVP:true,GUESTBOOK:true};
  if(focused){const r=await fetch(base+'/features',{credentials:'include',cache:'no-store'});if(!r.ok)throw Error('Unable to load guest settings.');caps=await r.json();setCapabilities(caps);}
  const paths=[...(caps.RSVP?['/rsvps','/rsvps/summary']:[]),...(caps.GUESTBOOK?['/wishes']:[])];
  const responses=await Promise.all(paths.map(p=>fetch(base+p,{credentials:'include',cache:'no-store'})));
  if(responses.some(r=>!r.ok))throw Error('Unable to load event responses. Sign in as the event host.');
  const rows=await Promise.all(responses.map(r=>r.json()));
  setRsvps(caps.RSVP?rows[0]:[]);setSummary(caps.RSVP?rows[1]:null);setWishes(caps.GUESTBOOK?rows[caps.RSVP?2:0]:[]);setError('');
 }catch(e){setError(e instanceof Error?e.message:'Unable to load')}finally{setLoading(false)}}

 useEffect(()=>{if(eventId)void reload()},[eventId]);
 async function moderate(wishId:string,status:'APPROVED'|'HIDDEN'){setUpdating(wishId);try{const r=await fetch(`/backend/api/v1/events/${encodeURIComponent(eventId)}/wishes/${encodeURIComponent(wishId)}/moderate?status=${status}`,{method:'POST',credentials:'include',headers:{'X-XSRF-TOKEN':csrfCookie(document.cookie)}});if(!r.ok)throw Error('Could not update message');await reload()}catch(e){setError(e instanceof Error?e.message:'Unable to moderate')}finally{setUpdating(null)}}
 const visibleTab=capabilities.RSVP?tab:'wishes';
 const filtered=rsvps.filter(r=>filter==='ALL'||r.attendance===filter);
 return <main className="ch-page"><div className="ch-container"><Link className="ch-back" href="/events">← Back to my events</Link><header className="ch-hero"><div><span className="ch-eyebrow">YOUR CELEBRATION · GUEST RESPONSES</span><h1>{focused?'Guests & wishes':'RSVP & Guestbook'}</h1><p>Stay close to every guest and cherish the wishes they share.</p></div><button className="ch-refresh" type="button" onClick={()=>void reload()}>↻ Refresh responses</button></header>{error&&<p className="ch-error" role="alert">{error}</p>}
 {focused&&<div className="focusSubnav" aria-label="Guest views">{capabilities.RSVP&&<button aria-pressed={visibleTab==='responses'} onClick={()=>setTab('responses')}>Responses</button>}{capabilities.GUESTBOOK&&<button aria-pressed={visibleTab==='wishes'} onClick={()=>setTab('wishes')}>Wishes</button>}</div>}
 {(!focused||visibleTab==='responses')&&<section className="ch-stats" aria-label="Attendance overview">{([['Joyfully accepted',summary?.yes??0,'🎉'],['Unable to attend',summary?.no??0,'💌'],['Maybe',summary?.maybe??0,'✨'],['Total attending',summary?.attendees??0,'👥']] as const).map(([label,count,emoji])=><article className="ch-stat" key={label}><span>{emoji}</span><strong>{loading?'…':count}</strong><p>{label}</p></article>)}</section>}
 {(!focused||(visibleTab==='responses'&&capabilities.RSVP))&&<section className="ch-panel"><div className="ch-panel-head"><div><span className="ch-eyebrow">ATTENDANCE</span><h2>Guest responses</h2><p>Keep track of who is joining your celebration.</p></div><div className="ch-filter"><label htmlFor="ch-rsvp-filter">Show</label><select id="ch-rsvp-filter" value={filter} onChange={e=>setFilter(e.target.value)}><option value="ALL">All responses</option><option value="YES">Attending</option><option value="NO">Not attending</option><option value="MAYBE">Maybe</option></select></div></div>{filtered.length===0?<div className="ch-empty">{loading?'Loading responses…':'No guest responses in this category yet.'}</div>:<div className="ch-table-scroll"><table className="ch-table"><thead><tr><th>Guest</th><th>Email</th><th>Response</th><th>People</th></tr></thead><tbody>{filtered.map(r=><tr key={r.id}><td><strong>{r.guestName}</strong></td><td>{r.guestEmail}</td><td><span className={`ch-pill ch-${r.attendance.toLowerCase()}`}>{replyText[r.attendance]??r.attendance}</span></td><td>{r.partySize}</td></tr>)}</tbody></table></div>}</section>}
 {(!focused||(visibleTab==='wishes'&&capabilities.GUESTBOOK))&&<section className="ch-panel"><div className="ch-panel-head"><div><span className="ch-eyebrow">MESSAGES & MEMORIES</span><h2>Guestbook moderation</h2><p>Approve heartfelt wishes before they appear on the public invitation.</p></div><span className="ch-count">{wishes.filter(w=>w.status==='PENDING').length} awaiting review</span></div>{wishes.length===0?<div className="ch-empty">No wishes yet. Messages from guests will appear here.</div>:<div className="ch-wishes">{wishes.map(w=><article className="ch-wish" key={w.id}><div className="ch-wish-top"><strong>♡ {w.guestName}</strong><span className={`ch-pill ch-${w.status.toLowerCase()}`}>{w.status==='PENDING'?'Awaiting review':w.status==='APPROVED'?'Publicly visible':'Hidden'}</span></div>{w.photoAvailable&&<img className="ch-wish-photo" loading="lazy" alt={`Photo from ${w.guestName}`} src={`/backend/api/v1/events/${encodeURIComponent(eventId)}/wishes/${encodeURIComponent(w.id)}/photo`}/>}<p>{w.message}</p><div className="ch-actions"><button type="button" className="ch-approve" disabled={updating===w.id||w.status==='APPROVED'} onClick={()=>void moderate(w.id,'APPROVED')}>{w.status==='APPROVED'?'✓ Approved':'✓ Approve & publish'}</button><button type="button" className="ch-hide" disabled={updating===w.id||w.status==='HIDDEN'} onClick={()=>void moderate(w.id,'HIDDEN')}>{w.status==='HIDDEN'?'Hidden':'Hide message'}</button></div></article>)}</div>}</section>}
 </div></main>;
}
