import {normalizeMobile} from './host-api.mjs';
import {passwordRules} from './credential-policy.mjs';

export const USERNAME_PATTERN=/^[A-Za-z0-9][A-Za-z0-9_]{3,29}$/;
export const EMAIL_PATTERN=/^[^\s@]+@[^\s@]+\.[^\s@]+$/;
/** Order of the first sign-up step, used to focus the first field that needs attention. */
export const DETAIL_FIELDS=['fullName','username','email','mobile'];

/** Per-field problems on the "Your details" step, in plain words. Empty object = ready to continue. */
export function detailProblems({fullName='',username='',email='',mobile=''}){
 const problems={};
 const name=fullName.trim();
 if(!name)problems.fullName='Please enter your full name.';
 else if(name.length<2||name.length>120)problems.fullName='Your name should be 2–120 characters.';
 if(!username)problems.username='Please choose a username.';
 else if(!USERNAME_PATTERN.test(username))problems.username='Use 4–30 letters, numbers or _ and start with a letter or number.';
 const mail=email.trim();
 if(!mail)problems.email='Please enter your email address.';
 else if(mail.length>254||!EMAIL_PATTERN.test(mail))problems.email='That email doesn’t look right. Example: name@example.com';
 if(!String(mobile).trim())problems.mobile='Please enter your mobile number.';
 else if(!normalizeMobile(mobile))problems.mobile='Enter a 10-digit mobile number (e.g. 9876543210) or include your country code (e.g. +44…).';
 return problems;
}

/** Problems on the "Secure your account" step. */
export function securityProblems({password='',confirmPassword='',termsAccepted=false}){
 const problems={};
 if(!password)problems.password='Please create a password.';
 else if(passwordRules(password).some(([,valid])=>!valid))problems.password='Your password doesn’t meet all the requirements listed below it yet.';
 if(!confirmPassword)problems.confirmPassword='Please type your password again to confirm it.';
 else if(confirmPassword!==password)problems.confirmPassword='The two passwords don’t match. Please type the same password again.';
 if(termsAccepted!==true)problems.terms='Please tick the box to agree to the Terms & Conditions and Privacy Policy.';
 return problems;
}

export function firstProblemField(problems,order=DETAIL_FIELDS){return order.find(field=>problems[field])||null}

/** Turns the availability API response into field problems (only for fields that are taken). */
export function availabilityProblems(result){
 const problems={};
 for(const field of ['username','email']){const value=result?.[field];if(value&&value.available===false)problems[field]=typeof value.message==='string'&&value.message?value.message:field==='email'?'An account already uses this email.':'This username is already taken.'}
 return problems;
}

/** Which field a registration error from the server belongs to, so the form can send the person straight to it. */
export function registrationErrorField(message=''){
 const text=String(message).toLowerCase();
 if(/username/.test(text))return 'username';
 if(/e-?mail/.test(text))return 'email';
 if(/mobile|phone/.test(text))return 'mobile';
 if(/password/.test(text))return 'password';
 if(/terms|privacy/.test(text))return 'terms';
 if(/full ?name|fullname/.test(text))return 'fullName';
 return null;
}

/** "+919876543210" → "+91 98765 43210" so people can confirm the number we will save. */
export function displayMobile(value){
 const normalized=normalizeMobile(value);
 if(!normalized)return '';
 const india=/^\+91([6-9][0-9]{4})([0-9]{5})$/.exec(normalized);
 return india?`+91 ${india[1]} ${india[2]}`:normalized;
}
