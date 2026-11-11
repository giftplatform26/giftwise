'use client';
import {useEffect,useState} from 'react';
import {useParams} from 'next/navigation';
import Link from 'next/link';
import {localizeText} from '../../../lib/localization.mjs';
import {useLocalizationRender} from '../../../components/LocalizationProvider';
import {vendorLinks} from '../../../lib/vendors.mjs';
import {ShowcaseCard,type Card} from '../../../components/Showcase';
import '../../../components/acquisition.css';

type Partner={businessName:string;kind:string;city:string|null;phone:string|null;website:string|null;brandColor:string;bio:string|null;hasLogo:boolean;celebrations:Card[]};
const KIND:Record<string,string>={PLANNER:'Event planner',VENUE:'Venue',DECORATOR:'Decorator',CATERER:'Caterer',PHOTOGRAPHER:'Photographer',OTHER:'Celebration partner'};

/** Public page for a planner or venue partner, with the celebrations they've shared in the showcase. */
export default function PartnerPage(){useLocalizationRender();
 const {slug}=useParams();const s=String(slug??'');
 const [p,setP]=useState<Partner|null>(null),[gone,setGone]=useState(false);
 useEffect(()=>{if(!/^[a-z0-9-]{3,40}$/.test(s)){setGone(true);return}fetch(`/backend/api/public/partners/${s}`,{cache:'no-store'}).then(r=>{if(!r.ok){setGone(true);return null}return r.json()}).then(v=>v&&setP(v)).catch(()=>setGone(true))},[s]);
 if(gone)return <main className="ppPage"><h1>{localizeText('This page is not available')}</h1><Link href="/">{localizeText('Go to GiftWise')}</Link></main>;
 if(!p)return <main className="ppPage"><p role="status">{localizeText('Loading…')}</p></main>;
 const links=vendorLinks(p);const color=/^#[0-9A-Fa-f]{6}$/.test(p.brandColor)?p.brandColor:'#683c55';
 return <main className="ppPage" style={{'--pp-accent':color} as React.CSSProperties}>
  <header className="ppHero">{p.hasLogo?<img src={`/backend/api/public/partners/${s}/logo.png`} alt="" width={96} height={96}/>:<span className="ptMono" style={{background:color}}>{p.businessName.slice(0,1)}</span>}
   <p className="focusEyebrow">{localizeText(KIND[p.kind]??'Celebration partner')}{p.city?` · ${p.city}`:''}</p><h1>{p.businessName}</h1>{p.bio&&<p className="ppBio">{p.bio}</p>}
   <nav className="vdLinks">{links.whatsapp&&<a className="focusPrimary" href={links.whatsapp} target="_blank" rel="noopener noreferrer">{localizeText('WhatsApp')}</a>}{links.call&&<a href={links.call}>{localizeText('Call')}</a>}{links.website&&<a href={links.website} target="_blank" rel="noopener noreferrer nofollow">{localizeText('Website')}</a>}</nav></header>
  {p.celebrations.length>0&&<section><h2>{localizeText('Celebrations we’ve planned')}</h2><div className="shGrid">{p.celebrations.map(c=><ShowcaseCard key={c.eventId} card={c}/>)}</div></section>}
  <footer className="ppFoot"><Link href="/">{localizeText('Made with GiftWise — create your own invitation')}</Link></footer>
 </main>;
}
