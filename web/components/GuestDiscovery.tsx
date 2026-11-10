'use client';
import {useLocalizationRender} from './LocalizationProvider';
import Link from 'next/link';
import {useGrowth} from './GrowthProvider';
import {localizeText as t} from '../lib/localization.mjs';
export default function GuestDiscovery(){useLocalizationRender();const {features}=useGrowth();if(!features.GUEST_DISCOVERY)return null;return <aside className="growthDiscovery"><p>{t('Have a moment of your own to celebrate?')}</p><Link href="/register" data-track-action="GUEST_DISCOVERY">{t('Create your own invitation')} →</Link></aside>}
