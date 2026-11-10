'use client';
/* localization:instrumented */
import {localize} from '../lib/localization.mjs';
import {useLocalizationRender} from './LocalizationProvider';

import {usePathname} from 'next/navigation';
import {createContext,useContext,useEffect,useState,type ReactNode} from 'react';
export type ViewMode='CLASSIC'|'FOCUSED';
const ExperienceContext=createContext<{mode:ViewMode;apply:(mode:ViewMode)=>void}>({mode:'CLASSIC',apply:()=>{}});
export const useExperience=()=>useContext(ExperienceContext);
export default function ExperienceProvider({children}:{children:ReactNode}){useLocalizationRender();
 const path=usePathname();const discovery=['/','/demo','/privacy','/terms','/contact'].includes(path)||path.startsWith('/memories/');
 const [mode,setMode]=useState<ViewMode>('CLASSIC'),[ready,setReady]=useState(false);
 useEffect(()=>{let active=true;const controller=new AbortController();const timeout=setTimeout(()=>controller.abort(),5000);
  fetch('/backend/api/public/experience',{cache:'no-store',credentials:'same-origin',signal:controller.signal})
   .then(async r=>{if(!r.ok)throw Error('Unavailable');const value=await r.json();if(!active)return;setMode(value.viewMode==='FOCUSED'?'FOCUSED':'CLASSIC')})
   .catch(()=>{}).finally(()=>{clearTimeout(timeout);if(active)setReady(true)});return()=>{active=false;clearTimeout(timeout);controller.abort()};
 },[]);
 return <ExperienceContext.Provider value={{mode,apply:setMode}}><div className={mode==='FOCUSED'?'ux-focused':'ux-classic'} data-experience={mode}>{ready||discovery?children:<main className="experienceLoading" role="status">{localize("ui.7a005f0f1dbc704f",{defaultValue:"Opening your celebration space…"})}</main>}</div></ExperienceContext.Provider>;
}
