'use client';
import {useEffect,useId,useRef,useState,type ReactNode} from 'react';
import {BrandLogo} from '../BrandingProvider';
import {hostApi} from '../../lib/host-api.mjs';
import {localizeText} from '../../lib/localization.mjs';
import {useLocalizationRender} from '../LocalizationProvider';
import {ADMIN_GROUPS,ADMIN_SECTIONS,adminSection,findSettings,sectionHref} from '../../lib/admin-sections.mjs';

type SearchEntry={label:string;section:string;keywords?:string;query?:string};

/** Settings search: type a plain-language name ("razorpay", "otp", "seating") and jump straight to the control. */
function SettingSearch({extra,onPick}:{extra:SearchEntry[];onPick:()=>void}){
 const [query,setQuery]=useState(''),[active,setActive]=useState(0);
 const listId=useId();
 const results=findSettings(query,extra) as SearchEntry[];
 function go(entry:SearchEntry){window.location.assign(sectionHref(entry.section,entry.query||''));setQuery('');onPick();}
 return <div className="admSearch" role="search">
  <label htmlFor={listId+'-input'}>{localizeText('Find a setting')}</label>
  <input id={listId+'-input'} type="search" autoComplete="off" placeholder={localizeText('e.g. Razorpay, OTP, seating')} value={query}
   role="combobox" aria-expanded={results.length>0} aria-controls={listId} aria-autocomplete="list" aria-activedescendant={results.length?`${listId}-${active}`:undefined}
   onChange={e=>{setQuery(e.target.value);setActive(0);}}
   onKeyDown={e=>{if(e.key==='ArrowDown'&&results.length){e.preventDefault();setActive(i=>(i+1)%results.length);}
    else if(e.key==='ArrowUp'&&results.length){e.preventDefault();setActive(i=>(i-1+results.length)%results.length);}
    else if(e.key==='Enter'&&results[active]){e.preventDefault();go(results[active]);}
    else if(e.key==='Escape'){setQuery('');}}}/>
  {query.trim().length>=2&&<ul id={listId} role="listbox" aria-label={localizeText('Matching settings')}>
   {results.map((r,i)=><li key={r.section+r.label} id={`${listId}-${i}`} role="option" aria-selected={i===active} onMouseDown={e=>{e.preventDefault();go(r);}} onMouseEnter={()=>setActive(i)}>
    <span>{localizeText(r.label)}</span><small>{localizeText(adminSection(r.section).label)}</small></li>)}
   {!results.length&&<li className="admSearchNone" role="option" aria-selected={false} aria-disabled="true">{localizeText('No matching settings. Try another word.')}</li>}
  </ul>}
 </div>;
}

export default function AdminShell({current,title,description,actions,children,searchExtra=[]}:{current:string;title?:string;description?:string;actions?:ReactNode;children:ReactNode;searchExtra?:SearchEntry[]}){
 useLocalizationRender();
 const [menuOpen,setMenuOpen]=useState(false),[signOutError,setSignOutError]=useState('');
 const headingRef=useRef<HTMLHeadingElement>(null),firstRender=useRef(true);
 const section=adminSection(current);
 useEffect(()=>{setMenuOpen(false);if(firstRender.current){firstRender.current=false;return;}window.scrollTo({top:0});headingRef.current?.focus();},[current]);
 useEffect(()=>{if(!menuOpen)return;const close=(e:KeyboardEvent)=>{if(e.key==='Escape')setMenuOpen(false);};window.addEventListener('keydown',close);return()=>window.removeEventListener('keydown',close);},[menuOpen]);
 async function signout(){try{await hostApi.logout(fetch);window.location.assign('/login');}catch(e){setSignOutError(e instanceof Error?e.message:String(e));}}
 const groupLabel=ADMIN_GROUPS.find(g=>g.id===section.group)?.label;
 return <div className="adm">
  <a className="admSkip" href="#admin-content">{localizeText('Skip to content')}</a>
  <div className="admTopbar">
   <a className="admBrand" href="/admin"><BrandLogo size={28}/><small>{localizeText('Administration')}</small></a>
   <button type="button" className="admMenuButton" aria-expanded={menuOpen} aria-controls="admin-navigation" onClick={()=>setMenuOpen(v=>!v)}>{localizeText(menuOpen?'Close menu':'Menu')}</button>
  </div>
  <aside id="admin-navigation" className={`admSide${menuOpen?' is-open':''}`}>
   <a className="admBrand admBrandSide" href="/admin"><BrandLogo size={32}/><small>{localizeText('Administration')}</small></a>
   <SettingSearch extra={searchExtra} onPick={()=>setMenuOpen(false)}/>
   <nav aria-label={localizeText('Administration')}>
    {ADMIN_GROUPS.map(group=>{const items=ADMIN_SECTIONS.filter(s=>s.group===group.id);return <div className="admNavGroup" key={group.id}>
     {group.label&&<h2>{localizeText(group.label)}</h2>}
     <ul>{items.map(item=><li key={item.id}><a href={sectionHref(item.id)} aria-current={item.id===current?'page':undefined} onClick={()=>setMenuOpen(false)}>{localizeText(item.label)}{item.href&&<span className="admExternal" aria-hidden="true"> ↗</span>}</a></li>)}</ul>
    </div>;})}
   </nav>
   <p className="admSideNote">{localizeText('Secure platform management')}</p>
  </aside>
  {menuOpen&&<button type="button" className="admScrim" aria-label={localizeText('Close menu')} onClick={()=>setMenuOpen(false)}/>}
  <main className="admMain" id="admin-content">
   <header className="admHeader">
    <div>
     <p className="admEyebrow">{localizeText('Platform administration')}{groupLabel?` · ${localizeText(groupLabel)}`:''}</p>
     <h1 ref={headingRef} tabIndex={-1}>{localizeText(title||section.title)}</h1>
     <p className="admLead">{localizeText(description||section.description)}</p>
    </div>
    <div className="admHeaderActions">{actions}<button type="button" className="admButton" onClick={()=>void signout()}>{localizeText('Sign out')}</button></div>
   </header>
   {signOutError&&<div role="alert" className="acError">{localizeText(signOutError)}</div>}
   {children}
   <footer className="admFooter"><BrandLogo size={24}/> · {localizeText('Administrator workspace · Payment confirmation remains independently verified')}</footer>
  </main>
 </div>;
}
