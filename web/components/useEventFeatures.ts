 'use client';
import {useEffect,useState} from 'react';
import {fetchEventFeatures,normalizeFeatures} from '../lib/event-features.mjs';
export type EventFeatures=Record<'GROUP_GIFTING'|'RSVP'|'GUEST_ATTENDANCE'|'WHATSAPP_SHARING'|'GUESTBOOK',boolean>&{SOCIAL_SHARING?:boolean;GUEST_GIFTS?:boolean};
export default function useEventFeatures(token:string,initial?:EventFeatures){
 const [features,setFeatures]=useState<EventFeatures>(()=>normalizeFeatures(initial));
 const [loading,setLoading]=useState(!initial);
 useEffect(()=>{
  let active=true;let controller:AbortController|undefined;
  setFeatures(normalizeFeatures(initial));setLoading(!initial);
  const refresh=async()=>{
   controller?.abort();controller=new AbortController();const current=controller;
   // Keep active forms mounted during background checks; revoke only when the result arrives.
   try{const result=await fetchEventFeatures(fetch,token,current.signal);if(active&&!current.signal.aborted)setFeatures(result);}
   catch{if(active&&!current.signal.aborted)setFeatures(normalizeFeatures(null));}
   finally{if(active&&!current.signal.aborted)setLoading(false);}
  };
  const visible=()=>{if(!document.hidden)void refresh()};
  if(!initial)void refresh();
  window.addEventListener('focus',visible);document.addEventListener('visibilitychange',visible);
  return()=>{active=false;controller?.abort();window.removeEventListener('focus',visible);document.removeEventListener('visibilitychange',visible)};
 },[token,initial]);
 return {features,loading};
}
