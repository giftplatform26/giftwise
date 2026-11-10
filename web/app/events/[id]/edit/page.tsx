'use client';
import {useEffect,useState} from 'react';
import Link from 'next/link';
import {useParams,useRouter} from 'next/navigation';
import {localizeText} from '../../../../lib/localization.mjs';
import {useLocalizationRender} from '../../../../components/LocalizationProvider';
import DeliveryAddressFields,{emptyDeliveryAddress,type DeliveryAddress} from '../../../../components/DeliveryAddressFields';
import {hostApi,OCCASIONS} from '../../../../lib/host-api.mjs';
import {occasionLabel,statusLabel,statusHint,lifecycleActions} from '../../../../lib/event-labels.mjs';
import {saveFlash} from '../../../../lib/flash.mjs';
import HostToast from '../../../components/HostToast';

type Address={addressLine1?:string|null;city?:string|null;state?:string|null;postalCode?:string|null;country?:string|null};
type EventDetails={id:string;title:string;description:string|null;occasionType:string;eventDate:string|null;status:string;hostName:string|null;eventAddress?:Address|null;deliveryAddress?:Address|null;deliverySameAsEvent?:boolean;contactName?:string|null;contactPhone?:string|null;contactEmail?:string|null};
type Form={title:string;description:string;occasionType:string;eventDate:string;hostName:string;addressLine1:string;city:string;state:string;postalCode:string;country:string;contactName:string;contactPhone:string;contactEmail:string;deliverySameAsEvent:boolean;deliveryAddress:DeliveryAddress};
type TextKey=Exclude<keyof Form,'deliverySameAsEvent'|'deliveryAddress'>;
type Pending=null|'complete'|'cancel';

const text=(value:string|null|undefined)=>value??'';
function toForm(e:EventDetails):Form{
 const a=e.eventAddress||{},d=e.deliveryAddress||{};
 return {title:e.title,description:text(e.description),occasionType:e.occasionType,eventDate:text(e.eventDate),hostName:text(e.hostName),
  addressLine1:text(a.addressLine1),city:text(a.city),state:text(a.state),postalCode:text(a.postalCode),country:text(a.country)||'India',
  contactName:text(e.contactName),contactPhone:text(e.contactPhone),contactEmail:text(e.contactEmail),
  deliverySameAsEvent:e.deliverySameAsEvent!==false,
  deliveryAddress:{addressLine1:text(d.addressLine1),city:text(d.city),state:text(d.state),postalCode:text(d.postalCode),country:text(d.country)||emptyDeliveryAddress.country}};
}

export default function EditEventPage(){useLocalizationRender();
 const params=useParams();const router=useRouter();const id=String(params.id??'');
 const [event,setEvent]=useState<EventDetails|null>(null),[form,setForm]=useState<Form|null>(null);
 const [loading,setLoading]=useState(true),[busy,setBusy]=useState(false),[error,setError]=useState(''),[notice,setNotice]=useState('');
 const [pending,setPending]=useState<Pending>(null);

 useEffect(()=>{let active=true;
  (async()=>{try{const value=await hostApi.getEvent(fetch,id) as EventDetails;if(!active)return;setEvent(value);setForm(toForm(value));}
   catch(e){if(active)setError(e instanceof Error?e.message:'Unable to load this celebration');}
   finally{if(active)setLoading(false);}})();
  return()=>{active=false};
 },[id]);

 const actions=lifecycleActions(event?.status);
 const field=(key:TextKey,value:string)=>{setForm(f=>f?{...f,[key]:value}:f);setError('');setNotice('');};
 const input=(key:TextKey,label:string,options:{required?:boolean;type?:string;maxLength?:number;autoComplete?:string}={})=>
  <label>{localizeText(label)}{options.required?' *':''}<input {...options} disabled={!actions.canEdit||busy} value={form?.[key]??''} onChange={e=>field(key,e.target.value)}/></label>;

 async function save(e:React.FormEvent){e.preventDefault();if(!form||busy)return;setBusy(true);setError('');setNotice('');
  try{const updated=await hostApi.updateEvent(fetch,id,form) as EventDetails;setEvent(updated);setForm(toForm(updated));setNotice(localizeText('Your changes are saved.'));}
  catch(err){setError(err instanceof Error?err.message:'Could not save your changes');}
  finally{setBusy(false);}
 }
 async function changeStatus(kind:'complete'|'cancel'){if(busy)return;setBusy(true);setError('');setNotice('');
  try{const updated=await (kind==='complete'?hostApi.completeEvent(fetch,id):hostApi.cancelEvent(fetch,id)) as EventDetails;
   setEvent(updated);setForm(toForm(updated));setPending(null);
   saveFlash(window.sessionStorage,'success',kind==='complete'?'Celebration marked as completed. Thank you for celebrating with GiftWise!':'Celebration cancelled. The invitation link is now closed.');
   router.push(`/events/${encodeURIComponent(id)}`);
  }catch(err){setError(err instanceof Error?err.message:'Could not update the celebration status');setBusy(false);}
 }

 return <main className="focusPage"><HostToast/><div style={{maxWidth:820}}>
  <p><Link className="focusTextLink" href={`/events/${encodeURIComponent(id)}`}>← {localizeText('Back to overview')}</Link></p>
  <section className="focusPanel">
   <span className="focusEyebrow">{localizeText('CELEBRATION DETAILS')}</span>
   <h1 style={{margin:'8px 0 4px'}}>{event?.title||localizeText('Edit celebration')}</h1>
   {event&&<p className="finePrint">{statusLabel(event.status)} · {statusHint(event.status)}</p>}
   {loading&&<p role="status">{localizeText('Loading your celebration…')}</p>}
   {error&&<p role="alert" className="focusError">{localizeText(error)}</p>}
   {notice&&<p role="status" className="focusGiftComplete">{notice}</p>}
   {event?.status==='PUBLISHED'&&<p className="finePrint">{localizeText('This celebration is live. Guests will see your changes as soon as you save.')}</p>}
   {event&&!actions.canEdit&&<p className="finePrint">{localizeText('Completed or cancelled celebrations can no longer be edited.')}</p>}
   {form&&<form className="focusForm" onSubmit={save}>
    {input('title','Event name',{required:true,maxLength:200})}
    <label>{localizeText('Occasion')}<select disabled={!actions.canEdit||busy} value={form.occasionType} onChange={e=>field('occasionType',e.target.value)}>{OCCASIONS.map((o:string)=><option key={o} value={o}>{occasionLabel(o)}</option>)}</select></label>
    {input('hostName','Hosted by',{required:true,maxLength:200})}
    {input('eventDate','Date (optional)',{type:'date'})}
    <label>{localizeText('About this event')}<textarea disabled={!actions.canEdit||busy} maxLength={10000} rows={4} value={form.description} onChange={e=>field('description',e.target.value)}/></label>
    <h3>{localizeText('Event address')}</h3>
    {input('addressLine1','Street address',{required:true,maxLength:200,autoComplete:'street-address'})}
    {input('city','City',{required:true,maxLength:120,autoComplete:'address-level2'})}
    {input('state','State',{required:true,maxLength:120,autoComplete:'address-level1'})}
    {input('postalCode','PIN / postal code',{required:true,maxLength:20,autoComplete:'postal-code'})}
    {input('country','Country',{required:true,maxLength:80,autoComplete:'country-name'})}
    <h3>{localizeText('Contact for guests')}</h3>
    {input('contactName','Contact name',{required:true,maxLength:150,autoComplete:'name'})}
    {input('contactPhone','Contact phone',{required:true,type:'tel',maxLength:30,autoComplete:'tel'})}
    {input('contactEmail','Contact email (optional)',{type:'email',maxLength:254,autoComplete:'email'})}
    {actions.canEdit&&<DeliveryAddressFields same={form.deliverySameAsEvent} address={form.deliveryAddress} onSameChange={same=>setForm(f=>f?{...f,deliverySameAsEvent:same}:f)} onAddressChange={address=>setForm(f=>f?{...f,deliveryAddress:address}:f)}/>}
    {actions.canEdit&&<button className="focusPrimary" type="submit" disabled={busy}>{busy?localizeText('Saving…'):localizeText('Save changes')}</button>}
   </form>}
  </section>

  {event&&(actions.canComplete||actions.canCancel)&&<section className="focusPanel" style={{marginTop:20}} aria-labelledby="event-status-heading">
   <h2 id="event-status-heading">{localizeText('Celebration status')}</h2>
   {pending===null&&<div style={{display:'flex',gap:12,flexWrap:'wrap'}}>
    {actions.canComplete&&<button type="button" className="focusSecondary" disabled={busy} onClick={()=>setPending('complete')}>{localizeText('Mark as completed')}</button>}
    {actions.canCancel&&<button type="button" className="focusSecondary" disabled={busy} onClick={()=>setPending('cancel')}>{localizeText('Cancel celebration')}</button>}
   </div>}
   {pending&&<div role="alertdialog" aria-labelledby="event-status-confirm" className="focusError">
    <p id="event-status-confirm"><strong>{pending==='complete'?localizeText('Mark this celebration as completed?'):localizeText('Cancel this celebration?')}</strong></p>
    <p>{pending==='complete'
     ?localizeText('The invitation link will close for guests, so no new contributions, RSVPs or wishes can be made. Your contribution reports stay available to you. This cannot be undone.')
     :localizeText('The invitation link will close for guests. Contributions already made are not refunded automatically. This cannot be undone.')}</p>
    <div style={{display:'flex',gap:12,flexWrap:'wrap'}}>
     <button type="button" className="focusPrimary" disabled={busy} onClick={()=>void changeStatus(pending)}>{busy?localizeText('Updating…'):pending==='complete'?localizeText('Yes, mark completed'):localizeText('Yes, cancel celebration')}</button>
     <button type="button" disabled={busy} onClick={()=>setPending(null)}>{localizeText('Keep it as it is')}</button>
    </div>
   </div>}
  </section>}
 </div></main>;
}
