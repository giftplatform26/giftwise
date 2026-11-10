'use client';
/* localization:instrumented */
import {localize,localizeText} from '../../../lib/localization.mjs';
import {useLocalizationRender} from '../../../components/LocalizationProvider';

import {PreferredLanguage} from '../../../components/LocalizationProvider';
import GroupMemberEmailPolicy from '../../../components/GroupMemberEmailPolicy';
import {useVerification} from '../../../components/VerificationProvider';
import MobileVerification from '../../../components/MobileVerification';
import AccountEmailVerification from '../../../components/AccountEmailVerification';
import {useEffect,useState} from 'react';
import Link from 'next/link';
import {useRouter} from 'next/navigation';
import {hostApi} from '../../../lib/host-api.mjs';
function AccountContent(){useLocalizationRender();
 const {emailVerificationEnabled}=useVerification();
 const router=useRouter();
 const [name,setName]=useState(''),[message,setMessage]=useState(''),[busy,setBusy]=useState(false);
 useEffect(()=>{hostApi.profileName(fetch).then(v=>setName(v.fullName||'')).catch(()=>setMessage('Please sign in to manage your account.'))},[]);
 async function save(e:React.FormEvent){e.preventDefault();setBusy(true);try{await hostApi.setProfileName(fetch,name);setMessage('Your name is saved.')}catch{setMessage('Your name could not be saved. Try again.')}finally{setBusy(false)}}
 async function signOut(){setBusy(true);try{await hostApi.logout(fetch);router.replace('/login')}catch{setMessage('Could not sign out. Try again.');setBusy(false)}}
 return <main className="focusPage"><header className="focusPageHeading"><div><p className="focusEyebrow">{localize("ui.c0bcd4c0f8e70784",{defaultValue:"Your details"})}</p><h1>{localize("ui.7e1b0d5641f2640c",{defaultValue:"Account"})}</h1></div></header><section className="focusPanel"><h2>{localize("ui.d82e5fe24eed3db4",{defaultValue:"What should we call you?"})}</h2><form className="focusForm" onSubmit={save}><label>{localize("ui.f13a64ba2fea87ef",{defaultValue:"Full name"})}<input required minLength={2} maxLength={120} value={name} onChange={e=>setName(e.target.value)}/></label><button className="focusPrimary" disabled={busy}>{busy?localize("ui.23e39291d6135814",{defaultValue:"Saving…"}):localize("ui.b7297226fd1f4681",{defaultValue:"Save name"})}</button></form></section><PreferredLanguage/><AccountEmailVerification/><MobileVerification/><GroupMemberEmailPolicy/><section className="focusPanel focusAccountLinks"><Link href="/host/payment-settings">{localize("ui.6615aff4b6ec7d9a",{defaultValue:"Receiving accounts "})}<span>→</span></Link>{emailVerificationEnabled&&<Link href="/forgot-password">{localize("ui.78576ea18e04a0e0",{defaultValue:"Password & recovery "})}<span>→</span></Link>}<button disabled={busy} onClick={()=>void signOut()}>{localize("ui.48f0d3d397d49f13",{defaultValue:"Sign out"})}</button></section>{message&&<p role="status">{localizeText(message)}</p>}</main>;
}

export default function Account(){useLocalizationRender();return <AccountContent/>}
