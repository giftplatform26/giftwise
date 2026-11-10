'use client';
/* localization:instrumented */
import {localize} from '../../lib/localization.mjs';
import {useLocalizationRender} from '../LocalizationProvider';

import {useEffect,type ReactNode} from 'react';
import {useRouter} from 'next/navigation';
import {useExperience} from '../ExperienceProvider';
export default function FocusedOnly({children}:{children:ReactNode}){useLocalizationRender();const {mode}=useExperience();const router=useRouter();useEffect(()=>{if(mode==='CLASSIC')router.replace('/events')},[mode,router]);return mode==='FOCUSED'?<>{children}</>:<main role="status">{localize("ui.2ee32085a6223e09",{defaultValue:"Opening your celebrations…"})}</main>}
