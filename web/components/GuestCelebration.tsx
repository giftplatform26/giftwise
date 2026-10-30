 'use client';
import {useEffect,useState} from 'react';
import {guestPost} from '../lib/celebration.mjs';
import './celebration.css';
import {useExperience} from './ExperienceProvider';
import SocialSharing from './SocialSharing';
import useEventFeatures,{type EventFeatures} from './useEventFeatures';
type Wish={id:string;guestName:string;message:string;photoAvailable?:boolean};
export default function GuestCelebration({token,title,features:initialFeatures}:{token:string;title:string;features?:EventFeatures}) {
 const {mode}=useExperience();const focused=mode==='FOCUSED';const [showWishes,setShowWishes]=useState(false);
 const {features}=useEventFeatures(token,initialFeatures);
 const [name,setName]=useState('');const [email,setEmail]=useState('');const [attendance,setAttendance]=useState('YES');const [party,setParty]=useState(1);
 const [wishName,setWishName]=useState('');const [message,setMessage]=useState('');const [photo,setPhoto]=useState<File|null>(null);const [wishes,setWishes]=useState<Wish[]>([]);
 const [rsvpResult,setRsvpResult]=useState('');const [wishResult,setWishResult]=useState('');const [rsvpOk,setRsvpOk]=useState(false);const [wishOk,setWishOk]=useState(false);const [busy,setBusy]=useState(false);
 const refreshWishes=()=>fetch(`/backend/api/public/celebrations/${encodeURIComponent(token)}/wishes`,{cache:'no-store'}).then(r=>r.ok?r.json():[]).then(v=>setWishes(Array.isArray(v)?v:[])).catch(()=>{});
 useEffect(()=>{if(!features.GUESTBOOK){setWishes([]);return;}let active=true;fetch(`/backend/api/public/celebrations/${encodeURIComponent(token)}/wishes`,{cache:'no-store'}).then(r=>r.ok?r.json():[]).then(v=>{if(active)setWishes(Array.isArray(v)?v:[])}).catch(()=>{});return()=>{active=false}},[token,features.GUESTBOOK]);
 async function submitRsvp(e:React.FormEvent){e.preventDefault();setBusy(true);setRsvpResult('');setRsvpOk(false);try{await guestPost(fetch,token,'rsvp',{guestName:name,guestEmail:email,attendance,partySize:attendance==='YES'?party:0});setRsvpOk(true);setRsvpResult('Thank you! Your response has been shared with the host.')}catch(err){setRsvpResult(err instanceof Error?err.message:'Unable to submit')}finally{setBusy(false)}}
 async function submitWish(e:React.FormEvent){e.preventDefault();setBusy(true);setWishResult('');setWishOk(false);try{if(photo){const data=new FormData();data.append('guestName',wishName);data.append('message',message);data.append('photo',photo);const r=await fetch(`/backend/api/public/celebrations/${encodeURIComponent(token)}/wishes/photo`,{method:'POST',body:data});if(!r.ok)throw Error(r.status===413?'Photo is too large':`Unable to send photo (${r.status})`)}else{await guestPost(fetch,token,'wishes',{guestName:wishName,message})}setWishOk(true);setWishResult('Your lovely message has been sent to the host for approval.');setMessage('');setPhoto(null)}catch(err){setWishResult(err instanceof Error?err.message:'Unable to submit')}finally{setBusy(false)}}
 if(!features.RSVP&&!features.GUESTBOOK&&!features.WHATSAPP_SHARING&&!features.SOCIAL_SHARING)return null;
 return <section id="guest-celebration" className="ce-wrap" aria-label="Celebrate together">
  {(features.RSVP||features.GUESTBOOK)&&<div className="ce-heading"><span className="ce-eyebrow">✦ BE PART OF THE MOMENT ✦</span><h2>Celebrate together</h2><p>Your presence and warm wishes make this day even more special.</p></div>}
  <div className="ce-grid">
   {features.RSVP&&<details className="ce-card" open={focused?undefined:true}><summary>{focused?'Will you join us? · RSVP':'Attendance'}</summary><div className="ce-card-icon">✉️</div><span className="ce-eyebrow">THE INVITATION</span><h3>Will you join us?</h3><p>Let the host know if you can make it.</p>
    <form onSubmit={submitRsvp} className="ce-form">
     <label>Your name<input aria-label="RSVP guest name" placeholder="Your full name" required maxLength={120} value={name} onChange={e=>setName(e.target.value)}/></label>
     <label>Email address<input aria-label="RSVP email" type="email" placeholder="you@example.com" required maxLength={254} value={email} onChange={e=>setEmail(e.target.value)}/></label>
     <fieldset className="ce-attendance"><legend>Will you attend?</legend><div className="ce-options">{([['YES','🎉','Joyfully accept'],['NO','💌','Can’t make it'],['MAYBE','✨','Maybe']] as const).map(([value,emoji,label])=><label key={value} className={`ce-option ${attendance===value?'ce-option-selected':''}`}><input type="radio" name="attendance" value={value} checked={attendance===value} onChange={()=>setAttendance(value)}/><span>{emoji}</span><strong>{label}</strong></label>)}</div></fieldset>
     {attendance==='YES'&&<label>People attending (including you)<input type="number" min={1} max={10} required value={party} onChange={e=>setParty(Number(e.target.value))}/></label>}
     <button className="ce-primary" type="submit" disabled={busy}>{busy?'Sending…':'Send my RSVP →'}</button><p className={`ce-feedback ${rsvpOk?'ce-success':''}`} role="status">{rsvpResult}</p>
    </form></details>}
   {features.GUESTBOOK&&<details className="ce-card" open={focused?undefined:true}><summary>{focused?'Leave a little love · Write a wish':'Guestbook'}</summary><div className="ce-card-icon">💖</div><span className="ce-eyebrow">FROM THE HEART</span><h3>Leave your wishes</h3><p>A few kind words become a beautiful keepsake.</p>
    <form onSubmit={submitWish} className="ce-form"><label>Your name<input aria-label="Guestbook name" placeholder="Your name" required maxLength={120} value={wishName} onChange={e=>setWishName(e.target.value)}/></label><label>Your message<textarea aria-label="Guestbook message" placeholder="Write your heartfelt wishes here…" required maxLength={1000} rows={5} value={message} onChange={e=>setMessage(e.target.value)}/></label><label className="ce-photo-upload">📷 Add a photo (optional) <input aria-label="Guestbook photo" type="file" accept="image/jpeg,image/png,image/webp,image/heic,image/heif,.heic,.heif" onChange={e=>{const f=e.target.files?.[0]??null;if(f&&f.size>5*1024*1024){setWishResult('Please select a photo under 5 MB.');setPhoto(null);e.target.value='';return}setPhoto(f)}}/>{photo&&<span className="ce-photo-selected">✓ {photo.name} <button type="button" onClick={()=>setPhoto(null)}>Remove</button></span>}<small>iPhone HEIC/HEIF, JPEG, PNG or WebP. Original up to 5 MB; optimized to JPEG.</small></label><button className="ce-primary" type="submit" disabled={busy}>{busy?'Sending…':'Send my wishes ♡'}</button><p className={`ce-feedback ${wishOk?'ce-success':''}`} role="status">{wishResult}</p><small className="ce-hint">Messages appear publicly after the host approves them.</small></form>
   </details>}
  </div>
  {features.GUESTBOOK&&<section className="ce-wishes" aria-label="Approved guestbook wishes"><div className="ce-wishes-heading"><div><span className="ce-eyebrow">A LITTLE LOVE FROM EVERYONE</span><h3>Wishes from loved ones</h3></div><button type="button" className="ce-refresh" onClick={()=>void refreshWishes()}>↻ Refresh wishes</button></div>{wishes.length===0?<p className="ce-empty">Approved wishes will appear here. Be the first to leave a heartfelt message!</p>:<div className="ce-wish-grid">{(focused&&!showWishes?wishes.slice(0,3):wishes).map(w=><blockquote className="ce-wish" key={w.id}><span aria-hidden="true">❝</span>{w.photoAvailable&&<img className="ce-wish-photo" loading="lazy" alt={`Photo shared by ${w.guestName}`} src={`/backend/api/public/celebrations/${encodeURIComponent(token)}/wishes/${encodeURIComponent(w.id)}/photo`}/>}<p>{w.message}</p><footer>— {w.guestName}</footer></blockquote>)}</div>}</section>}
  {focused&&wishes.length>3&&<button type="button" onClick={()=>setShowWishes(v=>!v)}>{showWishes?'Show fewer wishes':'View all wishes'}</button>}
  
  <SocialSharing token={token} title={title} social={features.SOCIAL_SHARING===true} whatsapp={features.WHATSAPP_SHARING}/>
 </section>;
}
