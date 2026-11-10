'use client';
/* localization:instrumented */
import {localize} from '../../lib/localization.mjs';
import {useLocalizationRender} from '../LocalizationProvider';

import {giftPhotoUrl} from '../../lib/gift-photos.mjs';
import {useEffect,useState} from 'react';
import {useParams} from 'next/navigation';
import Link from 'next/link';
import {hostApi} from '../../lib/host-api.mjs';
import {giftApi} from '../../lib/gift-api.mjs';
import {Invitation,type InvitationEvent} from './Invitation';
export default function Preview(){useLocalizationRender();const {id}=useParams();const [event,setEvent]=useState<InvitationEvent|null>(null),[error,setError]=useState(''),[width,setWidth]=useState('full');useEffect(()=>{let active=true;Promise.all([hostApi.getEvent(fetch,String(id)),giftApi.list(fetch,String(id)),fetch(`/backend/api/v1/events/${id}/features`,{credentials:'include',cache:'no-store'}).then(r=>{if(!r.ok)throw Error('Preview unavailable');return r.json()}),hostApi.qrDetails(fetch,String(id)).catch(()=>({theme:''}))]).then(([e,g,f,q])=>{if(active)setEvent({...e,gifts:g.filter((x:{visibility:string;status:string})=>x.visibility==='PUBLIC'&&x.status==='ACTIVE').map((gift:{imageKey?:string})=>({...gift,imageUrl:giftPhotoUrl(String(id),gift.imageKey)})),features:f,invitationTheme:q.theme})}).catch(e=>{if(active)setError(e.message)});return()=>{active=false}},[id]);return <><div className="focusPreviewToolbar"><Link href={`/events/${id}`}>{localize("ui.af886f5edd0221cb",{defaultValue:"← Back to overview"})}</Link><span>{localize("ui.a9fad4ee74285e2b",{defaultValue:"Private preview · Payments disabled"})}</span><label>{localize("ui.0644dda59d887ed5",{defaultValue:"Preview width"})}<select value={width} onChange={e=>setWidth(e.target.value)}><option value="full">{localize("ui.d34008d28f89d292",{defaultValue:"Full width"})}</option><option value="mobile">{localize("ui.cbaad3cf4a6507e5",{defaultValue:"Mobile"})}</option></select></label></div><div className={width==='mobile'?'focusMobilePreview':''}>{event?<Invitation event={event} token="" preview/>:<p className="focusPage" role={error?'alert':'status'}>{error||localize("ui.c5e10b05898dfc6a",{defaultValue:"Preparing your preview…"})}</p>}</div></>}
