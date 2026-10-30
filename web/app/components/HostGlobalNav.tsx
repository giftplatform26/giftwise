'use client';
import {Suspense,type ReactNode} from 'react';
import {usePathname} from 'next/navigation';
import {useExperience} from '../../components/ExperienceProvider';
import HostWorkspace from '../../components/HostWorkspace';
import FocusedHostShell from '../../components/focused/HostShell';
function HostNavigation({children}:{children:ReactNode}){
 const path=usePathname(),{mode}=useExperience();
 const host=path==='/'||path==='/dashboard'||path==='/events'||path.startsWith('/events/')||path==='/gifts'||path.startsWith('/host/');
 if(!host)return <>{children}</>;
 return <HostWorkspace>{mode==='FOCUSED'?<FocusedHostShell>{children}</FocusedHostShell>:children}</HostWorkspace>;
}
export default function HostGlobalNav({children}:{children:ReactNode}){return <Suspense fallback={<main role="status">Opening your workspace…</main>}><HostNavigation>{children}</HostNavigation></Suspense>}
