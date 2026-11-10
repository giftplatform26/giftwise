'use client';
import {useEffect,useState} from 'react';
import {GROWTH_FEATURES} from '../lib/growth.mjs';
import {hostRequest} from '../lib/host-api.mjs';
import {useGrowth} from './GrowthProvider';
import {localizeText as t} from '../lib/localization.mjs';
import {useLocalizationRender} from './LocalizationProvider';
type Settings={features:Record<string,boolean>;version:number};
export default function GrowthControls(){
 useLocalizationRender();const {apply}=useGrowth();const [value,setValue]=useState<Settings|null>(null),[busy,setBusy]=useState(false),[error,setError]=useState(''),[notice,setNotice]=useState('');
 async function load(){setError('');try{const r=await fetch('/backend/api/public/growth-settings',{cache:'no-store'});if(!r.ok)throw Error();setValue(await r.json())}catch{setError('Unable to load experience controls. Please retry.')}}
 useEffect(()=>{void load()},[]);
 async function save(e:React.FormEvent){e.preventDefault();if(!value)return;setBusy(true);setError('');setNotice('');try{
  await hostRequest(fetch,'/auth/csrf');const entry=document.cookie.split(';').map(x=>x.trim()).find(x=>x.startsWith('XSRF-TOKEN='));if(!entry)throw Error('Please sign in again.');
  const r=await fetch('/backend/api/admin/console/growth-settings',{method:'PUT',credentials:'include',headers:{'Content-Type':'application/json','X-XSRF-TOKEN':decodeURIComponent(entry.slice(11))},body:JSON.stringify(value)});
  if(!r.ok)throw Error(r.status===409?'Settings changed in another session. Reload before saving.':r.status===403?'Platform administrator access is required.':'Unable to save experience controls. Please retry.');
  const saved=await r.json();setValue(saved);apply(saved);setNotice('Experience controls saved. Other open pages refresh on focus or reload.');
 }catch(e){setError(e instanceof Error?e.message:'Unable to save controls.')}finally{setBusy(false)}}
 return <section className="acPanel"><h2>{t('Experience and growth controls')}</h2><p>{t('Choose the experiences to roll out. RSVP, WhatsApp, albums, event reuse and payment permissions remain under Hosts & privileges and Events. These switches never grant those permissions.')}</p>{error&&<p role="alert">{t(error)}</p>}{notice&&<p role="status">{t(notice)}</p>}{value?<form onSubmit={save}><fieldset disabled={busy} className="growthSwitches"><legend>{t('Platform experiences')}</legend>{Object.entries(GROWTH_FEATURES).map(([key,[label,help]])=><label key={key}><input type="checkbox" checked={value.features[key]===true} onChange={e=>setValue({...value,features:{...value.features,[key]:e.target.checked}})}/><span><strong>{t(label)}</strong><small>{t(help)}</small></span></label>)}</fieldset><p>{t('Rich previews are off by default and also require each host’s consent. Messaging services may retain previews already fetched. Optional analytics still requires visitor consent.')}</p><button disabled={busy}>{t(busy?'Saving controls…':'Save experience controls')}</button> <button type="button" disabled={busy} onClick={()=>void load()}>{t('Reload saved settings')}</button></form>:<button onClick={()=>void load()}>{t('Load controls')}</button>}</section>;
}
