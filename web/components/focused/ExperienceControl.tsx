'use client';
/* localization:instrumented */
import {localize,localizeText} from '../../lib/localization.mjs';
import {useLocalizationRender} from '../LocalizationProvider';

import {useState} from 'react';
import {useExperience,type ViewMode} from '../ExperienceProvider';
export default function ExperienceControl(){useLocalizationRender();
 const {mode,apply}=useExperience();const [busy,setBusy]=useState(false),[error,setError]=useState(''),[message,setMessage]=useState('');
 async function change(next:ViewMode){if(busy||next===mode)return;setBusy(true);setError('');setMessage('');try{
  const init=await fetch('/backend/api/auth/csrf',{credentials:'include',cache:'no-store'});if(!init.ok)throw Error('Please sign in again.');
  const cookie=document.cookie.split(';').map(x=>x.trim()).find(x=>x.startsWith('XSRF-TOKEN='));if(!cookie)throw Error('Refresh this page to initialise your secure session.');
  const response=await fetch('/backend/api/admin/console/experience',{method:'POST',credentials:'include',headers:{'Content-Type':'application/json','X-XSRF-TOKEN':decodeURIComponent(cookie.slice('XSRF-TOKEN='.length))},body:JSON.stringify({viewMode:next})});
  if(!response.ok)throw Error(response.status===403?'Platform administrator access is required.':'The view could not be saved. Try again.');
  const saved=await response.json();if(saved.viewMode!==next)throw Error('The view was not saved.');apply(next);setMessage(`${next==='FOCUSED'?'Focused':'Classic'} view saved. Other open pages keep their current view until reloaded.`);
 }catch(e){setError(e instanceof Error?e.message:'Unable to save view')}finally{setBusy(false)}}
 return <section className="acPanel experienceControl" aria-labelledby="experience-label"><div><h2 id="experience-label">{localize("ui.8472ebb9fd0fcf86",{defaultValue:"Choose an experience"})}</h2><p>{localize("ui.b6b4728c227dd9b5",{defaultValue:"Choose the view for hosts, guests, groups and administration. Permissions and payment rules stay the same."})}</p></div><fieldset disabled={busy} className="experienceChoices"><legend className="srOnly">{localize("ui.e4e0564b0e03c1e1",{defaultValue:"Choose application view"})}</legend>{(['CLASSIC','FOCUSED'] as const).map(value=><label key={value}><input type="radio" name="experience-view" value={value} checked={mode===value} onChange={()=>void change(value)}/><span><strong>{value==='FOCUSED'?localize("ui.4f9a966fb271c31a",{defaultValue:"Focused"}):localize("ui.ae25d6481fc1741f",{defaultValue:"Classic"})}</strong><small>{value==='FOCUSED'?localize("ui.ba0452f465480b30",{defaultValue:"Calm, event-centred and designed for mobile."}):localize("ui.cee1ef6828fa0fe1",{defaultValue:"The existing application layout."})}</small></span></label>)}</fieldset><p className="finePrint">{localize("ui.2f164b7a0dd61a93",{defaultValue:"Applies platform-wide on the next reload. You can switch back at any time."})}</p>{busy&&<p role="status">{localize("ui.c6a3c87c8708580c",{defaultValue:"Saving view…"})}</p>}{error&&<p role="alert">{localizeText(error)}</p>}{message&&<p role="status">{localizeText(message)}</p>}</section>;
}
