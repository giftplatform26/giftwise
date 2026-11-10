const redirecting=new WeakSet();
export function isProtectedHostRequest(input) {
 try {
  const path=new URL(input,'https://gift-platform.invalid').pathname;
  return /^\/(?:backend\/)?api\/(?:v1\/(?:events|host)(?:\/|$)|auth\/(?:me|profile-name|logout)$)/.test(path);
 }catch{return false;}
}
export function redirectIfSessionExpired(response,input,location=typeof window==='undefined'?null:window.location) {
 if(response.status!==401||!location||!isProtectedHostRequest(input))return false;
 if(location.pathname==='/login')return false;
 // Verification owns its sign-in prompt and preserves the return to this page.
 if(location.pathname==='/verify-email'&&/^\/(?:backend\/)?api\/auth\/me$/.test(new URL(input,'https://gift-platform.invalid').pathname))return false;
 if(redirecting.has(location))return true;
 redirecting.add(location);
 const next=location.pathname+(location.search||'');
 location.replace(`/login?reason=session-expired&next=${encodeURIComponent(next)}`);
 return true;
}
