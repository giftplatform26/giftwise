'use client';
/* localization:instrumented */
import {localize} from '../../lib/localization.mjs';
import {useLocalizationRender} from '../../components/LocalizationProvider';

import {Suspense,type ReactNode} from 'react';
import {usePathname} from 'next/navigation';
import {useExperience} from '../../components/ExperienceProvider';
import HostWorkspace from '../../components/HostWorkspace';
import FocusedHostShell from '../../components/focused/HostShell';
function HostNavigation({children}:{children:ReactNode}){useLocalizationRender();
 const path=usePathname(),{mode}=useExperience();
 const host=path==='/'||path==='/dashboard'||path==='/events'||path.startsWith('/events/')||path==='/gifts'||path.startsWith('/host/');
 if(!host)return <>{children}</>;
 return <HostWorkspace>{mode==='FOCUSED'?<FocusedHostShell>{children}</FocusedHostShell>:children}</HostWorkspace>;
}
export default function HostGlobalNav({children}:{children:ReactNode}){useLocalizationRender();return <Suspense fallback={<main role="status">{localize("ui.bed9096c411e6da3",{defaultValue:"Opening your workspace…"})}</main>}><HostNavigation>{children}</HostNavigation></Suspense>}
