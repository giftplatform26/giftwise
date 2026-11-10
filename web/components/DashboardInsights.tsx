'use client';
import {useLocalizationRender} from './LocalizationProvider';
import {useEffect,useState} from 'react';
import Link from 'next/link';
import {useGrowth} from './GrowthProvider';
import {hostRequest} from '../lib/host-api.mjs';
import {localizeText as t} from '../lib/localization.mjs';
type Row={id:string;title:string;status:string;event_date:string|null;gift_count:number;responses:number|null;attending:number|null};
export default function DashboardInsights(){useLocalizationRender();const {features}=useGrowth();const [rows,setRows]=useState<Row[]>([]),[state,setState]=useState('loading');
 async function load(){setState('loading');try{setRows(await hostRequest(fetch,'/v1/events/workspace-summary'));setState('ready')}catch{setState('error')}}
 useEffect(()=>{if(features.SMART_DASHBOARD)void load()},[features.SMART_DASHBOARD]);
 if(!features.SMART_DASHBOARD)return null;
 return <section className="growthInsights" aria-label={t('Your next steps')}><div className="growthSectionHeading"><div><p className="focusEyebrow">{t('A little help along the way')}</p><h2>{t('Your next steps')}</h2></div><Link href="/events/new">{t('Create a celebration')}</Link></div>{state==='loading'?<p role="status">{t('Loading next steps…')}</p>:state==='error'?<p role="alert">{t('Unable to load next steps.')} <button onClick={()=>void load()}>{t('Retry')}</button></p>:rows.length===0?<p>{t('Start with an occasion. We will help you turn it into a shareable invitation.')}</p>:<div className="growthInsightGrid">{rows.map(row=><article key={row.id}><small>{t(row.status==='DRAFT'?'Finish your private draft':row.status==='COMPLETED'?'Keep the memories':'Your invitation is live')}</small><h3>{row.title}</h3>{row.event_date&&<p>{row.event_date}</p>}{row.responses!==null&&<p>{row.responses} {t('responses')} · {row.attending} {t('attending')}</p>}<Link href={`/events/${row.id}${row.status==='COMPLETED'?'/thank-you':row.status==='DRAFT'?'/preview':''}`}>{t(row.status==='DRAFT'?'Review invitation':row.status==='COMPLETED'?'Prepare thank-you cards':'Manage celebration')} →</Link></article>)}</div>}</section>;
}
