'use client';
import {useEffect,useState} from 'react';
import {useRouter} from 'next/navigation';
import Link from 'next/link';
import {localizeText} from '../../../lib/localization.mjs';
import {useLocalizationRender} from '../../../components/LocalizationProvider';
import {hostApi,hostRequest} from '../../../lib/host-api.mjs';
import {readTryDraft,TRY_DRAFT_KEY} from '../../../lib/acquisition.mjs';
import '../../../components/acquisition.css';

/** After sign-up/sign-in: move the visitor's draft into the signed-in creator, then open it. Nothing is created until they confirm there. */
export default function ClaimTryDraft(){useLocalizationRender();
 const router=useRouter();const [message,setMessage]=useState('Bringing your invitation across…');
 useEffect(()=>{let active=true;hostApi.me(fetch).then((me:{userId?:string;username?:string})=>{if(!active)return;
  const draft=readTryDraft(localStorage);if(!draft){router.replace('/events/new');return}
  const key=`gift.event.draft.${me.userId||me.username}`;
  localStorage.setItem(key,JSON.stringify({...draft,contactName:draft.hostName,savedAt:Date.now()}));localStorage.removeItem(TRY_DRAFT_KEY);
  void hostRequest(fetch,'/v1/me/acquisition-events',{method:'POST',payload:{kind:'TRY_CLAIMED'}}).catch(()=>{});
  router.replace('/events/new')}).catch(()=>{if(active)setMessage('Please sign in to continue with your invitation.')});return()=>{active=false}},[router]);
 return <main className="tyPage"><section className="tyCard"><p role="status">{localizeText(message)}</p>{message.startsWith('Please')&&<Link className="focusPrimary" href={`/login?next=${encodeURIComponent('/try/claim')}`}>{localizeText('Sign in')}</Link>}</section></main>;
}
