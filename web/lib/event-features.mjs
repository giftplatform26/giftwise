export const featureCodes=['GROUP_GIFTING','GUEST_GIFTS','RSVP','GUEST_ATTENDANCE','WHATSAPP_SHARING','GUESTBOOK','SOCIAL_SHARING'];
export function normalizeFeatures(value){
  return {GIFTING_ENABLED:value?.GIFTING_ENABLED===true,CELEBRATION_PLANNING:value?.CELEBRATION_PLANNING===true,EVENT_ESSENTIALS:value?.EVENT_ESSENTIALS===true,EVENT_REUSE:value?.EVENT_REUSE===true,GUEST_GIFTS:value?.GUEST_GIFTS===true&&value?.GROUP_GIFTING===true,GROUP_GIFTING:value?.GROUP_GIFTING===true,RSVP:value?.RSVP===true,GUEST_ATTENDANCE:value?.GUEST_ATTENDANCE===true,WHATSAPP_SHARING:value?.WHATSAPP_SHARING===true,GUESTBOOK:value?.GUESTBOOK===true,SOCIAL_SHARING:value?.SOCIAL_SHARING===true};
}
export async function fetchEventFeatures(fetcher,token,signal){
  if(!/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(token))throw Error('Invalid invitation');
  const response=await fetcher(`/backend/api/public/events/${encodeURIComponent(token)}/features`,{cache:'no-store',credentials:'same-origin',signal});
  if(!response.ok)throw Error('Invitation features are unavailable');
  return normalizeFeatures(await response.json());
}
