import {redirectIfSessionExpired} from './host-session.mjs';
import {eventGiftPath, toGiftRequest, csrfToken} from './gift-api.mjs';
import {normalizeProductUrl} from './gifts.mjs';
const validMethods = new Set(['GET','POST','PUT']);
export async function remoteRequest(fetcher, {eventId,giftId,action='',method='GET',payload,cookie=''}) {
  if (!validMethods.has(method)) throw Error('Unsupported HTTP method');
  const path=eventGiftPath(eventId,giftId)+(action?`/${action}`:'');
  const headers={'Accept':'application/json'};
  if (payload!==undefined) headers['Content-Type']='application/json';
  if (method!=='GET') {const token=csrfToken(cookie);if(!token)throw Error('CSRF token missing. Sign in through the backend and refresh this page.');headers['X-XSRF-TOKEN']=token;}
  const response=await fetcher(path,{method,credentials:'include',headers,...(payload===undefined?{}:{body:JSON.stringify(payload)})});
  redirectIfSessionExpired(response,path);
  if(!response.ok){const message=({400:'Invalid gift details',401:'Login required',403:'Access denied or CSRF validation failed',404:'Event or gift not found',409:'Gift was changed; reload and retry'})[response.status]||`Server error (HTTP ${response.status})`;throw Error(message);}
  return response.status===204?null:response.json();
}
export const remoteGifts={
  list:(f,e)=>remoteRequest(f,{eventId:e}),
  create:(f,e,g,c)=>remoteRequest(f,{eventId:e,method:'POST',payload:toGiftRequest(g),cookie:c}),
  update:(f,e,id,g,c)=>remoteRequest(f,{eventId:e,giftId:id,method:'PUT',payload:toGiftRequest(g),cookie:c}),
  publish:(f,e,id,visibility,c)=>remoteRequest(f,{eventId:e,giftId:id,action:'publication',method:'PUT',payload:{visibility},cookie:c}),
  link:(f,e,id,url,c)=>remoteRequest(f,{eventId:e,giftId:id,action:'product-link',method:'PUT',payload:{productUrl:url?normalizeProductUrl(url):null},cookie:c}),
  cancel:(f,e,id,c)=>remoteRequest(f,{eventId:e,giftId:id,action:'cancel',method:'POST',cookie:c})
};
export function toDisplayGift(g){return {id:g.id,name:g.name,description:g.description||'',imageKey:g.imageKey||'',targetAmount:Number(g.targetAmount),productUrl:g.productUrl||'',status:g.visibility,contributionsEnabled:false,createdAt:'',lifecycle:g.status,priority:g.priority??'MEDIUM'};}
