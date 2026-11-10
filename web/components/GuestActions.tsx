'use client';
import {beginAction,completeAction} from '../lib/interaction-analytics.mjs';
/* localization:instrumented */
import {localize,localizeText} from '../lib/localization.mjs';
import {useLocalizationRender} from './LocalizationProvider';

import {useState} from 'react';
import {Heart,CalendarPlus,MessageCircle} from 'lucide-react';
import GroupContribution from './GroupContribution';
import useEventFeatures,{type EventFeatures} from './useEventFeatures';
import {calendarInvitation} from '../lib/invitation-sharing.mjs';
import './guest-actions.css';
export default function GuestActions({token,title,date,features:initial,onContribute}:{token:string;title:string;date:string|null;features?:EventFeatures;onContribute:()=>void}){useLocalizationRender();
 const {features}=useEventFeatures(token,initial);const [notice,setNotice]=useState('');
 async function saveDate(){
  const interaction=beginAction('CALENDAR_DOWNLOAD',{token});
  try{
   if(features.EVENT_ESSENTIALS&&features.CELEBRATION_PLANNING){
    const response=await fetch(`/backend/api/public/events/${encodeURIComponent(token)}/calendar`,{cache:'no-store'});if(!response.ok)throw Error();
    const url=URL.createObjectURL(await response.blob());const link=document.createElement('a');link.href=url;link.download='celebration.ics';link.click();setTimeout(()=>URL.revokeObjectURL(url),1000);completeAction(interaction,'BROWSER_READY','CALENDAR_HANDED_TO_BROWSER');setNotice('Calendar invitation downloaded. Open it to save the date.');return;
   }
   const content=calendarInvitation({token,title,date,url:`${location.origin}/e/${token}`});if(!content){completeAction(interaction,'FAILURE','INVALID_EVENT_DATE');return;}
   const url=URL.createObjectURL(new Blob([content],{type:'text/calendar;charset=utf-8'}));const a=document.createElement('a');a.href=url;a.download='celebration.ics';a.click();setTimeout(()=>URL.revokeObjectURL(url),1000);completeAction(interaction,'BROWSER_READY','CALENDAR_HANDED_TO_BROWSER');setNotice('Calendar invitation downloaded. Open it to save the date.');
  }catch{completeAction(interaction,'FAILURE','CALENDAR_DOWNLOAD_FAILED');setNotice('Could not download the calendar invitation.');}
 }

 return <div className="guest-action-area"><nav className="guest-actions" aria-label={localize("ui.cffd89a6e999001d",{defaultValue:"Ways to celebrate"})}>{features.GIFTING_ENABLED!==false&&<button className="guest-action-primary" type="button" onClick={onContribute}><Heart size={17}/> {localize("ui.a89376d87b358fce",{defaultValue:" Contribute"})}</button>}{features.GROUP_GIFTING&&<GroupContribution token={token} compact/>}{(features.RSVP||features.GUESTBOOK)&&<a href="#guest-celebration"><MessageCircle size={17}/> {features.RSVP&&features.GUESTBOOK?localize("ui.a7bae23613d3952f",{defaultValue:"RSVP & wishes"}):features.RSVP?localize("ui.1dfe8a8e0cb2d1da",{defaultValue:"RSVP"}):localize("ui.9b2c93eb39dfb1fa",{defaultValue:"Leave wishes"})}</a>}{date&&<button type="button" onClick={saveDate}><CalendarPlus size={17}/> {localize("ui.fc40890ffdf9eb33",{defaultValue:" Save the date"})}</button>}</nav>{notice&&<p role="status" className="guest-action-notice">{localizeText(notice)}</p>}</div>;
}
