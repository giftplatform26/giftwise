'use client';
import {usePathname} from 'next/navigation';
import {type ReactNode} from 'react';
import {useHostWorkspace} from '../HostWorkspace';
import {eventDate,eventBannerPhrase} from '../../lib/focused.mjs';
export default function HostShell({children}:{children:ReactNode}){
 const path=usePathname();
 const {eventId,event}=useHostWorkspace();
 const status=event?.status||'';
 const label=status==='DRAFT'?'Private draft':status==='PUBLISHED'?'Invitation published':status==='COMPLETED'?'Completed':status==='CANCELLED'?'Cancelled':'Your celebration';
 return <div className={`focusShell${eventId?' focusEventShell':''}`}>{eventId&&<section className="hostEventBanner" aria-label="Current celebration"><div className="hostEventHeading"><h1>{event?.title||'Celebration'}</h1>{event&&<p>{eventDate(event.eventDate)}</p>}</div><p className="hostEventMotto" aria-live="polite">{eventBannerPhrase(path)}</p><span className={`hostEventStatus hostEventStatus-${status.toLowerCase()||'loading'}`}>{label}</span></section>}{children}</div>;
}
