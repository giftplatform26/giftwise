import ApplicationInteractions from './components/ApplicationInteractions';
import LocalizationProvider from '../components/LocalizationProvider';
import type { Metadata } from 'next';
import './style.css';
import './giftwise.css';
import '../components/focused/focused.css';
import '../components/invitation-editor-toolbar.css';
import '../components/ai-invitation.css';
import '../components/invitation-studio.css';
import VerificationProvider from '../components/VerificationProvider';
import ExperienceProvider from '../components/ExperienceProvider';
import PerformanceDiagnostics from './components/PerformanceDiagnostics';
import HostSessionRedirect from './components/HostSessionRedirect';
import HostGlobalNav from './components/HostGlobalNav';
export const metadata: Metadata = { title: 'GiftWise | Celebrations, thoughtfully together', description: 'Your celebrations, wishes, group gifts, invitations, and memories in one thoughtful place.' };
export default function RootLayout({children}:{children:React.ReactNode}) {return <html lang="en"><body className="giftwise"><LocalizationProvider><ApplicationInteractions/><HostSessionRedirect/><ExperienceProvider><VerificationProvider><PerformanceDiagnostics/><HostGlobalNav>{children}</HostGlobalNav></VerificationProvider></ExperienceProvider></LocalizationProvider></body></html>}
