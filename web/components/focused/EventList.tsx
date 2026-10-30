'use client';
import {useEffect,useState} from 'react';
import Link from 'next/link';
import {hostApi} from '../../lib/host-api.mjs';
import {eventDate,occasionArt} from '../../lib/focused.mjs';
import HostToast from '../../app/components/HostToast';
type Event={id:string;title:string;occasionType:string;eventDate:string|null;status:string};
export default function EventList({overview=false}:{overview?:boolean}){
 const [events,setEvents]=useState<Event[]>([]),[loading,setLoading]=useState(true),[error,setError]=useState('');
 useEffect(()=>{let active=true;hostApi.events(fetch).then(data=>{if(!Array.isArray(data))throw Error('Unable to load celebrations');if(active)setEvents(data)}).catch(e=>{if(active)setError(e.message)}).finally(()=>{if(active)setLoading(false)});return()=>{active=false}},[]);
 const ordered=[...events].sort((a,b)=>(a.status==='COMPLETED'?1:0)-(b.status==='COMPLETED'?1:0)||(a.eventDate||'9999').localeCompare(b.eventDate||'9999'));
 return <main className="focusPage"><HostToast/><header className="focusPageHeading"><div><p className="focusEyebrow">Your moments</p><h1>{overview?'Something worth celebrating.':'Your celebrations'}</h1><p>{overview?'Pick up where you left off.':'A little space for every special occasion.'}</p></div><Link className="focusPrimary" href="/events/new">Create celebration <span aria-hidden="true">＋</span></Link></header>{error&&<div className="focusError" role="alert">{error} <button onClick={()=>location.reload()}>Retry</button></div>}{loading?<div className="focusSkeleton" role="status">Loading celebrations…</div>:ordered.length===0?<section className="focusEmpty"><span aria-hidden="true">✧</span><h2>Make room for a special moment.</h2><p>Your celebrations will live here. Start with an occasion and a few words from the heart.</p></section>:<div className="focusEventGrid">{ordered.map(event=><article className="focusEventCard" key={event.id}><div className="focusEventArtwork" style={{backgroundImage:`url(/invitation-backgrounds/${occasionArt(event.occasionType)}.svg)`}}><span className="focusBadge">{event.status==='DRAFT'?'Private draft':event.status==='PUBLISHED'?'Published':event.status.toLowerCase()}</span></div><div className="focusEventBody"><p className="focusEyebrow">{event.occasionType.replaceAll('_',' ')}</p><h2>{event.title}</h2><p>{eventDate(event.eventDate)}</p><Link className="focusTextLink focusEventCardLink" href={`/events/${event.id}`}>{event.status==='DRAFT'?'Continue setup':'Open celebration'} <span aria-hidden="true">→</span></Link></div></article>)}</div>}</main>;
}
