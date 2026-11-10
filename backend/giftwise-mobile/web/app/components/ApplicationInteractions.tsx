'use client';
import {useEffect} from 'react';
import {usePathname} from 'next/navigation';
import {installInteractionTracking,trackPage} from '../../lib/interaction-analytics.mjs';
export default function ApplicationInteractions(){const path=usePathname();useEffect(()=>installInteractionTracking(),[]);useEffect(()=>{trackPage()},[path]);return null;}
