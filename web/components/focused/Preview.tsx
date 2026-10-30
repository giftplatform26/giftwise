'use client';
import {giftPhotoUrl} from '../../lib/gift-photos.mjs';
import {useEffect,useState} from 'react';
import {useParams} from 'next/navigation';
import Link from 'next/link';
import {hostApi} from '../../lib/host-api.mjs';
import {giftApi} from '../../lib/gift-api.mjs';
import {Invitation,type InvitationEvent} from './Invitation';
export default function Preview(){const {id}=useParams();const [event,setEvent]=useState<InvitationEvent|null>(null),[error,setError]=useState(''),[width,setWidth]=useState('full');useEffect(()=>{let active=true;Promise.all([hostApi.getEvent(fetch,String(id)),giftApi.list(fetch,String(id)),fetch(`/backend/api/v1/events/${id}/features`,{credentials:'include',cache:'no-store'}).then(r=>{if(!r.ok)throw Error('Preview unavailable');return r.json()}),hostApi.qrDetails(fetch,String(id)).catch(()=>({theme:''}))]).then(([e,g,f,q])=>{if(active)setEvent({...e,gifts:g.filter((x:{visibility:string;status:string})=>x.visibility==='PUBLIC'&&x.status==='ACTIVE').map((gift:{imageKey?:string})=>({...gift,imageUrl:giftPhotoUrl(String(id),gift.imageKey)})),features:f,invitationTheme:q.theme})}).catch(e=>{if(active)setError(e.message)});return()=>{active=false}},[id]);return <><div className="focusPreviewToolbar"><Link href={`/events/${id}`}>← Back to overview</Link><span>Private preview · Payments disabled</span><label>Preview width<select value={width} onChange={e=>setWidth(e.target.value)}><option value="full">Full width</option><option value="mobile">Mobile</option></select></label></div><div className={width==='mobile'?'focusMobilePreview':''}>{event?<Invitation event={event} token="" preview/>:<p className="focusPage" role={error?'alert':'status'}>{error||'Preparing your preview…'}</p>}</div></>}
