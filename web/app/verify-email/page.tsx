'use client';
import {useVerification} from '../../components/VerificationProvider';
import {useEffect,useState} from 'react';
import Link from 'next/link';
import {Mail,ArrowLeft,CheckCircle2} from 'lucide-react';
import '../forgot-username/recovery.css';
type Account={email:string;role:'CUSTOMER'|'PLATFORM_ADMIN'};
function csrfCookie(){return decodeURIComponent(document.cookie.split('; ').find(x=>x.startsWith('XSRF-TOKEN='))?.slice(11)||'');}
export default function VerifyEmail(){
 const {emailVerificationEnabled}=useVerification();
 const [account,setAccount]=useState<Account|null>(null),[session,setSession]=useState<'checking'|'ready'|'required'|'error'>('checking'),[retry,setRetry]=useState(0);
 const [challengeId,setId]=useState(''),[code,setCode]=useState(''),[message,setMessage]=useState(''),[error,setError]=useState(''),[busy,setBusy]=useState(false),[verified,setVerified]=useState(false);
 useEffect(()=>{const controller=new AbortController();setSession('checking');setError('');fetch('/backend/api/auth/me',{credentials:'include',cache:'no-store',signal:controller.signal}).then(async response=>{
  if(response.status===401){setAccount(null);setSession('required');return;}
  if(!response.ok)throw Error('Could not check your sign-in session. Please try again.');
  const user=await response.json();if(user.role!=='CUSTOMER'&&user.role!=='PLATFORM_ADMIN')throw Error('Sign in as an event host or platform administrator to verify your email.');
  if(!controller.signal.aborted){setAccount(user);setSession('ready');}
 }).catch(e=>{if(!controller.signal.aborted){setSession('error');setError(e instanceof Error?e.message:'Could not check your session.')}});return()=>controller.abort()},[retry]);
 function expired(){setAccount(null);setSession('required');setId('');setCode('');setMessage('');}
 async function post(path:string,body:unknown){
  const init=await fetch('/backend/api/auth/csrf',{credentials:'include',cache:'no-store'});
  if(init.status===401){expired();throw Error('Your session has expired. Sign in again to continue.');}
  if(!init.ok)throw Error('Could not initialize a secure request. Please try again.');
  const response=await fetch('/backend/api/auth/recovery/'+path,{method:'POST',credentials:'include',cache:'no-store',headers:{'Content-Type':'application/json','X-XSRF-TOKEN':csrfCookie()},body:JSON.stringify(body)});
  const data=await response.json().catch(()=>({}));
  if(response.status===401){expired();throw Error('Your session has expired. Sign in again to continue.');}
  if(!response.ok)throw Error(response.status===403?'Your secure session could not be verified. Refresh this page and try again.':response.status===429?'Too many verification requests. Please try again later.':data.detail||data.message||(response.status===503?'Email verification is not configured. Please contact support.':'Unable to verify email. Please try again.'));
  return data;
 }
 async function request(e:React.FormEvent){e.preventDefault();if(!account||busy)return;setBusy(true);setMessage('');setError('');try{const result=await post('enroll-email',{});setId(result.challengeId);setMessage(result.message)}catch(e){setError(e instanceof Error?e.message:'Unable to request code')}finally{setBusy(false)}}
 async function verify(e:React.FormEvent){e.preventDefault();if(!account||busy)return;setBusy(true);setMessage('');setError('');try{const result=await post('verify-email',{challengeId,code});setMessage(result.message);setId('');setCode('');setVerified(true)}catch(e){setError(e instanceof Error?e.message:'Unable to verify email')}finally{setBusy(false)}}
 const dashboard=account?.role==='PLATFORM_ADMIN'?'/admin':'/events';
 if(!emailVerificationEnabled)return <main className="hostShell"><h1>Account help</h1><p>Use your existing username and password to sign in. Contact support if you need help accessing your account.</p><Link href={dashboard}>Continue to your workspace</Link></main>;
 return <main className="username-recovery"><Link className="username-back" href={account?dashboard:'/login?next=%2Fverify-email'}><ArrowLeft size={16}/> {account?'Back to your workspace':'Back to sign in'}</Link><section className="username-card"><span className="username-icon">{verified?<CheckCircle2 size={25}/>:<Mail size={25}/>}</span><p className="username-kicker">SECURE YOUR ACCOUNT</p><h1>{verified?'Email verified':'Verify your email'}</h1>{session==='checking'?<p role="status">Checking your sign-in session…</p>:session==='required'?<><p>Sign in using your registered email and password first. You’ll return here automatically to verify your email.</p><Link className="verification-sign-in" href="/login?next=%2Fverify-email">Sign in to verify email →</Link></>:session==='error'?<button type="button" onClick={()=>setRetry(v=>v+1)}>Check session again</button>:verified?<><p>Your email can now be used for account recovery.</p><Link href={`/forgot-username?kind=${account?.role==='PLATFORM_ADMIN'?'PLATFORM_ADMIN':'HOST'}&returnTo=%2Flogin`}>Request a username reminder</Link></>:<><p>Confirm ownership of <strong>{account?.email}</strong> to enable account recovery.</p>{!challengeId?<form onSubmit={request}><button type="submit" disabled={busy}>{busy?'Sending request…':'Send email verification code'}</button></form>:<form onSubmit={verify}><label>Six-digit code<input required pattern="[0-9]{6}" inputMode="numeric" autoComplete="one-time-code" maxLength={6} value={code} onChange={e=>setCode(e.target.value)}/></label><button type="submit" disabled={busy}>{busy?'Verifying…':'Verify email'}</button></form>}</>}{error&&<p className="username-error" role="alert">{error}</p>}{message&&<p role="status">{message}</p>}</section></main>;
}
