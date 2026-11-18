'use client';
/* localization:instrumented */
import {activeLocale,localizeText} from '../lib/localization.mjs';
import {useLocalizationRender} from './LocalizationProvider';
import {trackedCopy} from '../lib/interaction-analytics.mjs';
import {useCallback,useEffect,useRef,useState} from 'react';
import {clearStoredAttempt,detectPlatform,isSafeUpiUri,parseUpiResponse,readStoredAttempt,screenshotProblem,storeAttempt,upiLaunchOptions,upiStatusCopy} from '../lib/upi-intent.mjs';
import {normaliseReference,receiptPath,referenceProblem} from '../lib/direct-transfer.mjs';
import {isNativeAppUserAgent} from '../lib/native-app.mjs';
import './upi-intent.css';

type Attempt={attemptId:string;status:string;amountPaise:number;paymentCode:string;transactionRef:string;payeeName:string;upiId:string|null;
  upiUri:string|null;qrPngBase64:string|null;expiresAt:string;appResponseStatus:string|null;suggestedReference:string|null;claimId:string|null;receiptToken:string|null;message:string|null};
type Phase='idle'|'ready'|'launched'|'report'|'review'|'reported'|'closed';
type Props={token:string;destinationId:string;giftId:string;amountPaise:number;validAmount:boolean;guestName:string;guestEmail:string;guestPhone:string;
  onUnavailable?:()=>void};

const money=(paise:number)=>new Intl.NumberFormat(activeLocale('en-IN'),{style:'currency',currency:'INR'}).format(paise/100);
const OPEN=['INITIATED','PENDING','FAILED'];
async function csrfHeaders(json=true):Promise<Record<string,string>>{
  await fetch('/backend/api/auth/csrf',{credentials:'same-origin',cache:'no-store'});
  const token=document.cookie.split('; ').find(c=>c.startsWith('XSRF-TOKEN='))?.split('=')[1];
  if(!token)throw new Error('Could not start a secure payment. Reload and try again.');
  return json?{'Content-Type':'application/json','X-XSRF-TOKEN':decodeURIComponent(token)}:{'X-XSRF-TOKEN':decodeURIComponent(token)};
}
async function call(url:string,init?:RequestInit):Promise<Attempt>{
  const response=await fetch(url,{credentials:'same-origin',cache:'no-store',...init});
  if(!response.ok){
    let message='';try{message=(await response.json())?.message||''}catch{/* not json */}
    const error=new Error(message||(response.status===429?'Too many attempts. Please wait a few minutes.':response.status===409?'This payment changed. Refresh and check its status.':response.status===410?'This payment link expired. Start a new payment.':'Payment service unavailable. Use the QR or UPI ID above.'));
    (error as Error&{status?:number}).status=response.status;throw error;
  }
  return response.json() as Promise<Attempt>;
}
function session():Storage|null{try{return window.sessionStorage}catch{return null}}

/** Mobile-first "Pay using UPI" flow. Money goes straight from the guest's UPI app to the host's own UPI ID. */
export default function UpiIntentPayment(p:Props){useLocalizationRender();
  const base=`/backend/api/public/payments/upi-intent/${encodeURIComponent(p.token)}/attempts`;
  const [platform,setPlatform]=useState<'ANDROID'|'IOS'|'DESKTOP'>('DESKTOP');
  const [attempt,setAttempt]=useState<Attempt|null>(null);
  const [phase,setPhase]=useState<Phase>('idle');
  const [busy,setBusy]=useState(false),[error,setError]=useState(''),[notice,setNotice]=useState('');
  const [launchStuck,setLaunchStuck]=useState(false);
  const [outcome,setOutcome]=useState<''|'COMPLETED'|'PENDING'|'FAILED'>('');
  const [reference,setReference]=useState(''),[name,setName]=useState(p.guestName);
  const [screenshot,setScreenshot]=useState<File|null>(null);
  const key=useRef('');const hiddenSinceLaunch=useRef(false);const stuckTimer=useRef<ReturnType<typeof setTimeout>|null>(null);
  useEffect(()=>{setPlatform(detectPlatform({userAgent:navigator.userAgent,maxTouchPoints:navigator.maxTouchPoints}))},[]);
  useEffect(()=>{if(!name)setName(p.guestName)},[p.guestName]);// eslint-disable-line react-hooks/exhaustive-deps

  // A guest coming back from the UPI app (or after the browser reloaded the tab) resumes the same attempt.
  useEffect(()=>{const stored=readStoredAttempt(session(),p.token);if(!stored)return;let active=true;
    call(`${base}/${stored.attemptId}`).then(a=>{if(!active)return;setAttempt(a);
      if(a.status==='PAYMENT_REPORTED'||!OPEN.includes(a.status)){setPhase(a.claimId?'reported':'closed');return}
      setPhase(stored.launched?'report':'ready');
    }).catch(()=>clearStoredAttempt(session(),p.token));
    return()=>{active=false};
  },[p.token]);// eslint-disable-line react-hooks/exhaustive-deps

  // Amount, gift or recipient changed before reporting: the old attempt no longer matches, start fresh.
  // (A resumed attempt keeps its own server-fixed amount even if this form was reset by a reload.)
  const startedFor=useRef('');const params=`${p.destinationId}|${p.giftId}|${p.amountPaise}`;
  useEffect(()=>{if(attempt&&OPEN.includes(attempt.status)&&startedFor.current&&startedFor.current!==params)reset()},[params]);// eslint-disable-line react-hooks/exhaustive-deps

  function reset(){startedFor.current='';setAttempt(null);setPhase('idle');setOutcome('');setReference('');setScreenshot(null);setError('');setNotice('');setLaunchStuck(false);key.current='';clearStoredAttempt(session(),p.token)}

  async function start(){
    if(busy||!p.validAmount||!p.destinationId)return;setBusy(true);setError('');setNotice('');
    try{if(!key.current)key.current=crypto.randomUUID();
      const a=await call(base,{method:'POST',headers:await csrfHeaders(),body:JSON.stringify({destinationId:p.destinationId,giftId:p.giftId||null,amountPaise:p.amountPaise,idempotencyKey:key.current,platform})});
      if(!a.upiUri||!isSafeUpiUri(a.upiUri))throw new Error('Payment link unavailable. Use the QR or UPI ID above.');
      startedFor.current=params;setAttempt(a);setPhase('ready');storeAttempt(session(),p.token,a.attemptId,false);
    }catch(e){const status=(e as Error&{status?:number}).status;if(status===503||status===404)p.onUnavailable?.();setError(e instanceof Error?e.message:'Payment unavailable')}
    finally{setBusy(false)}
  }

  // Leaving for the UPI app hides the page; coming back shows "Did you complete your payment?".
  const onVisibility=useCallback(()=>{
    if(document.visibilityState==='hidden'){hiddenSinceLaunch.current=true;if(stuckTimer.current)clearTimeout(stuckTimer.current);return}
    if(hiddenSinceLaunch.current){hiddenSinceLaunch.current=false;setLaunchStuck(false);setPhase(ph=>ph==='launched'?'report':ph)}
  },[]);
  useEffect(()=>{document.addEventListener('visibilitychange',onVisibility);return()=>{document.removeEventListener('visibilitychange',onVisibility);if(stuckTimer.current)clearTimeout(stuckTimer.current)}},[onVisibility]);
  function launched(){if(!attempt)return;hiddenSinceLaunch.current=false;setPhase('launched');setLaunchStuck(false);storeAttempt(session(),p.token,attempt.attemptId,true);
    if(stuckTimer.current)clearTimeout(stuckTimer.current);
    // Inside the GiftWise app the shell reports launch failure and return itself (see the listener below).
    if(isNativeAppUserAgent(navigator.userAgent))return;
    // Still visible after a few seconds: no UPI app took the link (none installed, or the browser blocked it).
    stuckTimer.current=setTimeout(()=>{if(!hiddenSinceLaunch.current&&document.visibilityState==='visible')setLaunchStuck(true)},2500);
  }

  // GiftWise mobile app: the shell signals when the guest returns from the UPI app, or when no app opened.
  useEffect(()=>{
    const resumed=()=>{if(stuckTimer.current)clearTimeout(stuckTimer.current);setLaunchStuck(false);setPhase(ph=>ph==='launched'?'report':ph)};
    const failed=()=>{if(stuckTimer.current)clearTimeout(stuckTimer.current);setLaunchStuck(true)};
    window.addEventListener('giftwise:app-resumed',resumed);window.addEventListener('giftwise:upi-launch-failed',failed);
    return()=>{window.removeEventListener('giftwise:app-resumed',resumed);window.removeEventListener('giftwise:upi-launch-failed',failed)};
  },[]);
  // Native shells that receive the UPI app's result dispatch it here. It is untrusted and only pre-fills the report.
  useEffect(()=>{if(!attempt)return;
    const handler=(event:Event)=>{const detail=(event as CustomEvent).detail as {response?:unknown;source?:unknown}|undefined;
      const raw=typeof detail?.response==='string'?detail.response:'';const preview=parseUpiResponse(raw);if(!preview)return;
      if(preview.utr)setReference(preview.utr);
      setOutcome(preview.status==='SUCCESS'?'COMPLETED':preview.status==='FAILURE'?'FAILED':'PENDING');setPhase('report');
      void csrfHeaders().then(headers=>call(`${base}/${attempt.attemptId}/response`,{method:'POST',headers,body:JSON.stringify({response:raw,source:typeof detail?.source==='string'?detail.source:null})}))
        .then(a=>{setAttempt(a);if(a.suggestedReference)setReference(r=>r||a.suggestedReference||'');if(a.message)setNotice(a.message)}).catch(()=>{/* manual report still works */});
    };
    window.addEventListener('giftwise:upi-response',handler);return()=>window.removeEventListener('giftwise:upi-response',handler);
  },[attempt?.attemptId]);// eslint-disable-line react-hooks/exhaustive-deps

  const cleanReference=normaliseReference('upi',reference);
  const referenceError=outcome==='COMPLETED'?referenceProblem('upi',cleanReference):'';
  const fileError=screenshotProblem(screenshot);
  const nameError=outcome==='COMPLETED'&&!name.trim()?'Enter your name so the host knows who sent this gift.':'';

  async function sendOutcome(choice:'PENDING'|'FAILED'){if(!attempt||busy)return;setBusy(true);setError('');
    try{const a=await call(`${base}/${attempt.attemptId}/report`,{method:'POST',headers:await csrfHeaders(),body:JSON.stringify({outcome:choice})});setAttempt(a);setNotice(a.message||'');
      if(choice==='FAILED'){clearStoredAttempt(session(),p.token)}
    }catch(e){setError(e instanceof Error?e.message:'Could not save')}finally{setBusy(false)}
  }
  async function submitReport(){if(!attempt||busy||referenceError||fileError||nameError)return;setBusy(true);setError('');
    try{const a=await call(`${base}/${attempt.attemptId}/report`,{method:'POST',headers:await csrfHeaders(),body:JSON.stringify({outcome:'COMPLETED',transactionReference:cleanReference,guestName:name.trim(),guestEmail:p.guestEmail.trim()||null,guestPhone:p.guestPhone.trim()||null})});
      setAttempt(a);setPhase('reported');setNotice(a.message||'');clearStoredAttempt(session(),p.token);
      if(screenshot){try{const form=new FormData();form.append('file',screenshot);const withShot=await call(`${base}/${attempt.attemptId}/evidence`,{method:'POST',headers:await csrfHeaders(false),body:form});setAttempt(withShot)}
        catch(e){setError((e instanceof Error?e.message:'')+' Your payment report was saved without the screenshot.')}}
    }catch(e){setError(e instanceof Error?e.message:'Could not report the payment')}finally{setBusy(false)}
  }
  async function refresh(){if(!attempt)return;try{setAttempt(await call(`${base}/${attempt.attemptId}`))}catch(e){setError(e instanceof Error?e.message:'Could not refresh')}}
  function copy(text:string,done:string){trackedCopy(text).then(()=>setNotice(done)).catch(()=>setNotice('Select the text to copy it.'))}

  const status=upiStatusCopy(attempt?.status??'');
  const options=attempt?.upiUri?upiLaunchOptions(attempt.upiUri,platform):[];
  const qr=attempt?.qrPngBase64?<figure className="upiIntentQr">
    {/* eslint-disable-next-line @next/next/no-img-element */}
    <img src={`data:image/png;base64,${attempt.qrPngBase64}`} alt={localizeText('UPI payment QR for this gift')} width={220} height={220}/>
    <figcaption>{localizeText('Scan with any UPI app. The amount and reference are filled in.')}</figcaption></figure>:null;
  const fallback=attempt&&<div className="upiIntentFallback">
    <p><strong>{localizeText('Pay another way')}</strong></p>
    {platform!=='DESKTOP'&&qr}
    {attempt.upiId&&<p>{localizeText('UPI ID:')} <strong>{attempt.upiId}</strong> <button type="button" onClick={()=>copy(attempt.upiId||'','UPI ID copied.')}>{localizeText('Copy UPI ID')}</button></p>}
    <p>{localizeText('Amount:')} <strong>{money(attempt.amountPaise)}</strong> · {localizeText('Note:')} <strong>GiftWise {attempt.paymentCode}</strong> <button type="button" onClick={()=>copy(`GiftWise ${attempt.paymentCode}`,'Note copied.')}>{localizeText('Copy note')}</button></p>
    <button type="button" className="upiIntentLink" onClick={()=>setPhase('report')}>{localizeText('I paid — report my payment')}</button>
  </div>;

  return <section className="upiIntent" aria-label={localizeText('Pay using UPI')} aria-live="polite">
    {attempt&&<p className={`upiIntentStatus tone-${status.tone}`}><span>{localizeText(status.label)}</span></p>}
    {phase==='idle'&&<div className="upiIntentStep">
      <p className="upiIntentHint">{localizeText('Your money goes straight from your UPI app to the host. GiftWise never receives it.')}</p>
      <button type="button" className="upiIntentPrimary" disabled={busy||!p.validAmount||!p.destinationId} onClick={()=>void start()}>
        {busy?localizeText('Preparing…'):platform==='DESKTOP'?localizeText('Show UPI payment QR'):`${localizeText('Pay')} ${p.validAmount?money(p.amountPaise):''} ${localizeText('using UPI')}`}</button>
      {!p.destinationId&&<p className="upiIntentHint">{localizeText('Waiting for the host’s UPI details…')}</p>}
    </div>}
    {(phase==='ready'||phase==='launched')&&attempt&&<div className="upiIntentStep">
      <dl className="upiIntentFacts"><dt>{localizeText('To')}</dt><dd>{attempt.payeeName}{attempt.upiId?` · ${attempt.upiId}`:''}</dd>
        <dt>{localizeText('Amount')}</dt><dd>{money(attempt.amountPaise)}</dd><dt>{localizeText('Reference')}</dt><dd>{attempt.paymentCode}</dd></dl>
      {platform==='DESKTOP'?<>{qr}<ol className="upiIntentSteps"><li>{localizeText('Open any UPI app on your phone and scan this QR.')}</li><li>{localizeText('Check the name and amount, then pay.')}</li><li>{localizeText('Come back here and report your payment with the 12-digit UPI reference.')}</li></ol>
          <button type="button" className="upiIntentPrimary" onClick={()=>setPhase('report')}>{localizeText('I’ve paid — report payment')}</button></>
        :<>{options.map((o,i)=><a key={o.id} className={i===0||platform==='ANDROID'?'upiIntentPrimary':'upiIntentSecondary'} href={o.href} onClick={launched} rel="noopener">{localizeText(o.label)}</a>)}
          {platform==='IOS'&&<p className="upiIntentHint">{localizeText('After paying, switch back to this browser yourself. Some UPI apps do not return automatically.')}</p>}
          {platform==='ANDROID'&&<p className="upiIntentHint">{localizeText('Choose your UPI app. After paying, come back to this page.')}</p>}
          {phase==='launched'&&<button type="button" className="upiIntentLink" onClick={()=>setPhase('report')}>{localizeText('I’m back — report my payment')}</button>}
          {(launchStuck||phase==='launched')&&<details className="upiIntentDetails" open={launchStuck}><summary>{launchStuck?localizeText('UPI app didn’t open?'):localizeText('Having trouble?')}</summary>{fallback}</details>}</>}
      <button type="button" className="upiIntentLink" onClick={reset}>{localizeText('Cancel')}</button>
    </div>}
    {phase==='report'&&attempt&&OPEN.includes(attempt.status)&&<div className="upiIntentStep">
      <h4>{localizeText('Did you complete your payment?')}</h4>
      <p className="upiIntentHint">{money(attempt.amountPaise)} · {attempt.paymentCode}</p>
      <div className="upiIntentChoices" role="radiogroup" aria-label={localizeText('Payment outcome')}>
        {([['COMPLETED','Yes, payment completed'],['PENDING','Payment pending'],['FAILED','Payment failed']] as const).map(([value,label])=>
          <label key={value} className={outcome===value?'selected':''}><input type="radio" name={`upi-outcome-${attempt.attemptId}`} value={value} checked={outcome===value} onChange={()=>{setOutcome(value);setError('');setNotice('')}}/>{localizeText(label)}</label>)}
      </div>
      {outcome==='COMPLETED'&&<>
        <label className="upiIntentField">{localizeText('UPI reference (UTR / RRN) · 12 digits')}
          <input inputMode="numeric" autoComplete="off" maxLength={40} placeholder="e.g. 412345678901" value={reference} onChange={e=>{setReference(e.target.value);setError('')}} aria-invalid={!!reference&&!!referenceError}/>
          <small>{reference&&referenceError?localizeText(referenceError):localizeText('Find it in your UPI app under the payment details. Pasted text is fine; we pick out the 12 digits.')}</small></label>
        <label className="upiIntentField">{localizeText('Your name')}<input autoComplete="name" maxLength={200} value={name} onChange={e=>setName(e.target.value)}/>{nameError&&<small role="alert">{localizeText(nameError)}</small>}</label>
        <label className="upiIntentField">{localizeText('Payment screenshot (optional)')}<input type="file" accept="image/png,image/jpeg" onChange={e=>setScreenshot(e.target.files?.[0]??null)}/>
          <small>{fileError?localizeText(fileError):localizeText('Helps the host find your payment. It is not proof of payment.')}</small></label>
        <button type="button" className="upiIntentPrimary" disabled={!!referenceError||!!fileError||!!nameError} onClick={()=>setPhase('review')}>{localizeText('Review details')}</button></>}
      {outcome==='PENDING'&&<button type="button" className="upiIntentSecondary" disabled={busy} onClick={()=>void sendOutcome('PENDING')}>{localizeText('Save as pending')}</button>}
      {outcome==='FAILED'&&<button type="button" className="upiIntentSecondary" disabled={busy} onClick={()=>void sendOutcome('FAILED')}>{localizeText('Confirm payment failed')}</button>}
      {attempt.status==='FAILED'&&<button type="button" className="upiIntentLink" onClick={reset}>{localizeText('Try again')}</button>}
      {attempt.status==='PENDING'&&<button type="button" className="upiIntentLink" onClick={()=>setPhase('ready')}>{localizeText('Open the UPI app again')}</button>}
    </div>}
    {phase==='review'&&attempt&&<div className="upiIntentStep">
      <h4>{localizeText('Check your payment details')}</h4>
      <dl className="upiIntentFacts"><dt>{localizeText('Amount')}</dt><dd>{money(attempt.amountPaise)}</dd><dt>{localizeText('To')}</dt><dd>{attempt.payeeName}</dd>
        <dt>{localizeText('UPI reference')}</dt><dd>{cleanReference}</dd><dt>{localizeText('From')}</dt><dd>{name.trim()}</dd>
        <dt>{localizeText('Screenshot')}</dt><dd>{screenshot?screenshot.name:localizeText('None')}</dd></dl>
      <button type="button" className="upiIntentPrimary" disabled={busy} onClick={()=>void submitReport()}>{busy?localizeText('Sending…'):localizeText('Submit payment report')}</button>
      <button type="button" className="upiIntentSecondary" disabled={busy} onClick={()=>setPhase('report')}>{localizeText('Edit details')}</button>
    </div>}
    {phase==='reported'&&attempt&&<div className="upiIntentStep">
      <h4>{localizeText('Your payment has been reported. The host will verify receipt.')}</h4>
      <p className="upiIntentHint">{localizeText(status.next)}</p>
      <dl className="upiIntentFacts"><dt>{localizeText('Amount')}</dt><dd>{money(attempt.amountPaise)}</dd><dt>{localizeText('Reference')}</dt><dd>{attempt.paymentCode}</dd></dl>
      {attempt.receiptToken&&receiptPath(attempt.receiptToken)&&<a className="upiIntentSecondary" href={receiptPath(attempt.receiptToken)!}>{localizeText('View your receipt')}</a>}
      <button type="button" className="upiIntentLink" onClick={()=>void refresh()}>{localizeText('Check status')}</button>
      <button type="button" className="upiIntentLink" onClick={reset}>{localizeText('Send another gift')}</button>
    </div>}
    {phase==='closed'&&attempt&&<div className="upiIntentStep"><p className="upiIntentHint">{localizeText(status.next)}</p><button type="button" className="upiIntentPrimary" onClick={reset}>{localizeText('Start a new payment')}</button></div>}
    {notice&&<p role="status" className="upiIntentHint">{localizeText(notice)}</p>}
    {error&&<p role="alert" className="upiIntentError">{localizeText(error)}</p>}
  </section>;
}
