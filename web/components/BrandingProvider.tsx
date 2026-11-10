 'use client';
import {createContext,useCallback,useContext,useEffect,useRef,useState,type ReactNode} from 'react';
import Link from 'next/link';
import {DEFAULT_BRANDING,brandAsset,contrastText} from '../lib/branding.mjs';
export type Branding=typeof DEFAULT_BRANDING;
const Context=createContext({brand:DEFAULT_BRANDING,refresh:async()=>{},update:(_value:Branding)=>{}});
export function useBranding(){return useContext(Context)}
export function BrandLogo({size=40,showName=true}:{size?:number,showName?:boolean}){const {brand}=useBranding();return <span className="giftwiseBrand"><img src={brandAsset(brand.logoUrl)} alt={showName?'':brand.applicationName} width={size} height={size} style={{objectFit:'contain',maxWidth:160}} onError={e=>{if(!e.currentTarget.src.endsWith('/giftwise-icon.svg'))e.currentTarget.src='/giftwise-icon.svg'}}/>{showName&&<span>{brand.applicationName}</span>}</span>}
export function BrandHeader(){return <header className="giftwiseBrandHeader"><Link href="/" aria-label="Home"><BrandLogo/></Link></header>}
export default function BrandingProvider({children}:{children:ReactNode}){const [brand,setBrand]=useState(DEFAULT_BRANDING);const channel=useRef<BroadcastChannel|null>(null);const mounted=useRef(false);
const refresh=useCallback(async()=>{try{const response=await fetch('/backend/api/public/branding',{cache:'no-store',credentials:'omit'});if(response.ok){const value=await response.json();if(mounted.current)setBrand(previous=>value.revision>=previous.revision?{...DEFAULT_BRANDING,...value}:previous)}}catch{}},[]);
const update=useCallback((value:Branding)=>{setBrand(value);channel.current?.postMessage('refresh')},[]);
useEffect(()=>{mounted.current=true;void refresh();const onFocus=()=>{if(document.visibilityState==='visible')void refresh()};window.addEventListener('focus',onFocus);document.addEventListener('visibilitychange',onFocus);const timer=setInterval(onFocus,60000);if(typeof BroadcastChannel!=='undefined'){channel.current=new BroadcastChannel('giftwise-brand');channel.current.onmessage=()=>void refresh()}return()=>{mounted.current=false;clearInterval(timer);window.removeEventListener('focus',onFocus);document.removeEventListener('visibilitychange',onFocus);channel.current?.close()}},[refresh]);
useEffect(()=>{document.documentElement.style.setProperty('--brand-color',brand.brandColor);document.documentElement.style.setProperty('--brand-on-color',contrastText(brand.brandColor));document.title=brand.applicationName+' | Gifting & Celebrations';let icon=document.querySelector<HTMLLinkElement>('link[rel="icon"]');if(!icon){icon=document.createElement('link');icon.rel='icon';document.head.appendChild(icon)}icon.href=brandAsset(brand.faviconUrl||brand.logoUrl)},[brand]);
return <Context.Provider value={{brand,refresh,update}}>{children}</Context.Provider>}
