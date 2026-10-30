'use client';
import {useState} from 'react';
type Props={token:string;groupId:string};
type Started={challengeId:string;message:string};
function csrf(){return decodeURIComponent(document.cookie.split('; ').find(x=>x.startsWith('XSRF-TOKEN='))?.split('=').slice(1).join('=')||'');}
export default function GroupMobileVerification({token,groupId}:Props){
 const [mobile,setMobile]=useState(''),[code,setCode]=useState(''),[challenge,setChallenge]=useState(''),[busy,setBusy]=useState(false),[message,setMessage]=useState('');
 async function submit(path:string,body:unknown){
  await fetch('/backend/api/auth/csrf',{credentials:'include'});
  const response=await fetch(`/backend/api/public/groups/${encodeURIComponent(token)}/contact-mobile/${path}`,{
   method:'POST',credentials:'include',cache:'no-store',
   headers:{'Content-Type':'application/json','X-XSRF-TOKEN':csrf()},
   body:JSON.stringify(body)
  });
  const result=await response.json().catch(()=>({}));
  if(!response.ok)throw new Error(typeof result.detail==='string'?result.detail:`Mobile OTP unavailable (${response.status})`);
  return result;
 }
 async function start(e:React.FormEvent){
  e.preventDefault();setBusy(true);setMessage('');
  try{const result=await submit('start',{mobile,groupId}) as Started;setChallenge(result.challengeId);setMessage(result.message);}
  catch(error){setMessage(error instanceof Error?error.message:'Unable to start mobile test');}
  finally{setBusy(false);}
 }
 async function complete(e:React.FormEvent){
  e.preventDefault();setBusy(true);setMessage('');
  try{const result=await submit('complete',{challengeId:challenge,code,groupId}) as {message:string};setMessage(result.message);setChallenge('');setCode('');}
  catch(error){setMessage(error instanceof Error?error.message:'Invalid or expired code');}
  finally{setBusy(false);}
 }
 return <section aria-label="Mobile OTP development test" style={{border:'1px solid #d5d5d5',padding:16,borderRadius:12,margin:'16px 0'}}>
  <h3>Mobile OTP (local development only)</h3>
  <p>This test uses a local SMS inbox, not a real SMS. Completing it does not verify ownership of your mobile number or enable production account recovery.</p>
  {!challenge?<form onSubmit={start}><label>Mobile number (international format)
    <input type="tel" required placeholder="+919876543210" pattern="\\+[1-9][0-9]{7,14}" value={mobile} onChange={e=>setMobile(e.target.value)} />
   </label><button type="submit" disabled={busy}>{busy?'Sending…':'Send local test OTP'}</button></form>:
   <form onSubmit={complete}><label>Six-digit test code
     <input inputMode="numeric" autoComplete="one-time-code" pattern="[0-9]{6}" maxLength={6} required value={code} onChange={e=>setCode(e.target.value)}/>
    </label><button type="submit" disabled={busy}>{busy?'Checking…':'Check local OTP'}</button>
    <button type="button" onClick={()=>{setChallenge('');setCode('');setMessage('')}}>Cancel</button></form>}
  {message&&<p role="status">{message}</p>}
 </section>;
}
