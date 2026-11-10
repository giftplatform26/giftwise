'use client';
import {useLocalizationRender} from './LocalizationProvider';
import {useEffect,useState} from 'react';
import {hostRequest} from '../lib/host-api.mjs';
import {localizeText as t} from '../lib/localization.mjs';
export default function SharePreviewControl({eventId}:{eventId:string}){
 useLocalizationRender();const [value,setValue]=useState<{enabled:boolean;platformEnabled:boolean}|null>(null),[busy,setBusy]=useState(false),[notice,setNotice]=useState('');
 useEffect(()=>{let active=true;hostRequest(fetch,`/v1/events/${eventId}/share-preview`).then(value=>{if(active)setValue(value)}).catch(()=>{if(active)setNotice('Sharing preview settings could not be loaded. Reload to retry.')});return()=>{active=false}},[eventId]);
 async function change(enabled:boolean){setBusy(true);setNotice('');try{setValue(await hostRequest(fetch,`/v1/events/${eventId}/share-preview`,{method:'PUT',payload:{enabled}}));setNotice('Sharing preview preference saved.')}catch{setNotice('Unable to save your preview preference. Please retry.')}finally{setBusy(false)}}
 return <section className="focusPanel"><h2>{t('Messaging preview privacy')}</h2><p>{t('Allow messaging services to display the event title, occasion and date when someone shares its link. Venue, private contacts, guest details and gifts are excluded. Existing cached previews cannot be recalled from third-party services.')}</p>{value&&<label><input type="checkbox" checked={value.enabled} disabled={busy||(!value.platformEnabled&&!value.enabled)} onChange={e=>void change(e.target.checked)}/> {t('Allow a rich preview for this invitation')}</label>}{value&&!value.platformEnabled&&<p>{t('The platform administrator has disabled rich previews. Your invitation link continues to work.')}</p>}{notice&&<p role="status">{t(notice)}</p>}</section>;
}
