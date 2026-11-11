'use client';
import {useEffect,useState} from 'react';
import Link from 'next/link';
import {localizeText} from '../lib/localization.mjs';
import {useLocalizationRender} from './LocalizationProvider';
import {occasionArt} from '../lib/focused.mjs';
import './acquisition.css';

export type Card={eventId:string;displayTitle:string;blurb:string|null;city:string|null;occasion:string;monthYear:string|null;wishes:{id:string;guestName:string;message:string}[];photoUrl:string|null;partnerName:string|null;partnerSlug:string|null};
const safePhoto=(u:string|null)=>u&&/^\/backend\/api\/public\/celebrations\/[0-9a-f-]{36}\/wishes\/[0-9a-f-]{36}\/photo$/i.test(u)?u:null;

export function ShowcaseCard({card}:{card:Card}){useLocalizationRender();
 const photo=safePhoto(card.photoUrl);const wish=card.wishes[0];
 return <article className="shCard"><div className="shArt" style={{backgroundImage:`url(${photo??`/invitation-backgrounds/${occasionArt(card.occasion)}.svg`})`}}/>
  <div className="shBody"><p className="focusEyebrow">{[card.monthYear,card.city].filter(Boolean).join(' · ')}</p><h3>{card.displayTitle}</h3>{card.blurb&&<p>{card.blurb}</p>}
   {wish&&<blockquote>“{wish.message}” <cite>— {wish.guestName}</cite></blockquote>}
   {card.partnerName&&card.partnerSlug&&<p className="shPartner">{localizeText('Planned by')} <Link href={`/p/${card.partnerSlug}`}>{card.partnerName}</Link></p>}</div></article>;
}

/** Approved real celebrations. Renders nothing when the showcase is off or empty. */
export function ShowcaseStrip({limit=3,title=true}:{limit?:number;title?:boolean}){useLocalizationRender();
 const [cards,setCards]=useState<Card[]>([]);
 useEffect(()=>{fetch(`/backend/api/public/showcase?limit=${limit}`,{cache:'no-store'}).then(r=>r.ok?r.json():[]).then(v=>Array.isArray(v)&&setCards(v)).catch(()=>{})},[limit]);
 if(cards.length===0)return null;
 return <section className="shStrip">{title&&<div className="shHead"><h2>{localizeText('Real celebrations on GiftWise')}</h2><Link href="/showcase">{localizeText('See more')} →</Link></div>}
  <div className="shGrid">{cards.map(c=><ShowcaseCard key={c.eventId} card={c}/>)}</div></section>;
}
