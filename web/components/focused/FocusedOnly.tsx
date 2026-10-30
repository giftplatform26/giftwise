'use client';
import {useEffect,type ReactNode} from 'react';
import {useRouter} from 'next/navigation';
import {useExperience} from '../ExperienceProvider';
export default function FocusedOnly({children}:{children:ReactNode}){const {mode}=useExperience();const router=useRouter();useEffect(()=>{if(mode==='CLASSIC')router.replace('/events')},[mode,router]);return mode==='FOCUSED'?<>{children}</>:<main role="status">Opening your celebrations…</main>}
