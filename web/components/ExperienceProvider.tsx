'use client';
import {createContext,useContext,useEffect,useState,type ReactNode} from 'react';
export type ViewMode='CLASSIC'|'FOCUSED';
const ExperienceContext=createContext<{mode:ViewMode;apply:(mode:ViewMode)=>void}>({mode:'CLASSIC',apply:()=>{}});
export const useExperience=()=>useContext(ExperienceContext);
export default function ExperienceProvider({children}:{children:ReactNode}){
 const [mode,setMode]=useState<ViewMode>('CLASSIC'),[ready,setReady]=useState(false);
 useEffect(()=>{let active=true;const controller=new AbortController();const timeout=setTimeout(()=>controller.abort(),5000);
  fetch('/backend/api/public/experience',{cache:'no-store',credentials:'same-origin',signal:controller.signal})
   .then(async r=>{if(!r.ok)throw Error('Unavailable');const value=await r.json();if(!active)return;setMode(value.viewMode==='FOCUSED'?'FOCUSED':'CLASSIC')})
   .catch(()=>{}).finally(()=>{clearTimeout(timeout);if(active)setReady(true)});return()=>{active=false;clearTimeout(timeout);controller.abort()};
 },[]);
 return <ExperienceContext.Provider value={{mode,apply:setMode}}><div className={mode==='FOCUSED'?'ux-focused':'ux-classic'} data-experience={mode}>{ready?children:<main className="experienceLoading" role="status">Opening your celebration space…</main>}</div></ExperienceContext.Provider>;
}
