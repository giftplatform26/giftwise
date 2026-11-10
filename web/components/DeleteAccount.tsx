'use client';
import {useState} from 'react';
import Link from 'next/link';
import {localizeText} from '../lib/localization.mjs';
import {useLocalizationRender} from './LocalizationProvider';
import {hostApi} from '../lib/host-api.mjs';
import {saveFlash} from '../lib/flash.mjs';

/** Self-service account deletion (also required for Google Play distribution). */
export default function DeleteAccount(){useLocalizationRender();
 const [open,setOpen]=useState(false),[password,setPassword]=useState(''),[typed,setTyped]=useState('');
 const [busy,setBusy]=useState(false),[error,setError]=useState('');
 const ready=password.length>0&&typed==='DELETE'&&!busy;

 async function remove(e:React.FormEvent){e.preventDefault();if(!ready)return;setBusy(true);setError('');
  try{await hostApi.deleteAccount(fetch,password);saveFlash(window.sessionStorage,'info','Your account has been deleted. Thank you for celebrating with us.');window.location.assign('/login');}
  catch(err){setError(err instanceof Error?err.message:'Your account could not be deleted. Try again.');setBusy(false);}
 }

 return <section className="focusPanel" aria-labelledby="delete-account-heading">
  <h2 id="delete-account-heading">{localizeText('Delete account')}</h2>
  {!open?<>
   <p>{localizeText('Permanently remove your personal details and close your GiftWise account.')}</p>
   <button type="button" onClick={()=>setOpen(true)}>{localizeText('Delete my account…')}</button>
  </>:<form className="focusForm" onSubmit={remove}>
   <p><strong>{localizeText('This cannot be undone.')}</strong></p>
   <ul>
    <li>{localizeText('Your name, email, mobile number, username, profile picture and preferences are removed, and you are signed out on every device.')}</li>
    <li>{localizeText('Draft celebrations are cancelled and your receiving accounts are disabled.')}</li>
    <li>{localizeText('Records of contributions and payments are kept in anonymised form where the law requires it.')}</li>
    <li>{localizeText('Live celebrations must be completed or cancelled first.')} <Link href="/events">{localizeText('Review celebrations')}</Link></li>
   </ul>
   {error&&<p role="alert" className="focusError">{localizeText(error)}</p>}
   <label>{localizeText('Your password')}<input type="password" autoComplete="current-password" value={password} onChange={e=>{setPassword(e.target.value);setError('')}} required/></label>
   <label>{localizeText('Type DELETE to confirm')}<input value={typed} onChange={e=>setTyped(e.target.value.trim())} autoComplete="off" autoCapitalize="characters" spellCheck={false} required/></label>
   <div style={{display:'flex',gap:12,flexWrap:'wrap'}}>
    <button type="submit" className="focusPrimary" disabled={!ready} style={{background:'#9c3028',borderColor:'#9c3028'}}>{busy?localizeText('Deleting…'):localizeText('Permanently delete account')}</button>
    <button type="button" disabled={busy} onClick={()=>{setOpen(false);setPassword('');setTyped('');setError('')}}>{localizeText('Keep my account')}</button>
   </div>
  </form>}
 </section>;
}
