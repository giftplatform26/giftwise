'use client';
import {BrandLogo} from '../../components/BrandingProvider';
/* localization:instrumented */
import {localize,localizeText} from '../../lib/localization.mjs';
import {useLocalizationRender} from '../../components/LocalizationProvider';

import CredentialRequirements from '../../components/CredentialRequirements';
import {useVerification} from '../../components/VerificationProvider';
import {useExperience} from '../../components/ExperienceProvider';
import {useEffect,useRef,useState} from 'react';import {rememberReferral} from '../../lib/reminders.mjs';import Link from 'next/link';import {useRouter} from 'next/navigation';import {hostApi} from '../../lib/host-api.mjs';import '../host.css';import './register.css';import {saveFlash} from '../../lib/flash.mjs';import HostToast from '../components/HostToast';
import {Eye,EyeOff,CheckCircle2,AlertCircle,Loader2} from 'lucide-react';
import {detailProblems,securityProblems,firstProblemField,availabilityProblems,registrationErrorField,displayMobile,USERNAME_PATTERN,EMAIL_PATTERN} from '../../lib/signup-form.mjs';

type Field='fullName'|'username'|'email'|'mobile'|'password'|'confirmPassword'|'terms';
type Problems=Partial<Record<Field,string>>;
type Availability={available:boolean;message:string};
const DETAILS:Field[]=['fullName','username','email','mobile'];

/**
 * Host sign-up. Username and email are checked against existing accounts on the first step (as you leave each
 * field, and again on Continue), so nobody reaches the password step only to be told their email is taken.
 * Errors appear next to the field they belong to, and focus moves to the first field that needs attention.
 */
export default function Register(){useLocalizationRender();
 useEffect(()=>{try{rememberReferral(window.location.search,localStorage)}catch{}},[]);
 const {emailVerificationEnabled}=useVerification();const {mode}=useExperience();const stepped=mode==='FOCUSED';
 const router=useRouter();
 const [step,setStep]=useState(0);
 const [fullName,setFullName]=useState(''),[username,setUsername]=useState(''),[email,setEmail]=useState(''),[mobile,setMobile]=useState(''),[password,setPassword]=useState(''),[termsAccepted,setTermsAccepted]=useState(false);
 const [showPassword,setShowPassword]=useState(false),[confirmPassword,setConfirmPassword]=useState('');
 const [problems,setProblems]=useState<Problems>({});
 const [available,setAvailable]=useState<Partial<Record<'username'|'email',Availability&{value:string}>>>({});
 const [checking,setChecking]=useState<Partial<Record<'username'|'email',boolean>>>({});
 const [busy,setBusy]=useState(false),[error,setError]=useState('');
 const refs={fullName:useRef<HTMLInputElement>(null),username:useRef<HTMLInputElement>(null),email:useRef<HTMLInputElement>(null),mobile:useRef<HTMLInputElement>(null),password:useRef<HTMLInputElement>(null),confirmPassword:useRef<HTMLInputElement>(null),terms:useRef<HTMLInputElement>(null)};
 const stepHeading=useRef<HTMLHeadingElement>(null);
 const pendingFocus=useRef<Field|'step'|null>(null);
 useEffect(()=>{const target=pendingFocus.current;if(!target)return;pendingFocus.current=null;(target==='step'?stepHeading.current:refs[target].current)?.focus()});

 function focusLater(target:Field|'step'){pendingFocus.current=target}
 function edit(field:Field,set:(v:string)=>void){return (e:React.ChangeEvent<HTMLInputElement>)=>{set(e.target.value);if(problems[field])setProblems(p=>({...p,[field]:undefined}));if(field==='username'||field==='email')setAvailable(a=>({...a,[field]:undefined}));setError('')}}

 /** Ask the server whether username/email are free. Network trouble or rate limiting never blocks the person: the final submit re-checks. */
 async function check(fields:{username?:string;email?:string}):Promise<Problems>{
  const keys=(Object.keys(fields) as ('username'|'email')[]).filter(k=>fields[k]);if(!keys.length)return {};
  setChecking(c=>({...c,...Object.fromEntries(keys.map(k=>[k,true]))}));
  try{const result=await hostApi.registerAvailability(fetch,{username:fields.username??"",email:fields.email??""}) as Record<string,Availability>;
   setAvailable(a=>({...a,...Object.fromEntries(keys.filter(k=>result?.[k]).map(k=>[k,{...result[k],value:String(fields[k])}]))}));
   return availabilityProblems(result) as Problems;
  }catch{return {}}
  finally{setChecking(c=>({...c,...Object.fromEntries(keys.map(k=>[k,false]))}))}
 }
 async function checkOnBlur(field:'username'|'email'){
  const value=field==='email'?email.trim():username;
  const valid=field==='email'?EMAIL_PATTERN.test(value):USERNAME_PATTERN.test(value);
  if(!valid){if(value)setProblems(p=>({...p,[field]:(detailProblems({fullName,username,email,mobile}) as Problems)[field]}));return}
  if(available[field]?.value===value)return;
  const found=await check({[field]:value});
  if(found[field])setProblems(p=>({...p,[field]:found[field]}));
 }

 async function continueToSecurity(){
  const local=detailProblems({fullName,username,email,mobile}) as Problems;
  if(Object.keys(local).length){setProblems(local);focusLater(firstProblemField(local,DETAILS) as Field);return false}
  const taken=await check({username,email:email.trim()});
  if(Object.keys(taken).length){setProblems(taken);focusLater(firstProblemField(taken,DETAILS) as Field);return false}
  return true;
 }

 async function submit(e:React.FormEvent){e.preventDefault();if(busy)return;setError('');
  if(stepped&&step===0){setBusy(true);try{if(await continueToSecurity()){setStep(1);setProblems({});focusLater('step')}}finally{setBusy(false)}return}
  const details=detailProblems({fullName,username,email,mobile}) as Problems;
  if(Object.keys(details).length){setProblems(details);if(stepped)setStep(0);focusLater(firstProblemField(details,DETAILS) as Field);return}
  const security=securityProblems({password,confirmPassword,termsAccepted}) as Problems;
  if(Object.keys(security).length){setProblems(security);focusLater(firstProblemField(security,['password','confirmPassword','terms']) as Field);return}
  setBusy(true);
  try{
   if(!stepped){const taken=await check({username,email:email.trim()});if(Object.keys(taken).length){setProblems(taken);focusLater(firstProblemField(taken,DETAILS) as Field);return}}
   await hostApi.register(fetch,{fullName,username,email:email.trim(),mobile,password,termsAccepted});
   saveFlash(window.sessionStorage,'success',emailVerificationEnabled?'Account created. Sign in and verify your email under Account.':'Account created. Please sign in to continue.');
   try{window.sessionStorage.setItem('gift.registration.identifier',email.trim())}catch{}
   const next=new URLSearchParams(window.location.search).get('next');router.push(next?`/login?next=${encodeURIComponent(next)}`:'/login');
  }catch(err){
   const message=err instanceof Error?err.message:'We couldn’t create your account. Please try again.';
   const field=registrationErrorField(message) as Field|null;
   if(field){setProblems({[field]:message});if(stepped&&DETAILS.includes(field))setStep(0);focusLater(field)}else setError(message);
  }finally{setBusy(false)}
 }

 function rememberEmailForSignIn(){try{window.sessionStorage.setItem('gift.registration.identifier',email.trim())}catch{}}
 const describe=(field:Field,extra?:string)=>[problems[field]?`${field}-problem`:'',extra||''].filter(Boolean).join(' ')||undefined;
 const problem=(field:Field)=>problems[field]?<span id={`${field}-problem`} className="suProblem" role="alert"><AlertCircle size={16} aria-hidden="true"/><span>{localizeText(problems[field]!)}{field==='email'&&/already/i.test(problems[field]!)&&<> <Link href="/login" onClick={rememberEmailForSignIn}>{localizeText('Sign in')}</Link> · <Link href="/forgot-password">{localizeText('Forgot password?')}</Link></>}</span></span>:null;
 const status=(field:'username'|'email',value:string)=>checking[field]?<span className="suStatus" aria-live="polite"><Loader2 size={15} className="suSpin" aria-hidden="true"/>{localizeText('Checking…')}</span>:!problems[field]&&available[field]?.available&&available[field]?.value===value?<span className="suStatus suOk" aria-live="polite"><CheckCircle2 size={15} aria-hidden="true"/>{localizeText(field==='email'?'Email is available.':'Username is available.')}</span>:null;
 const showDetails=!stepped||step===0,showSecurity=!stepped||step===1;
 const mobilePreview=displayMobile(mobile);

 return <main className="hostShell"><HostToast/><div className="hostAuth signupAuth"><Link href="/" className="hostLogo"><BrandLogo size={48}/></Link><div className="hostKicker">{localize("ui.113e7bbd3292e5b1",{defaultValue:"START CELEBRATING"})}</div><h1>{localize("ui.b45d227f5e9a8289",{defaultValue:"Create your host account."})}</h1><p>{localize("ui.c98043f9192d6d20",{defaultValue:"Plan moments worth remembering."})}</p>
  {stepped&&<ol className="suSteps" aria-label={localizeText('Sign-up progress')}><li aria-current={step===0?'step':undefined} data-done={step>0}>{localizeText('Your details')}</li><li aria-current={step===1?'step':undefined}>{localizeText('Password')}</li></ol>}
  <form onSubmit={submit} className="hostForm" noValidate>
   {showDetails&&<fieldset className="suGroup"><legend>{stepped?localizeText('Step 1 of 2 · Your details'):localizeText('Your details')}</legend>
    <div className="suField"><label>{localize("ui.f13a64ba2fea87ef",{defaultValue:"Full name"})}<input ref={refs.fullName} maxLength={120} autoComplete="name" enterKeyHint="next" value={fullName} aria-invalid={!!problems.fullName} aria-describedby={describe('fullName')} onChange={edit('fullName',setFullName)}/></label>{problem('fullName')}</div>
    <div className="suField"><label>{localize("ui.e3b89e9d33f88e52",{defaultValue:"Username"})}<input ref={refs.username} maxLength={30} autoComplete="username" autoCapitalize="none" autoCorrect="off" spellCheck={false} enterKeyHint="next" value={username} aria-invalid={!!problems.username} aria-describedby={describe('username','username-requirements')} onChange={edit('username',setUsername)} onBlur={()=>void checkOnBlur('username')}/></label>{problem('username')}{status('username',username)}</div>
    <CredentialRequirements kind="username" value={username} id="username-requirements"/>
    <div className="suField"><label>{localize("ui.f2488fd4ef4adbc6",{defaultValue:"Email address"})}<input ref={refs.email} type="email" inputMode="email" maxLength={254} autoComplete="email" autoCapitalize="none" autoCorrect="off" spellCheck={false} enterKeyHint="next" value={email} aria-invalid={!!problems.email} aria-describedby={describe('email','email-hint')} onChange={edit('email',setEmail)} onBlur={()=>void checkOnBlur('email')}/></label>{problem('email')}{status('email',email.trim())}<small id="email-hint" className="suHint">{emailVerificationEnabled?localizeText('You can verify it from Account after signing up.'):localizeText('We’ll use it for sign-in help and receipts.')}</small></div>
    <div className="suField"><label>{localizeText("Mobile number")}<input ref={refs.mobile} type="tel" inputMode="tel" autoComplete="tel" required aria-required="true" placeholder={localizeText("10-digit mobile number")} maxLength={20} enterKeyHint="next" value={mobile} aria-invalid={!!problems.mobile} aria-describedby={describe('mobile','mobile-hint')} onChange={edit('mobile',setMobile)}/></label>{problem('mobile')}<small id="mobile-hint" className="suHint">{mobilePreview?localizeText('We’ll save it as')+' '+mobilePreview:localizeText('10-digit Indian number, or start with + and your country code.')}</small></div>
   </fieldset>}
   {stepped&&step===1&&<div className="suSummary"><span>{localize("ui.b11b9535d6bd013e",{defaultValue:"Creating an account for "})}<strong>{fullName}</strong> · {email.trim()}</span><button type="button" className="suLinkButton" onClick={()=>{setStep(0);setProblems({});focusLater('fullName')}}>{localize("ui.a2fe55208689a5de",{defaultValue:"Edit your details"})}</button></div>}
   {showSecurity&&<fieldset className="suGroup"><legend>{stepped?<h2 ref={stepHeading} tabIndex={-1} className="suStepHeading">{localizeText('Step 2 of 2 · Secure your account')}</h2>:localizeText('Secure your account')}</legend>
    <label htmlFor="signup-password">{localizeText('Password')}</label>
    <div className="suPassword"><input id="signup-password" ref={refs.password} type={showPassword?'text':'password'} autoComplete="new-password" enterKeyHint="done" value={password} aria-invalid={!!problems.password} aria-describedby={describe('password','password-requirements')} onChange={e=>{edit('password',setPassword)(e);if(problems.confirmPassword)setProblems(p=>({...p,confirmPassword:undefined}))}}/><button type="button" className="suReveal" aria-pressed={showPassword} aria-label={showPassword?localizeText('Hide password'):localizeText('Show password')} onClick={()=>setShowPassword(v=>!v)}>{showPassword?<EyeOff size={20} aria-hidden="true"/>:<Eye size={20} aria-hidden="true"/>}</button></div>
    {problem('password')}
    <CredentialRequirements kind="password" value={password} id="password-requirements"/>
    <div className="suField"><label htmlFor="signup-confirm-password">{localizeText('Confirm password')}</label>
    <input id="signup-confirm-password" ref={refs.confirmPassword} type={showPassword?'text':'password'} autoComplete="new-password" enterKeyHint="done" value={confirmPassword} aria-invalid={!!problems.confirmPassword} aria-describedby={describe('confirmPassword','confirm-password-status')} onChange={edit('confirmPassword',setConfirmPassword)} onBlur={()=>{if(confirmPassword&&confirmPassword!==password)setProblems(p=>({...p,confirmPassword:'The two passwords don’t match. Please type the same password again.'}))}}/>
    {problem('confirmPassword')}
    {!problems.confirmPassword&&confirmPassword&&<span id="confirm-password-status" className={`suStatus${confirmPassword===password?' suOk':''}`} aria-live="polite">{confirmPassword===password?<><CheckCircle2 size={15} aria-hidden="true"/>{localizeText('Passwords match.')}</>:localizeText('Keep typing — must match the password above.')}</span>}</div>
    <label className="suTerms"><input ref={refs.terms} type="checkbox" checked={termsAccepted} aria-invalid={!!problems.terms} aria-describedby={describe('terms')} onChange={e=>{setTermsAccepted(e.target.checked);if(problems.terms)setProblems(p=>({...p,terms:undefined}))}}/><span>I agree to the <Link href="/terms">Terms &amp; Conditions</Link> and <Link href="/privacy">Privacy Policy</Link>.</span></label>
    {problem('terms')}
   </fieldset>}
   {error&&<div role="alert" className="hostError">{localizeText(error)}</div>}
   <button className="hostPrimary" disabled={busy} aria-busy={busy}>{busy?(stepped&&step===0?localizeText('Checking your details…'):localize("ui.f2daf61e527b4f1c",{defaultValue:"Creating account…"})):stepped&&step===0?localize("ui.496267ccc6b01605",{defaultValue:"Continue →"}):localize("ui.b44f53d3c6ec0d33",{defaultValue:"Create account →"})}</button>
  </form>
  <p>{localize("ui.6f6fec2efacfb96e",{defaultValue:"Already registered? "})}<Link href="/login">{localize("ui.bfd402b2f6f38125",{defaultValue:"Sign in"})}</Link></p></div></main>;
}
