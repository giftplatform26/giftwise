
import {activeLocale} from './localization.mjs';
export const isEventId=value=>typeof value==='string'&&/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(value);
export function eventIdForRoute(path,queryId){const match=path.match(/^\/events\/([^/]+)(?:\/|$)/);return match&&isEventId(match[1])?match[1]:path==='/gifts'&&isEventId(queryId)?queryId:null;}
export function eventDate(value){if(!value)return 'Date to be decided';const d=new Date(`${value}T12:00:00Z`);return Number.isNaN(d.getTime())?'Date to be decided':new Intl.DateTimeFormat(activeLocale("en-IN"),{day:'numeric',month:'long',year:'numeric',timeZone:'UTC'}).format(d);}
export const inr=paise=>new Intl.NumberFormat(activeLocale("en-IN"),{style:'currency',currency:'INR',maximumFractionDigits:2}).format(paise/100);
export function occasionArt(occasion){return ({WEDDING:'sage-leaves',BIRTHDAY:'balloon-dreams',BABY_SHOWER:'cloud-dream',HOUSEWARMING:'olive-branch',ANNIVERSARY:'rose-gold-lines',FAREWELL:'linen-lines',RETIREMENT:'golden-arch',REUNION:'family-vines',OTHER:'cream-paper'})[occasion]||'cream-paper';}
export function restoreEventDraft(raw,defaults){
 try{
  const value=JSON.parse(raw);if(!value||typeof value!=='object'||Array.isArray(value))return defaults;
  const restore=(stored,base)=>Object.fromEntries(Object.entries(base).map(([key,fallback])=>{
   const candidate=stored?.[key];
   if(typeof fallback==='boolean')return [key,typeof candidate==='boolean'?candidate:fallback];
   if(fallback&&typeof fallback==='object')return [key,restore(candidate,fallback)];
   return [key,typeof candidate==='string'&&candidate.length<=10000?candidate:fallback];
  }));
  return restore(value,defaults);
 }catch{return defaults;}
}

export function invitationArt(event){return ({MODERN_MINIMAL:'cream-paper',ELEGANT_GOLD:'sage-leaves',BIRTHDAY_CELEBRATION:'balloon-dreams',BABY_SHOWER:'cloud-dream',ANNIVERSARY:'rose-gold-lines',HOUSEWARMING:'olive-branch'})[event.invitationTheme]||occasionArt(event.occasionType);}

export function eventBannerPhrase(path){
 if(path==='/gifts')return 'Curate Joy';
 const section=path.match(/^\/events\/[^/]+(?:\/([^/]+))?\/?$/)?.[1]||'';
 return ({'':'Plan Beautifully',celebration:'Gather Together',album:'Relive the Little Moments',contributions:'Shared Generosity',qr:'Create Wonder',preview:'First Glimpse',notifications:'Stay Connected','thank-you':'Share Gratitude'})[section]||'Celebrate Together';
}
