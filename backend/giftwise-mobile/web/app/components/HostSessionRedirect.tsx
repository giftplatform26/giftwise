'use client';
import {useLayoutEffect} from 'react';
import {redirectIfSessionExpired} from '../../lib/host-session.mjs';
export default function HostSessionRedirect(){
 useLayoutEffect(()=>{
  const previous=window.fetch;
  const wrapped:typeof fetch=async(input,init)=>{
   const response=await previous.call(window,input,init);
   const url=typeof input==='string'?input:input instanceof URL?input.href:input.url;
   redirectIfSessionExpired(response,url);
   return response;
  };
  window.fetch=wrapped;
  return()=>{if(window.fetch===wrapped)window.fetch=previous};
 },[]);
 return null;
}
