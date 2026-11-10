export const validId = id => /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(id);
export function whatsappUrl(url,title) {
  const parsed=new URL(url);
  if(!['https:','http:'].includes(parsed.protocol))throw Error('Invalid invitation URL');
  return `https://wa.me/?text=${encodeURIComponent(`You're invited to ${title}! ✨\n${parsed.href}`)}`;
}
export function csrfCookie(cookie='') {
  const part=cookie.split(';').map(x=>x.trim()).find(x=>x.startsWith('XSRF-TOKEN='));
  return part?decodeURIComponent(part.slice('XSRF-TOKEN='.length)):'';
}
export async function guestPost(fetcher,token,kind,body) {
  if(!validId(token)||!['rsvp','wishes'].includes(kind))throw Error('Invalid request');
  const r=await fetcher(`/backend/api/public/celebrations/${token}/${kind}`,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(body)});
  if(!r.ok)throw Error(r.status===409?'An RSVP already exists for this email':r.status===429?'Too many requests. Please try later.':'Unable to submit. Please check your details.');
  return r.json();
}
