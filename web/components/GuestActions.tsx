'use client';
import {useState} from 'react';
import {Heart,CalendarPlus,MessageCircle} from 'lucide-react';
import GroupContribution from './GroupContribution';
import useEventFeatures,{type EventFeatures} from './useEventFeatures';
import {calendarInvitation} from '../lib/invitation-sharing.mjs';
import './guest-actions.css';
export default function GuestActions({token,title,date,features:initial,onContribute}:{token:string;title:string;date:string|null;features?:EventFeatures;onContribute:()=>void}){
 const {features}=useEventFeatures(token,initial);const [notice,setNotice]=useState('');
 function saveDate(){const content=calendarInvitation({token,title,date,url:`${location.origin}/e/${token}`});if(!content)return;const url=URL.createObjectURL(new Blob([content],{type:'text/calendar;charset=utf-8'}));const a=document.createElement('a');a.href=url;a.download='celebration.ics';a.click();setTimeout(()=>URL.revokeObjectURL(url),1000);setNotice('Calendar invitation downloaded. Open it to save the date.');}
 return <div className="guest-action-area"><nav className="guest-actions" aria-label="Ways to celebrate"><button className="guest-action-primary" type="button" onClick={onContribute}><Heart size={17}/> Contribute</button>{features.GROUP_GIFTING&&<GroupContribution token={token} compact/>}{(features.RSVP||features.GUESTBOOK)&&<a href="#guest-celebration"><MessageCircle size={17}/> {features.RSVP&&features.GUESTBOOK?'RSVP & wishes':features.RSVP?'RSVP':'Leave wishes'}</a>}{date&&<button type="button" onClick={saveDate}><CalendarPlus size={17}/> Save the date</button>}</nav>{notice&&<p role="status" className="guest-action-notice">{notice}</p>}</div>;
}
