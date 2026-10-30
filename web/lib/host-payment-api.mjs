import {csrfToken} from './gift-api.mjs';
const base='/backend/api/v1/host/payment-profiles';
const uuid=/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
export function validId(id){if(!uuid.test(String(id)))throw Error('Invalid destination identifier');return id;}
export function validateUpi(upiId){const v=String(upiId||'').trim();if(!/^[a-zA-Z0-9._-]{2,64}@[a-zA-Z0-9.-]{2,32}$/.test(v))throw Error('Enter a valid UPI ID');return {upiId:v};}
export function validateBank({accountNumber,ifsc,accountHolderName}){const n=String(accountNumber||'').trim(),i=String(ifsc||'').trim().toUpperCase(),h=String(accountHolderName||'').trim();if(!/^\d{6,20}$/.test(n))throw Error('Account number must contain 6–20 digits');if(!/^[A-Z]{4}0[A-Z0-9]{6}$/.test(i))throw Error('Enter a valid IFSC');if(h.length<2||h.length>120)throw Error('Enter the account holder name');return {accountNumber:n,ifsc:i,accountHolderName:h};}
async function request(fetcher,path,{method='GET',payload,file,cookie}={}){if(!path.startsWith(base+'/'))throw Error('Unsupported payment route');const headers={Accept:'application/json'};if(payload!==undefined)headers['Content-Type']='application/json';if(method!=='GET'){const token=csrfToken(cookie??(typeof document!=='undefined'?document.cookie:''));if(!token)throw Error('Secure session token missing. Sign in and refresh.');headers['X-XSRF-TOKEN']=token;}const res=await fetcher(path,{method,credentials:'include',cache:'no-store',headers,...(payload!==undefined?{body:JSON.stringify(payload)}:{}),...(file?{body:file}:{})});if(!res.ok){if(res.status===404)throw Error('Payment profiles are unavailable. Ask the administrator to enable this feature.');if(res.status===401)throw Error('Please sign in to manage payment details.');if(res.status===403)throw Error('Access denied or secure session expired.');if(res.status===409&&path.includes('/event-links/'))throw Error('Enable the account and confirm its details before selecting Preferred for this event. UPI requires a valid QR. If the host account is disabled, enable it in Host receiving accounts first.');if(res.status===409)throw Error('This action is not available. For UPI accounts, upload or generate a valid QR and confirm your details first.');if(res.status===400){
    let detail='';
    try {
      const body=await res.json();
      detail=String(body.detail||body.message||'');
    } catch {}
    const allowed=[
      'QR image size is invalid',
      'QR image dimensions are unsupported',
      'QR must contain a valid UPI payment link',
      'QR contains malformed UPI payment information',
      'QR UPI ID does not match the registered UPI account',
      'Invalid or unreadable UPI QR image'
    ];
    throw Error(allowed.includes(detail)?detail:'Invalid payment details or QR image (HTTP 400)');
  }
  throw Error(`Payment settings request failed (HTTP ${res.status})`);}return res.status===204?null:res.json();}
function eventAccountRoute(eventId,kind,id,action){return `${base}/event-links/${validId(eventId)}/${kind==='banks'?'bank':kind}/${validId(id)}/${action}`;}
export const paymentProfiles={
 linkToEvent:(f,eventId,kind,id,cookie)=>request(f,eventAccountRoute(eventId,kind,id,'link'),{method:'POST',cookie}),
 verificationSettings:f=>request(f,base+'/account-verification/settings'),
 verificationHistory:(f,kind,id)=>request(f,`${base}/account-verification/${kind}/${validId(id)}`),
 verifyAccount:(f,kind,id,accountHolderName,cookie)=>request(f,`${base}/account-verification/${kind}/${validId(id)}`,{method:'POST',payload:{accountHolderName,consent:true},cookie}),
 upis:(f,eventId='')=>request(f,base+'/upi'+(eventId?`?eventId=${validId(eventId)}`:'')),banks:(f,eventId='')=>request(f,base+'/banks'+(eventId?`?eventId=${validId(eventId)}`:'')),
 async qrUploaded(f,id){const res=await f(`${base}/upi/${validId(id)}/qr/uploaded/status`,{method:'GET',credentials:'include',cache:'no-store',headers:{Accept:'application/json'}});if(!res.ok)throw Error(`Unable to check QR status (HTTP ${res.status})`);return (await res.json()).uploaded===true;},
 addUpi:(f,upiId,cookie,accountLabel='',eventId='')=>request(f,base+'/upi',{method:'POST',payload:{...validateUpi(upiId),accountLabel,eventId:eventId||null},cookie}),
 addBank:(f,input,cookie)=>request(f,base+'/banks',{method:'POST',payload:{...validateBank(input),accountLabel:input.accountLabel||'',eventId:input.eventId||null},cookie}),
 rename:(f,kind,id,accountLabel,cookie)=>request(f,`${base}/${kind}/${validId(id)}/label`,{method:'PATCH',payload:{accountLabel},cookie}),
 archive:(f,kind,id,cookie,eventId='')=>request(f,eventId?eventAccountRoute(eventId,kind,id,'unlink'):`${base}/${kind}/${validId(id)}`,{method:eventId?'POST':'DELETE',cookie}),
 preferUpi:(f,id,cookie,eventId='')=>request(f,eventId?eventAccountRoute(eventId,'upi',id,'preferred'):`${base}/upi/${validId(id)}/preferred`,{method:'POST',cookie}),
 preferBank:(f,id,cookie,eventId='')=>request(f,eventId?eventAccountRoute(eventId,'banks',id,'preferred'):`${base}/banks/${validId(id)}/preferred`,{method:'POST',cookie}),
 enableUpi:(f,id,cookie,eventId='')=>request(f,eventId?eventAccountRoute(eventId,'upi',id,'enable'):`${base}/upi/${validId(id)}/enable`,{method:'POST',cookie}),
 confirmUpi:(f,id,cookie)=>request(f,`${base}/upi/${validId(id)}/host-confirm`,{method:'POST',cookie}),
 disableUpi:(f,id,cookie,eventId='')=>request(f,eventId?eventAccountRoute(eventId,'upi',id,'disable'):`${base}/upi/${validId(id)}/disable`,{method:'POST',cookie}),
 enableBank:(f,id,cookie,eventId='')=>request(f,eventId?eventAccountRoute(eventId,'banks',id,'enable'):`${base}/banks/${validId(id)}/enable`,{method:'POST',cookie}),
 confirmBank:(f,id,cookie)=>request(f,`${base}/banks/${validId(id)}/host-confirm`,{method:'POST',cookie}),
 disableBank:(f,id,cookie,eventId='')=>request(f,eventId?eventAccountRoute(eventId,'banks',id,'disable'):`${base}/banks/${validId(id)}/disable`,{method:'POST',cookie}),
 uploadQr:(f,id,file,cookie)=>{if(!(file instanceof Blob)||file.size===0||file.type!=='image/png')throw Error('Select a PNG QR image');if(file.size>2*1024*1024)throw Error('QR image exceeds the 2 MB upload limit');const data=new FormData();data.append('file',file,'upi-qr.png');return request(f,`${base}/upi/${validId(id)}/qr/upload`,{method:'POST',file:data,cookie});},
 async qrImage(f,id,kind='uploaded',cookie){if(!['uploaded','generate'].includes(kind))throw Error('Invalid QR action');const path=`${base}/upi/${validId(id)}/qr/${kind}`;const method=kind==='generate'?'POST':'GET';const headers={Accept:'image/png'};if(method==='POST'){const token=csrfToken(cookie??(typeof document!=='undefined'?document.cookie:''));if(!token)throw Error('Secure session token missing');headers['X-XSRF-TOKEN']=token;}const res=await f(path,{method,credentials:'include',cache:'no-store',headers});if(!res.ok)throw Error(res.status===404?'No uploaded QR found for this UPI ID. Upload a valid matching PNG first.':res.status===409?'UPI account is unavailable for QR generation.':res.status===401?'Your session has expired. Sign in again.':`QR unavailable (HTTP ${res.status})`);if(!(res.headers?.get('content-type')||'').includes('image/png'))throw Error('Unexpected QR response');return res.blob();}
};
export function maskedBank(b){const masked=String(b.accountMasked??b.maskedAccountNumber??b.accountNumberMasked??'');return masked&& !/^\d{6,20}$/.test(masked)?masked:'Account details protected';}

