'use client';
import {useEffect,useState} from 'react';
import Link from 'next/link';
import {localizeText} from '../lib/localization.mjs';
import {useLocalizationRender} from './LocalizationProvider';
import {hostRequest} from '../lib/host-api.mjs';

/**
 * On a guest's receipt: keep this gift in your own GiftWise ledger. Signed-in users with the ledger save in one tap;
 * everyone else gets a gentle sign-up prompt that returns here.
 */
export default function SaveToLedger({receiptToken}:{receiptToken:string}){useLocalizationRender();
 const [state,setState]=useState<'unknown'|'guest'|'ready'|'saved'|'off'>('unknown'),[error,setError]=useState('');
 useEffect(()=>{let active=true;fetch('/backend/api/v1/me/features',{credentials:'include',cache:'no-store'}).then(r=>{if(r.status===401||r.status===403)return null;return r.ok?r.json():{}}).then((f:Record<string,boolean>|null)=>{if(!active)return;setState(f===null?'guest':f.GIFT_LEDGER?'ready':'off')}).catch(()=>{if(active)setState('off')});return()=>{active=false}},[]);
 async function save(){setError('');try{await hostRequest(fetch,`/v1/me/ledger/from-receipt/${receiptToken}`,{method:'POST'});setState('saved')}catch(e){setError(e instanceof Error?e.message:'Could not save')}}
 if(state==='unknown'||state==='off')return null;
 if(state==='saved')return <p role="status" className="receiptSaved">{localizeText('Saved to your gift ledger.')} <Link href="/ledger">{localizeText('Open ledger')}</Link></p>;
 if(state==='guest')return <Link className="focusSecondary" href={`/register?next=${encodeURIComponent(`/r/${receiptToken}`)}`}>{localizeText('Keep a record of your gifts — free')}</Link>;
 return <><button type="button" className="focusSecondary" onClick={()=>void save()}>{localizeText('Save to my gift ledger')}</button>{error&&<p role="alert">{localizeText(error)}</p>}</>;
}
