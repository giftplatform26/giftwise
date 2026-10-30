import type { Metadata } from 'next';
import './style.css';
import '../components/focused/focused.css';
import '../components/invitation-editor-toolbar.css';
import '../components/ai-invitation.css';
import ExperienceProvider from '../components/ExperienceProvider';
import PerformanceDiagnostics from './components/PerformanceDiagnostics';
import HostSessionRedirect from './components/HostSessionRedirect';
import HostGlobalNav from './components/HostGlobalNav';
export const metadata: Metadata = { title: 'Gift Platform | Invitation Studio', description: 'Design beautiful gift invitation cards' };
export default function RootLayout({children}:{children:React.ReactNode}) {return <html lang="en"><body><HostSessionRedirect/><ExperienceProvider><PerformanceDiagnostics/><HostGlobalNav>{children}</HostGlobalNav></ExperienceProvider></body></html>}
