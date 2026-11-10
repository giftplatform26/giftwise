'use client';
import {useLayoutEffect} from 'react';
import {redirectIfSessionExpired,isActivityRequest} from '../../lib/host-session.mjs';
export default function HostSessionRedirect(){
 useLayoutEffect(()=>{
  const previous=window.fetch;
  let pendingActivity=true;
  const mark=(event:Event)=>{if(event.isTrusted)pendingActivity=true};
  const events=['pointerdown','keydown','touchstart','wheel'];
  for(const name of events)window.addEventListener(name,mark,{capture:true,passive:true});
  const wrapped:typeof fetch=async(input,init)=>{
   const url=typeof input==='string'?input:input instanceof URL?input.href:input.url;
   if(pendingActivity&&isActivityRequest(url,window.location.origin)){
    const headers=new Headers(init?.headers??(input instanceof Request?input.headers:undefined));headers.set('X-Gift-User-Activity','1');init={...init,headers};pendingActivity=false;
   }
   const response=await previous.call(window,input,init);
   redirectIfSessionExpired(response,url);
   return response;
  };
  window.fetch=wrapped;
  return()=>{for(const name of events)window.removeEventListener(name,mark,true);if(window.fetch===wrapped)window.fetch=previous};
 },[]);
 return null;
}
