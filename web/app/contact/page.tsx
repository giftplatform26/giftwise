'use client';
import {useBranding} from '../../components/BrandingProvider';
import Link from 'next/link';
export default function Contact(){const {brand}=useBranding();return <main style={{maxWidth:760,margin:'3rem auto',padding:24}}><h1>Contact {brand.applicationName}</h1><p>For event or gift questions, contact your host using the contact details on your invitation.</p><p>For payment issues, keep your transaction reference and use your payment provider’s support channel. Never share passwords, security codes or full card details.</p>{brand.supportEmail?<p>Email support: <a href={'mailto:'+brand.supportEmail}>{brand.supportEmail}</a></p>:<p>Platform support contact details will be published here when configured by the operator.</p>}<Link href="/">Return to GiftWise</Link></main>}
