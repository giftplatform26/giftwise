 'use client';
import {createContext,useContext,useEffect,useState,type ReactNode} from 'react';
export type VerificationSettings={mobileOtpEnabled:boolean;emailVerificationEnabled:boolean};
const disabled:VerificationSettings={mobileOtpEnabled:false,emailVerificationEnabled:false};
const Context=createContext(disabled);
export const useVerification=()=>useContext(Context);
export default function VerificationProvider({children}:{children:ReactNode}){
 const [settings,setSettings]=useState(disabled);
 useEffect(()=>{let active=true;let sequence=0;const controller=new AbortController();
  // Cacheable for 60s: polling/focus use the browser cache; a 'verification-settings-changed' event without data reloads it.
  async function refresh(force=false){const current=++sequence;try{const r=await fetch('/backend/api/public/verification-settings',{credentials:'same-origin',cache:force?'reload':'default',signal:controller.signal});if(!r.ok)return;const x=await r.json();if(active&&current===sequence)setSettings({mobileOtpEnabled:x.mobileOtpEnabled===true,emailVerificationEnabled:x.emailVerificationEnabled===true})}catch{}}
  const changed=(event:globalThis.Event)=>{const data=(event as CustomEvent<VerificationSettings>).detail;if(data){sequence++;setSettings(data)}else void refresh(true)};
  void refresh();const timer=setInterval(()=>{if(!document.hidden)void refresh()},30000);const onFocus=()=>void refresh();window.addEventListener('focus',onFocus);window.addEventListener('verification-settings-changed',changed);
  return()=>{active=false;controller.abort();clearInterval(timer);window.removeEventListener('focus',onFocus);window.removeEventListener('verification-settings-changed',changed)};
 },[]);
 return <Context.Provider value={settings}>{children}</Context.Provider>;
}
