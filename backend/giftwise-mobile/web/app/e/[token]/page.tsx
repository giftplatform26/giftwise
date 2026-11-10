'use client';
import {activeLocale} from '../../../lib/localization.mjs';

/* localization:instrumented */
import {localize} from '../../../lib/localization.mjs';
import {useLocalizationRender} from '../../../components/LocalizationProvider';

import CelebrationDetails from '../../../components/CelebrationDetails';
import GroupContribution from '../../../components/GroupContribution';
import GuestGiftBoard from '../../../components/GuestGiftBoard';
import GiftDescription from '../../../components/GiftDescription';
import CommercialPlacements from '../../../components/CommercialPlacements';
import dynamic from 'next/dynamic';
import {useExperience} from '../../../components/ExperienceProvider';
const FocusedInvitation=dynamic(()=>import('../../../components/focused/Invitation'),{loading:()=> <main className="focusPage" role="status">{localize("ui.c926c2c50e65d5a5",{defaultValue:"Opening…"})}</main>});
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

type Gift={contributionEnabled?:boolean;contributionDisabledReason?:string;id:string;name:string;description:string|null;targetAmount:number;productUrl?:string|null;imageUrl?:string|null;priority?:'HIGH'|'MEDIUM'|'LOW'};
type Progress={giftId:string;contributedPaise:number;contributionCount:number};
type Event={title:string;description:string|null;occasionType:string;eventDate:string|null;gifts:Gift[];features?:EventFeatures};
const icon:Record<string,string>={WEDDING:'💍',BIRTHDAY:'🎂',BABY_SHOWER:'🧸',HOUSEWARMING:'🏡',ANNIVERSARY:'🥂',FAREWELL:'💐',RETIREMENT:'🌅',REUNION:'🤝',OTHER:'✨'};
const themes:Record<string,string>={WEDDING:'wedding',BIRTHDAY:'birthday',BABY_SHOWER:'baby',HOUSEWARMING:'home',ANNIVERSARY:'anniversary',OTHER:'other'};
function ClassicPublicEventPage(){useLocalizationRender();
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
  useEffect(()=>{const updated=(event:globalThis.Event)=>{const detail=(event as CustomEvent<{token:string;gifts:Gift[]}>).detail;if(detail?.token===token)setEvent(previous=>previous?{...previous,gifts:detail.gifts}:previous)};window.addEventListener('gift-group-updated',updated);return()=>window.removeEventListener('gift-group-updated',updated)},[token]);
  const progressValue=(value:string|number)=>progressState==='ready'?value:progressState==='loading'?'Loading…':'Unavailable';
  const rupees=(v:number)=>new Intl.NumberFormat(activeLocale("en-IN"),{style:'currency',currency:'INR',maximumFractionDigits:2}).format(v);
  if(state==='loading')return <main className="publicEventMessage">{localize("ui.8fe30ad66c95faea",{defaultValue:"Preparing your celebration…"})}</main>;
  if(state==='not-found')return <main className="publicEventMessage"><h1>{localize("ui.4ef9de8a0823545a",{defaultValue:"Invitation unavailable"})}</h1><p>{localize("ui.d94203469571bce9",{defaultValue:"This event may not be published yet."})}</p></main>;
  if(state==='error'||!event)return <main className="publicEventMessage"><h1>{localize("ui.059a0e9470c79bc9",{defaultValue:"Could not load this invitation"})}</h1><p>{localize("ui.386b9144ee224202",{defaultValue:"Please try again later."})}</p></main>;
  const theme=themes[event.occasionType]||'other';
  return <main className={`publicEvent ${theme}`}><div className="publicEventInner">
    <header className="publicEventHeader"><Link href="/">{localize("ui.775be12c6f4bb051",{defaultValue:"✦ GiftWise"})}</Link><span>{localize("ui.db0833ff8adc9c25",{defaultValue:"YOU'RE INVITED"})}</span></header>
    <section className="publicEventHero"><div className="publicEventSymbol" aria-hidden="true">{icon[event.occasionType]||'✨'}</div><p className="publicEventEyebrow">{localize("ui.a1396525daa7eaef",{defaultValue:"A MOMENT TO REMEMBER"})}</p><h1>{event.title}</h1>
      {event.eventDate&&<p className="publicEventDate">{new Date(`${event.eventDate}T12:00:00Z`).toLocaleDateString(activeLocale("en-IN"),{day:'numeric',month:'long',year:'numeric',timeZone:'UTC'})}</p>}
      {event.description&&<p className="publicEventDescription">{event.description}</p>}
      {event.features?.GIFTING_ENABLED!==false&&<a href="#wishlist" className="publicEventPrimary">{localize("ui.4699fd0a61bd3592",{defaultValue:"Explore the wishlist ↓"})}</a>}<GuestActions token={token} title={event.title} date={event.eventDate} features={event.features} onContribute={()=>setWalletOpen(true)}/>
    </section>
    <CelebrationDetails token={token}/>{event.features?.GIFTING_ENABLED!==false&&<section id="wishlist" className="publicEventWishlist"><p className="publicEventEyebrow">{localize("ui.9f779e9e5a5ebeb2",{defaultValue:"GIFTS WITH MEANING"})}</p><h2>{localize("ui.b9d972fa229ed76a",{defaultValue:"Celebrate with a thoughtful gift"})}</h2>{progressState==='error'&&<p role="alert">{localize("ui.14578d88ad5db954",{defaultValue:"Contribution progress is unavailable. "})}<button type="button" onClick={()=>void refreshProgress()}>{localize("ui.942087cc2d41e013",{defaultValue:"Retry"})}</button></p>}
      {event.gifts.length>0&&<label className="giftSortControl" style={{display:'flex',alignItems:'center',gap:12,justifyContent:'flex-end',marginBottom:18}}>{localize("ui.8998fc10fea0ba1e",{defaultValue:"Sort gifts "})}<select aria-label={localize("ui.a95dfa33544070f8",{defaultValue:"Sort gifts"})} value={sortBy} onChange={e=>setSortBy(e.target.value as typeof sortBy)}><option value="priority">{localize("ui.7b765ed03a7ecd5f",{defaultValue:"Host priority"})}</option><option value="amount-asc">{localize("ui.41186b6b15f2d652",{defaultValue:"Amount: Low to High"})}</option><option value="amount-desc">{localize("ui.c92f983d91ed1b2f",{defaultValue:"Amount: High to Low"})}</option><option value="percentage-desc">{localize("ui.7de668954d75a908",{defaultValue:"Gifted: Highest % First"})}</option><option value="percentage-asc">{localize("ui.0488a0a9924efcd4",{defaultValue:"Gifted: Lowest % First"})}</option></select></label>}
      {event.gifts.length===0?<p className="publicEventEmpty">{localize("ui.6921773e206ede9f",{defaultValue:"The host hasn't published any gifts yet. Check back soon!"})}</p>:
      <div className="publicEventGiftGrid">{sortWishlist(event.gifts,sortBy,progress).map(g=>{
        const total=Math.max(0,Math.round(g.targetAmount*100));const given=Math.max(0,progress[g.id]?.contributedPaise??0);
        const remaining=Math.max(0,total-given);const percent=total>0?Math.min(100,Math.round(given/total*100)):0;
        return <article className="publicEventGift loveGiftCard" key={g.id}>
          <div className="publicEventGiftSymbol" aria-hidden="true">🎁</div><span className="loveGiftPercent">{progressState==='ready'?localize("ui.edaf9278ab7e888f",{defaultValue:"{{value0}}% Gifted",value0:(percent)}):progressValue('')}</span>
          <h3>{g.name}</h3>{g.productUrl&&<a href={g.productUrl} target="_blank" rel="sponsored noopener noreferrer">{localize("ui.4327275910115c20",{defaultValue:"View retailer product · may earn a commission"})}</a>}{g.imageUrl&&<img src={g.imageUrl} alt={g.name} loading="lazy" style={{width:140,height:140,objectFit:'contain'}}/>}<GiftDescription name={g.name} description={g.description}/>
          <div className="loveGiftMetrics"><div><small>{localize("ui.89e68422c2ed7453",{defaultValue:"Gift Value"})}</small><b>{rupees(total/100)}</b></div><div><small>{localize("ui.26fa7036954b8a10",{defaultValue:"Contributions Received"})}</small><b>{progressValue(rupees(given/100))}</b></div><div><small>{localize("ui.b264258757305697",{defaultValue:"Still to Be Gifted"})}</small><b>{progressValue(rupees(remaining/100))}</b></div><div><small>{localize("ui.ddc27baf56341e70",{defaultValue:"Contributions Made"})}</small><b>{progressValue(progress[g.id]?.contributionCount??0)}</b></div></div>
          {progressState==='ready'&&<div className="loveGiftTrack" role="progressbar" aria-label={localize("ui.940c60082fd131b7",{defaultValue:"{{value0}} gifted",value0:(g.name)})} aria-valuemin={0} aria-valuemax={100} aria-valuenow={percent}><div style={{width:`${percent}%`}}/></div>}
          {progressState==='ready'&&remaining===0?<div className="loveGiftComplete">{localize("ui.6927b6dde3b35bbf",{defaultValue:"❤️ This Gift Is Complete with Love!"})}<small>{localize("ui.cce0c4a106f74178",{defaultValue:"Thank you to everyone who made this gift special."})}</small></div>:
            <button type="button" className="loveGiftButton" disabled={g.contributionEnabled===false} title={g.contributionDisabledReason} onClick={()=>setSelectedGift(g)}>{g.contributionEnabled===false?g.contributionDisabledReason:localize("ui.b90c7ad69079591b",{defaultValue:"♡ Contribute with Love"})}</button>}
          {event.features?.GROUP_GIFTING&&<GroupContribution token={token} initialGiftId={g.id} giftAvailable={g.contributionEnabled!==false} compact/>}
        </article>
      })}</div>}
    </section>}{event.features?.GROUP_GIFTING&&event.features?.GUEST_GIFTS&&<GuestGiftBoard token={token}/>}{event.features?.GIFTING_ENABLED!==false&&<section className="loveWalletSection"><h2>{localize("ui.9fc1f7d95aa985c3",{defaultValue:"Send Love to the Celebration"})}</h2><p>{localize("ui.3dc0a2d1a7c7793e",{defaultValue:"Prefer to share a gift for the celebration as a whole?"})}</p><button type="button" className="loveGiftButton" onClick={()=>setWalletOpen(true)}>{localize("ui.76c32149f8bae891",{defaultValue:"♡ Contribute to the Celebration"})}</button></section>}
    {(selectedGift||walletOpen)&&<div className="loveModalBackdrop" role="presentation" onMouseDown={e=>{if(e.target===e.currentTarget){setSelectedGift(null);setWalletOpen(false)}}}><div className="loveModal" role="dialog" aria-modal="true" aria-label={selectedGift?localize("ui.c7f13b55ca3201ed",{defaultValue:"Contribute to {{value0}}",value0:(selectedGift.name)}):localize("ui.9f22882fb715cb61",{defaultValue:"Contribute to the celebration"})}>
      {selectedGift&&<div className="loveModalGift"><h2>{selectedGift.name}</h2><p>{selectedGift.description}</p><div className="loveModalMetrics"><span>{localize("ui.8c87fd59d8a1da53",{defaultValue:"Gift Value: "})}{rupees(selectedGift.targetAmount)}</span><span>{localize("ui.ca343b5a42406c14",{defaultValue:"Contributions Received: "})}{progressValue(rupees((progress[selectedGift.id]?.contributedPaise??0)/100))}</span><span>{localize("ui.a471bb9c4511213c",{defaultValue:"Still to Be Gifted: "})}{progressValue(rupees(Math.max(0,selectedGift.targetAmount-(progress[selectedGift.id]?.contributedPaise??0)/100)))}</span><span>{localize("ui.06083acc7cce254b",{defaultValue:"Contributions Made: "})}{progressValue(progress[selectedGift.id]?.contributionCount??0)}</span></div></div>}
      <GuestContributionCheckout key={selectedGift?.id??'wallet'} token={token} gifts={selectedGift?[selectedGift]:[]} selectedGiftId={selectedGift?.id??''} onClose={()=>{setSelectedGift(null);setWalletOpen(false);void refreshProgress();}}/>
    </div></div>}
    <CommercialPlacements occasion={event.occasionType}/><CommercialPlacements stream="VENDOR_LISTING" occasion={event.occasionType}/><GuestCelebration token={token} title={event.title} features={event.features}/>
    <footer className="publicEventFooter">{localize("ui.54532edf07c01ebd",{defaultValue:"Made for meaningful celebrations ✦ GiftWise"})}</footer>
  </div></main>;
}

export default function PublicEventPage(){useLocalizationRender();const {mode}=useExperience();return mode==='FOCUSED'?<FocusedInvitation/>:<ClassicPublicEventPage/>;}
