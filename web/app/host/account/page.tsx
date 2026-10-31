'use client';
import GroupMemberEmailPolicy from '../../../components/GroupMemberEmailPolicy';
import AccountEmailVerification from '../../../components/AccountEmailVerification';
import {useEffect,useState} from 'react';
import Link from 'next/link';
import {useRouter} from 'next/navigation';
import {hostApi} from '../../../lib/host-api.mjs';
function AccountContent(){
 const router=useRouter();
 const [name,setName]=useState(''),[message,setMessage]=useState(''),[busy,setBusy]=useState(false);
 useEffect(()=>{hostApi.profileName(fetch).then(v=>setName(v.fullName||'')).catch(()=>setMessage('Please sign in to manage your account.'))},[]);
 async function save(e:React.FormEvent){e.preventDefault();setBusy(true);try{await hostApi.setProfileName(fetch,name);setMessage('Your name is saved.')}catch{setMessage('Your name could not be saved. Try again.')}finally{setBusy(false)}}
 async function signOut(){setBusy(true);try{await hostApi.logout(fetch);router.replace('/login')}catch{setMessage('Could not sign out. Try again.');setBusy(false)}}
 return <main className="focusPage"><header className="focusPageHeading"><div><p className="focusEyebrow">Your details</p><h1>Account</h1></div></header><section className="focusPanel"><h2>What should we call you?</h2><form className="focusForm" onSubmit={save}><label>Full name<input required minLength={2} maxLength={120} value={name} onChange={e=>setName(e.target.value)}/></label><button className="focusPrimary" disabled={busy}>{busy?'Saving…':'Save name'}</button></form></section><AccountEmailVerification/><GroupMemberEmailPolicy/><section className="focusPanel focusAccountLinks"><Link href="/host/payment-settings">Receiving accounts <span>→</span></Link><Link href="/forgot-password">Password & recovery <span>→</span></Link><button disabled={busy} onClick={()=>void signOut()}>Sign out</button></section>{message&&<p role="status">{message}</p>}</main>;
}

export default function Account(){return <AccountContent/>}
