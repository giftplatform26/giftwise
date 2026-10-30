'use client';
import GiftDescription from '../../../components/GiftDescription';
import dynamic from 'next/dynamic';
import {useExperience} from '../../../components/ExperienceProvider';
const FocusedInvitation=dynamic(()=>import('../../../components/focused/Invitation'),{loading:()=> <main className="focusPage" role="status">Opening…</main>});
import {useCallback,useEffect,useState} from 'react';
import {sortWishlist} from '../../../lib/gift-sorting.mjs';
import {useParams} from 'next/navigation';
import Link from 'next/link';
import {startScreenTiming} from '../../../lib/diagnostics.mjs';
import './public-event.css';
import GuestContributionCheckout from '../../../components/GuestContributionCheckout';
import type {EventFeatures} from '../../../components/useEventFeatures';
import GuestActions from '../../../components/GuestActions';
import GuestCelebration from '../../../components/GuestCelebration';

type Gift={id:string;name:string;description:string|null;targetAmount:number;imageUrl?:string|null;priority?:'HIGH'|'MEDIUM'|'LOW'};
type Progress={giftId:string;contributedPaise:number;contributionCount:number};
type Event={title:string;description:string|null;occasionType:string;eventDate:string|null;gifts:Gift[];features?:EventFeatures};
const icon:Record<string,string>={WEDDING:'💍',BIRTHDAY:'🎂',BABY_SHOWER:'🧸',HOUSEWARMING:'🏡',ANNIVERSARY:'🥂',OTHER:'✨'};
const themes:Record<string,string>={WEDDING:'wedding',BIRTHDAY:'birthday',BABY_SHOWER:'baby',HOUSEWARMING:'home',ANNIVERSARY:'anniversary',OTHER:'other'};
function ClassicPublicEventPage(){
  const params=useParams(); const token=String(params.token??'');
  const [progressState,setProgressState]=useState<'loading'|'ready'|'error'>('loading');
  const [sortBy,setSortBy]=useState<'priority'|'amount-asc'|'amount-desc'|'percentage-desc'|'percentage-asc'>('priority');const [event,setEvent]=useState<Event|null>(null); const [progress,setProgress]=useState<Record<string,Progress>>({}); const [selectedGift,setSelectedGift]=useState<Gift|null>(null); const [walletOpen,setWalletOpen]=useState(false); const [state,setState]=useState<'loading'|'ready'|'not-found'|'error'>('loading');
  useEffect(()=>{const ready=startScreenTiming('/e/'+token);let active=true;const controller=new AbortController();
    if(!/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(token)){setState('not-found');return;}
    fetch(`/backend/api/public/events/${encodeURIComponent(token)}`,{cache:'no-store',signal:controller.signal})
      .then(async r=>{if(r.status===404){if(active)setState('not-found');return;}if(!r.ok)throw Error('Unable to load event');const data=await r.json();if(active){setEvent(data);setState('ready');ready();}})
      .catch(()=>{if(active)setState('error');});
    return()=>{active=false;controller.abort()};
  },[token]);
  const refreshProgress=useCallback(async(signal?:AbortSignal)=>{
    setProgressState('loading');
    try{
      const response=await fetch(`/backend/api/public/gifts/progress/${encodeURIComponent(token)}`,{cache:'no-store',signal});
      if(!response.ok)throw Error('Unable to retrieve contribution details');
      const rows:Progress[]=await response.json();
      if(!Array.isArray(rows)||rows.some(p=>!p||typeof p.giftId!=='string'||!Number.isSafeInteger(p.contributedPaise)||p.contributedPaise<0||!Number.isSafeInteger(p.contributionCount)||p.contributionCount<0))throw Error('Invalid contribution details');
      if(!signal?.aborted){setProgress(Object.fromEntries(rows.map(p=>[p.giftId,p])));setProgressState('ready');}
    }catch{if(!signal?.aborted)setProgressState('error');}
  },[token]);
  useEffect(()=>{const controller=new AbortController();void refreshProgress(controller.signal);return()=>controller.abort()},[refreshProgress]);
  useEffect(()=>{const onKey=(e:KeyboardEvent)=>{if(e.key==='Escape'){setSelectedGift(null);setWalletOpen(false)}};window.addEventListener('keydown',onKey);return()=>window.removeEventListener('keydown',onKey)},[]);
  const progressValue=(value:string|number)=>progressState==='ready'?value:progressState==='loading'?'Loading…':'Unavailable';
  const rupees=(v:number)=>new Intl.NumberFormat('en-IN',{style:'currency',currency:'INR',maximumFractionDigits:2}).format(v);
  if(state==='loading')return <main className="publicEventMessage">Preparing your celebration…</main>;
  if(state==='not-found')return <main className="publicEventMessage"><h1>Invitation unavailable</h1><p>This event may not be published yet.</p></main>;
  if(state==='error'||!event)return <main className="publicEventMessage"><h1>Could not load this invitation</h1><p>Please try again later.</p></main>;
  const theme=themes[event.occasionType]||'other';
  return <main className={`publicEvent ${theme}`}><div className="publicEventInner">
    <header className="publicEventHeader"><Link href="/">✦ Gift Platform</Link><span>YOU'RE INVITED</span></header>
    <section className="publicEventHero"><div className="publicEventSymbol" aria-hidden="true">{icon[event.occasionType]||'✨'}</div><p className="publicEventEyebrow">A MOMENT TO REMEMBER</p><h1>{event.title}</h1>
      {event.eventDate&&<p className="publicEventDate">{new Date(`${event.eventDate}T12:00:00Z`).toLocaleDateString('en-IN',{day:'numeric',month:'long',year:'numeric',timeZone:'UTC'})}</p>}
      {event.description&&<p className="publicEventDescription">{event.description}</p>}
      <a href="#wishlist" className="publicEventPrimary">Explore the wishlist ↓</a><GuestActions token={token} title={event.title} date={event.eventDate} features={event.features} onContribute={()=>setWalletOpen(true)}/>
    </section>
    <section id="wishlist" className="publicEventWishlist"><p className="publicEventEyebrow">GIFTS WITH MEANING</p><h2>Celebrate with a thoughtful gift</h2>{progressState==='error'&&<p role="alert">Contribution progress is unavailable. <button type="button" onClick={()=>void refreshProgress()}>Retry</button></p>}
      {event.gifts.length>0&&<label className="giftSortControl" style={{display:'flex',alignItems:'center',gap:12,justifyContent:'flex-end',marginBottom:18}}>Sort gifts <select aria-label="Sort gifts" value={sortBy} onChange={e=>setSortBy(e.target.value as typeof sortBy)}><option value="priority">Host priority</option><option value="amount-asc">Amount: Low to High</option><option value="amount-desc">Amount: High to Low</option><option value="percentage-desc">Gifted: Highest % First</option><option value="percentage-asc">Gifted: Lowest % First</option></select></label>}
      {event.gifts.length===0?<p className="publicEventEmpty">The host hasn't published any gifts yet. Check back soon!</p>:
      <div className="publicEventGiftGrid">{sortWishlist(event.gifts,sortBy,progress).map(g=>{
        const total=Math.max(0,Math.round(g.targetAmount*100));const given=Math.max(0,progress[g.id]?.contributedPaise??0);
        const remaining=Math.max(0,total-given);const percent=total>0?Math.min(100,Math.round(given/total*100)):0;
        return <article className="publicEventGift loveGiftCard" key={g.id}>
          <div className="publicEventGiftSymbol" aria-hidden="true">🎁</div><span className="loveGiftPercent">{progressState==='ready'?`${percent}% Gifted`:progressValue('')}</span>
          <h3>{g.name}</h3>{g.imageUrl&&<img src={g.imageUrl} alt={g.name} loading="lazy" style={{width:140,height:140,objectFit:'contain'}}/>}<GiftDescription name={g.name} description={g.description}/>
          <div className="loveGiftMetrics"><div><small>Gift Value</small><b>{rupees(total/100)}</b></div><div><small>Contributions Received</small><b>{progressValue(rupees(given/100))}</b></div><div><small>Still to Be Gifted</small><b>{progressValue(rupees(remaining/100))}</b></div><div><small>Contributions Made</small><b>{progressValue(progress[g.id]?.contributionCount??0)}</b></div></div>
          {progressState==='ready'&&<div className="loveGiftTrack" role="progressbar" aria-label={`${g.name} gifted`} aria-valuemin={0} aria-valuemax={100} aria-valuenow={percent}><div style={{width:`${percent}%`}}/></div>}
          {progressState==='ready'&&remaining===0?<div className="loveGiftComplete">❤️ This Gift Is Complete with Love!<small>Thank you to everyone who made this gift special.</small></div>:
            <button type="button" className="loveGiftButton" onClick={()=>setSelectedGift(g)}>♡ Contribute with Love</button>}
        </article>
      })}</div>}
    </section><section className="loveWalletSection"><h2>Send Love to the Celebration</h2><p>Prefer to share a gift for the celebration as a whole?</p><button type="button" className="loveGiftButton" onClick={()=>setWalletOpen(true)}>♡ Contribute to the Celebration</button></section>
    {(selectedGift||walletOpen)&&<div className="loveModalBackdrop" role="presentation" onMouseDown={e=>{if(e.target===e.currentTarget){setSelectedGift(null);setWalletOpen(false)}}}><div className="loveModal" role="dialog" aria-modal="true" aria-label={selectedGift?`Contribute to ${selectedGift.name}`:'Contribute to the celebration'}>
      {selectedGift&&<div className="loveModalGift"><h2>{selectedGift.name}</h2><p>{selectedGift.description}</p><div className="loveModalMetrics"><span>Gift Value: {rupees(selectedGift.targetAmount)}</span><span>Contributions Received: {progressValue(rupees((progress[selectedGift.id]?.contributedPaise??0)/100))}</span><span>Still to Be Gifted: {progressValue(rupees(Math.max(0,selectedGift.targetAmount-(progress[selectedGift.id]?.contributedPaise??0)/100)))}</span><span>Contributions Made: {progressValue(progress[selectedGift.id]?.contributionCount??0)}</span></div></div>}
      <GuestContributionCheckout key={selectedGift?.id??'wallet'} token={token} gifts={selectedGift?[selectedGift]:[]} selectedGiftId={selectedGift?.id??''} onClose={()=>{setSelectedGift(null);setWalletOpen(false);void refreshProgress();}}/>
    </div></div>}
    <GuestCelebration token={token} title={event.title} features={event.features}/>
    <footer className="publicEventFooter">Made for meaningful celebrations ✦ Gift Platform</footer>
  </div></main>;
}

export default function PublicEventPage(){const {mode}=useExperience();return mode==='FOCUSED'?<FocusedInvitation/>:<ClassicPublicEventPage/>;}
