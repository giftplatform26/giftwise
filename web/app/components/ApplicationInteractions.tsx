 'use client';
import {useGrowth} from '../../components/GrowthProvider';
import {useEffect,useState} from 'react';
import {usePathname} from 'next/navigation';
import {installInteractionTracking,trackPage} from '../../lib/interaction-analytics.mjs';
import {readConsent} from '../../lib/cookie-consent.mjs';
export default function ApplicationInteractions(){const path=usePathname();const {features}=useGrowth();const enabled=features.INTERACTION_ANALYTICS;const [allowed,setAllowed]=useState(false);useEffect(()=>{setAllowed(readConsent()?.analytics===true);const update=(event:Event)=>setAllowed((event as CustomEvent).detail?.analytics===true);const storage=()=>setAllowed(readConsent()?.analytics===true);window.addEventListener('giftwise-consent-changed',update);window.addEventListener('storage',storage);return()=>{window.removeEventListener('giftwise-consent-changed',update);window.removeEventListener('storage',storage)}},[]);useEffect(()=>{if(allowed&&enabled)return installInteractionTracking()},[allowed,enabled]);useEffect(()=>{if(allowed&&enabled)trackPage()},[path,allowed,enabled]);return null}
