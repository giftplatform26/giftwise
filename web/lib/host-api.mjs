import {passwordRules} from './credential-policy.mjs';
import {redirectIfSessionExpired} from './host-session.mjs';
import {csrfToken} from './gift-api.mjs';
export const OCCASIONS=['WEDDING','BIRTHDAY','BABY_SHOWER','HOUSEWARMING','ANNIVERSARY','FAREWELL','RETIREMENT','REUNION','OTHER'];
const base='/backend/api';
export function validateRegistration({fullName,username,email,mobile,password,emailVerificationToken,termsAccepted}) {
 if(!fullName || fullName.trim().length<2 || fullName.trim().length>120)throw Error('Enter your full name');
 if(!/^[A-Za-z0-9][A-Za-z0-9_]{3,29}$/.test(username||'')) throw Error('Username must be 4–30 letters, numbers or underscores, starting with a letter or number');
 if(!email || email.length>254 || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) throw Error('Enter a valid email');
 if(!/^\+[1-9][0-9]{7,14}$/.test(mobile||'')) throw Error('Enter a mobile number with country code, e.g. +919876543210');
 if(passwordRules(password||'').some(([,valid])=>!valid)) throw Error('Password must contain at least 8 characters, uppercase and lowercase letters, a number and a special character, within 72 UTF-8 bytes.');
 if(termsAccepted!==true) throw Error('Please agree to the Terms & Conditions and Privacy Policy.');
 return {fullName:fullName.trim(),username,email,mobile,password,termsAccepted,...(emailVerificationToken?{emailVerificationToken}:{})};
}
export function validateEvent({title,description='',occasionType,eventDate='',addressLine1='',city='',state='',postalCode='',country='India',contactName='',contactPhone='',contactEmail='',hostName='',deliverySameAsEvent=true,deliveryAddress=null}) {
 if(!title?.trim()||title.trim().length>200) throw Error('Event title must be 1–200 characters');
 if(typeof hostName!=='string'||hostName.trim().length>200)throw Error('Host name must be at most 200 characters');
 if(description.length>10000) throw Error('Description too long');
 if(!OCCASIONS.includes(occasionType)) throw Error('Choose a supported occasion');
 if(eventDate){const parsed=new Date(eventDate+'T00:00:00.000Z');if(!/^\d{4}-\d{2}-\d{2}$/.test(eventDate)||!Number.isFinite(parsed.getTime())||parsed.toISOString().slice(0,10)!==eventDate)throw Error('Please enter a valid event date.');}
  for(const [key,value] of Object.entries({addressLine1,city,state,postalCode,country,contactName,contactPhone}))if(!String(value).trim())throw Error(`${key} is required`);
  if(addressLine1.length>200||city.length>120||state.length>120||country.length>80||contactName.length>150)throw Error('Address or contact field is too long');
  if(country.trim().toLowerCase()==='india'&&!/^[1-9][0-9]{5}$/.test(postalCode.trim()))throw Error('Enter a valid 6-digit Indian PIN code');
  if(!/^[+0-9()\- ]{7,30}$/.test(contactPhone.trim()))throw Error('Enter a valid contact phone');
  if(contactEmail&&(!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(contactEmail)||contactEmail.length>254))throw Error('Enter a valid contact email');
  let normalizedDelivery=null;
  if(deliverySameAsEvent===false){
   normalizedDelivery={};
   for(const [key,max] of Object.entries({addressLine1:200,city:120,state:120,postalCode:20,country:80})){
    const value=deliveryAddress?.[key];
    if(typeof value!=='string'||!value.trim()||value.trim().length>max)throw Error(`Enter a valid delivery ${key}`);
    normalizedDelivery[key]=value.trim();
   }
   if(normalizedDelivery.country.toLowerCase()==='india'&&!/^[1-9][0-9]{5}$/.test(normalizedDelivery.postalCode))throw Error('Enter a valid 6-digit Indian delivery PIN code');
  }
  return {deliverySameAsEvent:deliverySameAsEvent!==false,deliveryAddress:normalizedDelivery,title:title.trim(),description,occasionType,eventDate:eventDate||null,addressLine1:addressLine1.trim(),city:city.trim(),state:state.trim(),postalCode:postalCode.trim(),country:country.trim(),contactName:contactName.trim(),contactPhone:contactPhone.trim(),contactEmail:contactEmail.trim()||null,...(hostName.trim()?{hostName:hostName.trim()}:{})};
}
export async function bootstrapCsrf(fetcher) {
 const r=await fetcher(`${base}/auth/csrf`,{method:'GET',credentials:'include',cache:'no-store'});
 if(!r.ok) throw Error(`Unable to initialize secure session (HTTP ${r.status})`);
}
/** @param {typeof fetch} fetcher
 * @param {string} route
 * @param {{method?:string,payload?:unknown,cookie?:string,bootstrap?:boolean}} [options]
 */
export async function hostRequest(fetcher,route,{method='GET',payload,cookie='',bootstrap=true}={}) {
 if(!route.startsWith('/auth/')&&!route.startsWith('/v1/events')&&!route.startsWith('/v1/me/')) throw Error('Unsupported host API route');
 if(!['GET','POST','PUT'].includes(method)) throw Error('Unsupported method');
 if(method!=='GET' && bootstrap) await bootstrapCsrf(fetcher);
 const token=csrfToken(cookie || (typeof document!=='undefined'?document.cookie:''));
 if(method!=='GET'&&!token) throw Error('CSRF cookie missing. Reload and try again.');
 const headers={'Accept':'application/json'};
 if(payload!==undefined)headers['Content-Type']='application/json';
 if(method!=='GET')headers['X-XSRF-TOKEN']=token;
 // A login may rotate the CSRF cookie. Retry ONLY the idempotent profile-name
 // assignment after obtaining a fresh token; never retry payments or event creation.
 const send = () => fetcher(`${base}${route}`,{method,credentials:'include',cache:'no-store',headers,...(payload===undefined?{}:{body:JSON.stringify(payload)})});
 let r=await send();
 if(r.status===403 && route==='/auth/profile-name' && method==='POST') {
   await bootstrapCsrf(fetcher);
   const refreshed=csrfToken(typeof document!=='undefined'?document.cookie:'');
   if(refreshed && refreshed!==headers['X-XSRF-TOKEN']) {
     headers['X-XSRF-TOKEN']=refreshed;
     r=await send();
   }
 }

 redirectIfSessionExpired(r,`${base}${route}`);
 if(!r.ok){const detail=[400,402,409].includes(r.status)?await serverReason(r):'';if(detail)throw Error(detail);const message=({402:'This needs an upgraded plan',400:'Check the submitted details',401: route === '/auth/login'
 ? 'Invalid username/email or password. Please try again.'
 : 'Your session is not authenticated. Please sign in again.',403:'Access denied or CSRF verification failed',404:'Event not found',409:'Conflicting or duplicate details'})[r.status]||`Request failed (HTTP ${r.status})`;throw Error(message);}
 return r.status===204?null:r.json();
}
/** Host-facing reason supplied by the API ({error}) for validation/conflict responses, if any. */
async function serverReason(response){try{const body=await response.json();const value=typeof body?.error==='string'?body.error:'';return value.length>0&&value.length<=500?value:''}catch{return ''}}
export const hostApi={
 me:f=>hostRequest(f,'/auth/me'),
 profileName:f=>hostRequest(f,'/auth/profile-name'),
 setProfileName:(f,fullName)=>hostRequest(f,'/auth/profile-name',{method:'POST',payload:{fullName}}),
 register:(f,input)=>hostRequest(f,'/auth/register',{method:'POST',payload:validateRegistration(input)}),
 login:(f,identifier,password)=>hostRequest(f,'/auth/login',{method:'POST',payload:{identifier,password}}),
 logout:f=>hostRequest(f,'/auth/logout',{method:'POST'}),
 deleteAccount:(f,password)=>{if(typeof password!=='string'||!password)throw Error('Enter your password to confirm.');return hostRequest(f,'/auth/profile/delete-account',{method:'POST',payload:{password,confirmation:'DELETE'}});},
 events:f=>hostRequest(f,'/v1/events'),
 createEvent:(f,event)=>hostRequest(f,'/v1/events',{method:'POST',payload:validateEvent(event)}),
 getEvent:(f,id)=>{if(!/^[0-9a-f-]{36}$/i.test(id))throw Error('Invalid event identifier');return hostRequest(f,`/v1/events/${id}`);},
 updateEvent:(f,id,event)=>{if(!/^[0-9a-f-]{36}$/i.test(id))throw Error('Invalid event identifier');return hostRequest(f,`/v1/events/${id}`,{method:'PUT',payload:validateEvent(event)});},
 completeEvent:(f,id)=>hostRequest(f,`/v1/events/${encodeURIComponent(id)}/complete`,{method:'POST'}),
 cancelEvent:(f,id)=>hostRequest(f,`/v1/events/${encodeURIComponent(id)}/cancel`,{method:'POST'}),
 publishEvent: (f, id) =>
  hostRequest(f, `/v1/events/${encodeURIComponent(id)}/publish`, {
    method: 'POST'
  }),
  unpublishEvent: (f, id) =>
  hostRequest(f, `/v1/events/${encodeURIComponent(id)}/unpublish`, {
    method: 'POST'
  }),
  qrDetails: (f, id) =>
  hostRequest(f, `/v1/events/${encodeURIComponent(id)}/qr`),

changeQrTheme: (f, id, theme) =>
  hostRequest(f, `/v1/events/${encodeURIComponent(id)}/qr/theme`, {
    method: 'PUT',
    payload: { theme }
  })
};

export async function fetchEventQrPng(fetcher, eventId) {
  if (!/^[0-9a-f-]{36}$/i.test(eventId)) {
    throw Error('Invalid event identifier');
  }

  const response = await fetcher(
    `/backend/api/v1/events/${encodeURIComponent(eventId)}/qr/png`,
    {
      method: 'GET',
      credentials: 'include',
      cache: 'no-store',
      headers: { Accept: 'image/png' }
    }
  );

  if (!response.ok) {
    throw Error(`Unable to load QR image (HTTP ${response.status})`);
  }

  return response.blob();
}

