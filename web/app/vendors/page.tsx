'use client';
import {useCallback,useEffect,useState} from 'react';
import {localizeText} from '../../lib/localization.mjs';
import {useLocalizationRender} from '../../components/LocalizationProvider';
import {hostRequest} from '../../lib/host-api.mjs';
import {VENDOR_CATEGORIES,vendorLinks} from '../../lib/vendors.mjs';
import '../../components/gift-books.css';
import '../../components/growth-pages.css';

type Vendor={id:string;name:string;category:string;city:string;description:string|null;phone:string|null;website:string|null;instagram:string|null;priceHint:string|null;verified:boolean;shortlisted:boolean};
type Directory={vendors:Vendor[];cities:string[]};

export default function Vendors(){useLocalizationRender();
 const [d,setD]=useState<Directory|null>(null),[city,setCity]=useState(''),[category,setCategory]=useState(''),[mine,setMine]=useState(false),[error,setError]=useState('');
 const load=useCallback(async()=>{const q=new URLSearchParams({city,category,shortlist:String(mine)});try{setD(await hostRequest(fetch,`/v1/me/vendors?${q}`) as Directory);setError('')}catch(e){setError(e instanceof Error?e.message:'Could not load')}},[city,category,mine]);
 useEffect(()=>{void load()},[load]);
 async function toggle(v:Vendor){try{await hostRequest(fetch,`/v1/me/vendors/${v.id}/${v.shortlisted?'unshortlist':'shortlist'}`,{method:'POST'});setD(x=>x&&{...x,vendors:mine&&v.shortlisted?x.vendors.filter(y=>y.id!==v.id):x.vendors.map(y=>y.id===v.id?{...y,shortlisted:!y.shortlisted}:y)})}catch(e){setError(e instanceof Error?e.message:'Could not save')}}
 return <main className="focusPage gbPage">
  <header className="focusPageHeading"><div><p className="focusEyebrow">{localizeText('Trusted help near you')}</p><h1>{localizeText('Vendors')}</h1>
   <p>{localizeText('Decorators, caterers, photographers and more, picked by the GiftWise team. Contact them directly — GiftWise takes no booking fee.')}</p></div></header>
  {error&&<p className="focusError" role="alert">{localizeText(error)}</p>}
  <div className="vdFilters"><label>{localizeText('City')}<select value={city} onChange={e=>setCity(e.target.value)}><option value="">{localizeText('All cities')}</option>{d?.cities.map(c=><option key={c} value={c}>{c}</option>)}</select></label>
   <div className="gbChips" role="group" aria-label={localizeText('Category')}><button type="button" aria-pressed={category===''} onClick={()=>setCategory('')}>{localizeText('All')}</button>{VENDOR_CATEGORIES.map(([k,l]:[string,string])=><button key={k} type="button" aria-pressed={category===k} onClick={()=>setCategory(k)}>{localizeText(l)}</button>)}</div>
   <label className="gbCheck"><input type="checkbox" checked={mine} onChange={e=>setMine(e.target.checked)}/>{localizeText('My shortlist')}</label></div>
  {d&&(d.vendors.length===0?<p className="gbEmpty">{localizeText(mine?'Tap ♡ on a vendor to save them here.':'No vendors here yet. We are adding more every week.')}</p>:
  <div className="vdGrid">{d.vendors.map(v=>{const links=vendorLinks(v);return <article key={v.id} className="vdCard">
   <header><p className="focusEyebrow">{localizeText(VENDOR_CATEGORIES.find(([k]:[string,string])=>k===v.category)?.[1]??v.category)} · {v.city}</p>
    <button type="button" className={`vdHeart${v.shortlisted?' is-on':''}`} aria-pressed={v.shortlisted} aria-label={localizeText(v.shortlisted?'Remove from shortlist':'Save to shortlist')} onClick={()=>void toggle(v)}>{v.shortlisted?'♥':'♡'}</button></header>
   <h2>{v.name}{v.verified&&<span className="vdVerified" title={localizeText('Checked by GiftWise')}>✓</span>}</h2>
   {v.description&&<p>{v.description}</p>}{v.priceHint&&<p className="vdPrice">{v.priceHint}</p>}
   <nav className="vdLinks">{links.whatsapp&&<a href={links.whatsapp} target="_blank" rel="noopener noreferrer">{localizeText('WhatsApp')}</a>}{links.call&&<a href={links.call}>{localizeText('Call')}</a>}
    {links.website&&<a href={links.website} target="_blank" rel="noopener noreferrer nofollow">{localizeText('Website')}</a>}{links.instagram&&<a href={links.instagram} target="_blank" rel="noopener noreferrer nofollow">Instagram</a>}</nav>
  </article>})}</div>)}
 </main>;
}
