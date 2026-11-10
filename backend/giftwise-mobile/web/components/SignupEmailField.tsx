'use client';
/* localization:instrumented */
import {localize} from '../lib/localization.mjs';
import {useLocalizationRender} from './LocalizationProvider';

import {useVerification} from './VerificationProvider';
import {useId} from 'react';
import {CheckCircle2,XCircle} from 'lucide-react';
import {signupEmailState} from '../lib/signup-email.mjs';
import './email-verification.css';
export default function SignupEmailField({value,onChange,verified=false}:{value:string;onChange:(value:string)=>void;verified?:boolean}){useLocalizationRender();
 const {emailVerificationEnabled}=useVerification();
 const id=useId(),state=signupEmailState(value,verified);
 return <label className="signup-email-field" htmlFor={id}>{localize("ui.f2488fd4ef4adbc6",{defaultValue:"Email address"})}<span className="signup-email-input"><input id={id} required type="email" autoComplete="email" maxLength={254} value={value} aria-invalid={!!value&&!state.validFormat} aria-describedby={id+'-status'} onChange={e=>onChange(e.target.value)}/><span className="signup-email-mark" data-valid={state.validFormat} aria-hidden="true">{state.validFormat?<CheckCircle2 size={21}/>:<XCircle size={21}/>}</span></span><span id={id+'-status'} className="signup-email-note" data-valid={state.validFormat} aria-live="polite">{!emailVerificationEnabled&&state.validFormat?localize("ui.f0ce396726c0c304",{defaultValue:"Email format is valid."}):state.message}</span></label>;
}
