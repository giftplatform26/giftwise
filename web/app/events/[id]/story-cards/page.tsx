'use client';
import {useEffect,useState} from 'react';
import {useParams} from 'next/navigation';
import {localizeText} from '../../../../lib/localization.mjs';
import {useLocalizationRender} from '../../../../components/LocalizationProvider';
import {hostApi} from '../../../../lib/host-api.mjs';
import StoryCardMaker from '../../../../components/StoryCardMaker';
import '../../../../components/gift-books.css';

type Ev={title:string;hostName?:string|null;hostDisplayName?:string|null;occasionType:string;eventDate:string|null;publicToken:string;status:string};

export default function StoryCards(){useLocalizationRender();
 const {id}=useParams();const eventId=String(id??'');
 const [e,setE]=useState<Ev|null>(null),[on,setOn]=useState<boolean|null>(null),[error,setError]=useState('');
 useEffect(()=>{if(!/^[0-9a-f-]{36}$/i.test(eventId))return;
  Promise.all([hostApi.getEvent(fetch,eventId),fetch(`/backend/api/v1/events/${eventId}/features`,{credentials:'include',cache:'no-store'}).then(r=>r.ok?r.json():{})])
   .then(([ev,f])=>{setE(ev as Ev);setOn(Boolean((f as Record<string,boolean>).STORY_CARDS))}).catch(err=>setError(err instanceof Error?err.message:'Could not load'))},[eventId]);
 const link=e&&typeof window!=='undefined'?`${window.location.origin}/e/${e.publicToken}`:'';
 return <main className="focusPage gbPage">
  <header className="focusPageHeading"><div><p className="focusEyebrow">{localizeText('Spread the word')}</p><h1>{localizeText('Status & Story cards')}</h1>
   <p>{localizeText('Beautiful phone-sized cards for WhatsApp Status and Instagram Stories. Post the invitation, a countdown in the last week, and a thank-you afterwards.')}</p></div></header>
  {error&&<p className="focusError" role="alert">{localizeText(error)}</p>}
  {on===false&&<p className="gbQueued">{localizeText('Story cards are not switched on for this celebration.')}</p>}
  {e&&on&&(e.status==='DRAFT'?<p className="gbQueued">{localizeText('Publish your invitation first so the link on the card works.')}</p>:
   <StoryCardMaker title={e.title} hostName={e.hostDisplayName??e.hostName} occasion={e.occasionType} date={e.eventDate} link={link} kinds={['INVITE','COUNTDOWN','THANK_YOU','LIVE']}/>)}
 </main>;
}
