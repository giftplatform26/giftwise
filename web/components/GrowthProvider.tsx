'use client';
import {createContext,useCallback,useContext,useEffect,useRef,useState,type ReactNode} from 'react';
import {normalizeGrowth} from '../lib/growth.mjs';
type Settings={features:Record<string,boolean>;version:number};
const Context=createContext({features:normalizeGrowth(null),ready:false,refresh:async()=>{},apply:(_value:Settings)=>{}});
export const useGrowth=()=>useContext(Context);
export default function GrowthProvider({children}:{children:ReactNode}){
 const [settings,setSettings]=useState<Settings>({features:normalizeGrowth(null),version:-1}),[ready,setReady]=useState(false);
 const active=useRef(false),sequence=useRef(0),channel=useRef<BroadcastChannel|null>(null);
 const refresh=useCallback(async()=>{const id=++sequence.current;try{const r=await fetch('/backend/api/public/growth-settings',{cache:'no-store',credentials:'omit',signal:AbortSignal.timeout(5000)});if(!r.ok)throw Error();const value=await r.json();if(active.current&&id===sequence.current)setSettings({features:normalizeGrowth(value),version:value.version});}catch{if(active.current&&id===sequence.current)setSettings({features:normalizeGrowth(null),version:-1});}finally{if(active.current)setReady(true)}},[]);
 const apply=useCallback((value:Settings)=>{sequence.current++;setSettings({features:normalizeGrowth(value),version:value.version});channel.current?.postMessage('refresh')},[]);
 useEffect(()=>{active.current=true;void refresh();const visible=()=>{if(!document.hidden)void refresh()};window.addEventListener('focus',visible);document.addEventListener('visibilitychange',visible);if(typeof BroadcastChannel!=='undefined'){channel.current=new BroadcastChannel('giftwise-growth');channel.current.onmessage=()=>void refresh()}return()=>{active.current=false;sequence.current++;window.removeEventListener('focus',visible);document.removeEventListener('visibilitychange',visible);channel.current?.close()}},[refresh]);
 return <Context.Provider value={{features:settings.features,ready,refresh,apply}}>{children}</Context.Provider>;
}
