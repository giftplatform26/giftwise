'use client';
/* localization:instrumented */
import {localize,localizeText} from '../lib/localization.mjs';
import {useLocalizationRender} from './LocalizationProvider';

import {useEffect,useState} from 'react';
import {bootstrapCsrf} from '../lib/host-api.mjs';
import {csrfToken} from '../lib/gift-api.mjs';
const names:Record<string,string>={SEATING_PLAN:'Seating plan',GUEST_PASSES:'Guest passes',STORY_CARDS:'Status & Story cards',PARTNER_ACCOUNTS:'Partner branding & access',GROUP_COLLECTIONS:'Group gift collection',GUEST_FIRST_FREE:'First-celebration-free offer to guests',CELEBRATION_SHOWCASE:'Showcase sharing',PLANNING_CHECKLIST:'Planning checklist',EVENT_BUDGET:'Budget & vendor payments',RUN_SHEET:'Day-of run sheet',CATERING_HEADCOUNT:'Catering headcount (meal choices)',GUEST_CHECKIN:'Entry check-in',LIVE_UPDATES:'Live updates & stream link',RETURN_GIFTS:'Return gifts tracker',MEMORY_BOOK:'Printable memory book',EXPERIENCE_GIFTS:'Experience & charity wishes',WHATSAPP_MESSAGING:'WhatsApp invitations & replies',EVENT_FUNCTIONS:'Multi-function schedule',CO_HOSTS:'Co-hosts & gift-desk helpers',LIVE_WALL:'Live venue wall',SHAGUN_REGISTER:'Shagun / gift register',CELEBRATION_PLANNING:'Simple celebrations & optional gifting',EVENT_ESSENTIALS:'Venue details & calendar',EVENT_REUSE:'Repeat as a new draft'};
type Grant={module:string;eventEnabled:boolean;hostAvailable:boolean;effectiveEnabled:boolean};
export default function AdminEventModules({eventId}:{eventId:string}){useLocalizationRender();
 const [grants,setGrants]=useState<Grant[]>([]),[error,setError]=useState(''),[busy,setBusy]=useState(false),[opened,setOpened]=useState(false);
 const base=`/backend/api/admin/console/events/${eventId}/modules`;
 async function load(){const response=await fetch(base,{credentials:'include',cache:'no-store'});if(!response.ok)throw Error('Could not load event controls');setGrants(await response.json())}
 useEffect(()=>{if(opened)void load().catch(e=>setError(e.message))},[eventId,opened]);
 async function toggle(code:string,enabled:boolean){setBusy(true);setError('');try{await bootstrapCsrf(fetch);const token=csrfToken(document.cookie);if(!token)throw Error('Reload to initialize a secure session');const response=await fetch(base+'/'+code,{method:'POST',credentials:'include',headers:{'Content-Type':'application/json','X-XSRF-TOKEN':token},body:JSON.stringify({enabled})});if(!response.ok)throw Error('Could not update event access');await load()}catch(e){setError(e instanceof Error?e.message:'Could not save')}finally{setBusy(false)}}
 return <details onToggle={e=>setOpened(e.currentTarget.open)}><summary>{localize("ui.a3412e65c26aba65",{defaultValue:"Celebration feature controls"})}</summary><p>{localize("ui.56d9f03335cb4f5e",{defaultValue:"Requires platform, host and event approval. Venue/calendar editing also requires Simple celebrations."})}</p>{error&&<p role="alert">{localizeText(error)}</p>}{grants.map(g=><label key={g.module} style={{display:'block',margin:'12px 0'}}><input type="checkbox" checked={g.eventEnabled} disabled={busy} onChange={e=>void toggle(g.module,e.target.checked)}/>{names[g.module]}<small style={{display:'block'}}>{g.effectiveEnabled?localize("ui.e674447337e83c13",{defaultValue:"Available"}):g.eventEnabled?localize("ui.8e263afc5acc9d59",{defaultValue:"Waiting for platform and host approval"}):localize("ui.80b7b0d4756b110f",{defaultValue:"Disabled for this event"})}</small></label>)}</details>;
}
