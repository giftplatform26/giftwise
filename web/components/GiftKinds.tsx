'use client';
import {useState} from 'react';
import {localizeText} from '../lib/localization.mjs';
import {useLocalizationRender} from './LocalizationProvider';
import {hostRequest} from '../lib/host-api.mjs';
import {safeCharityUrl,charityProblem} from '../lib/gift-kinds.mjs';
import './gift-kinds.css';

export type GiftKind={kind:'ITEM'|'EXPERIENCE'|'CHARITY';charityName:string|null;charityUrl:string|null};

export function GiftKindBadge({kind}:{kind:GiftKind}){useLocalizationRender();
 if(kind.kind==='ITEM')return null;
 return <span className={`gkBadge gkBadge--${kind.kind.toLowerCase()}`}>{kind.kind==='EXPERIENCE'?localizeText('✦ Experience'):localizeText('♥ Charity wish')}</span>;
}

/** Guests donate on the charity's own site; GiftWise never handles this money. */
export function CharityAction({kind}:{kind:GiftKind}){useLocalizationRender();
 const url=safeCharityUrl(kind.charityUrl);
 if(!url)return null;
 return <div className="gkCharity"><a className="focusPrimary" href={url} target="_blank" rel="noopener noreferrer">{localizeText('Donate to {{name}}').replace('{{name}}',kind.charityName??'')}</a>
  <small>{localizeText('You give directly on the charity’s website. GiftWise does not receive this money.')}</small></div>;
}

/** Host control on each wishlist card: regular gift, experience or charity wish. */
export function GiftKindControl({eventId,giftId,value,onSaved}:{eventId:string;giftId:string;value?:GiftKind;onSaved:(k:GiftKind)=>void}){useLocalizationRender();
 const [open,setOpen]=useState(false),[kind,setKind]=useState<GiftKind['kind']>(value?.kind??'ITEM'),[name,setName]=useState(value?.charityName??''),[url,setUrl]=useState(value?.charityUrl??''),[error,setError]=useState(''),[busy,setBusy]=useState(false);
 async function save(){const problem=kind==='CHARITY'?charityProblem(name,url):'';if(problem){setError(problem);return}setBusy(true);setError('');
  try{const saved=await hostRequest(fetch,`/v1/events/${eventId}/gifts/${giftId}/kind`,{method:'PUT',payload:{kind,charityName:name,charityUrl:url}}) as GiftKind;onSaved(saved);setOpen(false)}
  catch(e){setError(e instanceof Error?e.message:'Could not save')}finally{setBusy(false)}}
 if(!open)return <button type="button" className="gkToggle" onClick={()=>setOpen(true)}>{value&&value.kind!=='ITEM'?<GiftKindBadge kind={value}/>:localizeText('Gift type')}</button>;
 return <div className="gkEditor">
  <div className="gkKinds" role="radiogroup" aria-label={localizeText('Gift type')}>{([['ITEM','Gift'],['EXPERIENCE','Experience'],['CHARITY','Charity']] as const).map(([k,l])=><label key={k}><input type="radio" name={`gk-${giftId}`} checked={kind===k} onChange={()=>setKind(k)}/><span>{localizeText(l)}</span></label>)}</div>
  {kind==='EXPERIENCE'&&<small>{localizeText('Guests contribute toward it like any gift — perfect for a honeymoon dinner or a spa day.')}</small>}
  {kind==='CHARITY'&&<><input value={name} maxLength={120} onChange={e=>setName(e.target.value)} placeholder={localizeText('Charity name')} aria-label={localizeText('Charity name')}/>
   <input type="url" value={url} maxLength={500} onChange={e=>setUrl(e.target.value)} placeholder="https://…/donate" aria-label={localizeText('Donation page link')}/>
   <small>{localizeText('Guests are sent to this page to donate. No money comes through GiftWise.')}</small></>}
  {error&&<p className="focusError" role="alert">{localizeText(error)}</p>}
  <div className="gkActions"><button type="button" disabled={busy} onClick={()=>void save()}>{localizeText('Save')}</button><button type="button" onClick={()=>setOpen(false)}>{localizeText('Cancel')}</button></div>
 </div>;
}
