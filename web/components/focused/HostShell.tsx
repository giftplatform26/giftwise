'use client';
/* localization:instrumented */
import {localize,localizeText} from '../../lib/localization.mjs';
import {useLocalizationRender} from '../LocalizationProvider';

import {usePathname} from 'next/navigation';
import {type ReactNode} from 'react';
import {useHostWorkspace} from '../HostWorkspace';
import {eventDate,eventBannerPhrase} from '../../lib/focused.mjs';
export default function HostShell({children}:{children:ReactNode}){useLocalizationRender();
 const path=usePathname();
 const {eventId,event}=useHostWorkspace();
 const status=event?.status||'';
 const label=status==='DRAFT'?'Private draft':status==='PUBLISHED'?'Invitation published':status==='COMPLETED'?'Completed':status==='CANCELLED'?'Cancelled':'Your celebration';
 return <div className={`focusShell${eventId?' focusEventShell':''}`}>{eventId&&<section className="hostEventBanner" aria-label={localize("ui.03dd38191d6c402a",{defaultValue:"Current celebration"})}><div className="hostEventHeading"><h1>{event?.title||localize("ui.4a8b403035a2b673",{defaultValue:"Celebration"})}</h1>{event&&<p>{eventDate(event.eventDate)}</p>}</div><p className="hostEventMotto" aria-live="polite">{eventBannerPhrase(path)}</p><span className={`hostEventStatus hostEventStatus-${status.toLowerCase()||'loading'}`}>{localizeText(label)}</span></section>}{children}</div>;
}
