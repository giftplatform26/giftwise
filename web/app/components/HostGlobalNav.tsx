'use client';
/* localization:instrumented */
import {localize} from '../../lib/localization.mjs';
import {useLocalizationRender} from '../../components/LocalizationProvider';

import {Suspense,type ReactNode} from 'react';
import {usePathname} from 'next/navigation';
import {useExperience} from '../../components/ExperienceProvider';
import HostWorkspace from '../../components/HostWorkspace';
import FocusedHostShell from '../../components/focused/HostShell';
import {isHostPath} from '../../lib/host-routes.mjs';
function HostNavigation({children}:{children:ReactNode}){useLocalizationRender();
 const path=usePathname(),{mode}=useExperience();
 const host=isHostPath(path);
 if(!host)return <>{children}</>;
 return <HostWorkspace>{mode==='FOCUSED'?<FocusedHostShell>{children}</FocusedHostShell>:children}</HostWorkspace>;
}
export default function HostGlobalNav({children}:{children:ReactNode}){useLocalizationRender();return <Suspense fallback={<main role="status">{localize("ui.ba3bbbe10d8bef66",{defaultValue:"Loading…"})}</main>}><HostNavigation>{children}</HostNavigation></Suspense>}
