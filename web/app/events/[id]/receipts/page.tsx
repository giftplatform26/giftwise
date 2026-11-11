'use client';
import {useCallback,useEffect,useState} from 'react';
import {useParams} from 'next/navigation';
import Link from 'next/link';
import {localizeText} from '../../../../lib/localization.mjs';
import {useLocalizationRender} from '../../../../components/LocalizationProvider';
import {hostRequest,bootstrapCsrf} from '../../../../lib/host-api.mjs';
import {csrfToken} from '../../../../lib/gift-api.mjs';
import {redirectIfSessionExpired} from '../../../../lib/host-session.mjs';
import {RECEIPT_LAYOUTS,RECEIPT_TEXT_LIMITS,receiptPayload,logoProblem,previewReceiptNumber} from '../../../../lib/receipt-design.mjs';
import CustomReceipt,{type ReceiptDesign,type ReceiptFacts} from '../../../../components/CustomReceipt';
import './receipt-designer.css';

type Template=ReceiptDesign&{platformEnabled:boolean;enabled:boolean;referencePrefix:string;version:number;nextReceiptNumber:string|null};
type TextKey=keyof typeof RECEIPT_TEXT_LIMITS;
type ToggleKey='showGuestName'|'showGiftName'|'showEventDate'|'showPaymentMethod'|'showPaymentCode';
const LAYOUT_NAMES:Record<string,[string,string]>={CLASSIC:['Classic','Clean and timeless'],ELEGANT:['Elegant','Serif lettering, gold rules'],FESTIVE:['Festive','Bright and joyful'],MINIMAL:['Minimal','Quiet and simple']};
const SWATCHES=['#6336B5','#9A6B2F','#D9467A','#3A8DBF','#2F7D5B','#30232B'];
const TEXT_FIELDS:[TextKey,string,boolean][]=[['title','Receipt title',false],['headerText','Header',false],['headerSubtext','Header subtitle',false],['thankYouMessage','Thank-you message',true],['signatureName','Signed by',false],['footerText','Footer note',true]];
const TOGGLES:[ToggleKey,string][]=[['showGuestName','Guest name'],['showGiftName','Gift they chose'],['showEventDate','Celebration date'],['showPaymentMethod','UPI or bank transfer'],['showPaymentCode','Payment code']];

export default function ReceiptDesigner(){useLocalizationRender();
 const {id}=useParams();const eventId=String(id??'');const route=`/v1/events/${eventId}/receipt-template`;
 const [design,setDesign]=useState<Template|null>(null),[saved,setSaved]=useState(''),[busy,setBusy]=useState(false),[error,setError]=useState(''),[notice,setNotice]=useState('');
 const load=useCallback(async()=>{setError('');try{const t=await hostRequest(fetch,route) as Template;setDesign(t);setSaved(JSON.stringify(t))}catch(e){setError(e instanceof Error?e.message:'Could not load your receipt design')}},[route]);
 useEffect(()=>{if(/^[0-9a-f-]{36}$/i.test(eventId))void load();else setError('Event not found')},[eventId,load]);
 const dirty=design!==null&&JSON.stringify(design)!==saved;
 useEffect(()=>{if(!dirty)return;const warn=(e:BeforeUnloadEvent)=>{e.preventDefault()};window.addEventListener('beforeunload',warn);return()=>window.removeEventListener('beforeunload',warn)},[dirty]);
 const set=<K extends keyof Template>(key:K,value:Template[K])=>{setNotice('');setDesign(d=>d&&{...d,[key]:value})};

 async function save(){if(!design)return;setBusy(true);setError('');setNotice('');
  try{const t=await hostRequest(fetch,route,{method:'PUT',payload:receiptPayload(design)}) as Template;setDesign(t);setSaved(JSON.stringify(t));
   setNotice(t.enabled?'Saved. Guests now see this design on their receipts.':'Saved. Turn on “Use this design” when you are ready for guests to see it.')}
  catch(e){setError(e instanceof Error?e.message:'Could not save your receipt design')}finally{setBusy(false)}}

 // Logo changes save immediately on the server; keep the host's unsaved text edits and take the new version.
 function adoptLogo(t:Template){setDesign(d=>d&&{...d,logoDataUrl:t.logoDataUrl,version:t.version});setSaved(s=>{try{return JSON.stringify({...JSON.parse(s),logoDataUrl:t.logoDataUrl,version:t.version})}catch{return s}})}
 async function uploadLogo(file:File|undefined){if(!file)return;const problem=logoProblem(file);if(problem){setError(problem);return}
  setBusy(true);setError('');setNotice('');
  try{await bootstrapCsrf(fetch);const body=new FormData();body.append('file',file);const url=`/backend/api${route}/logo`;
   const r=await fetch(url,{method:'POST',credentials:'include',cache:'no-store',headers:{'X-XSRF-TOKEN':csrfToken(document.cookie),'Accept':'application/json'},body});
   redirectIfSessionExpired(r,url);
   if(!r.ok){let reason='';try{const b=await r.json();reason=typeof b?.error==='string'?b.error:''}catch{}throw Error(reason||(r.status===413?'This image is too large':'Could not upload your logo'))}
   adoptLogo(await r.json());setNotice('Logo updated.')}
  catch(e){setError(e instanceof Error?e.message:'Could not upload your logo')}finally{setBusy(false)}}
 async function removeLogo(){setBusy(true);setError('');setNotice('');
  try{adoptLogo(await hostRequest(fetch,`${route}/logo/remove`,{method:'POST'}) as Template);setNotice('Logo removed.')}
  catch(e){setError(e instanceof Error?e.message:'Could not remove your logo')}finally{setBusy(false)}}

 const heading=<header className="focusPageHeading"><div><p className="focusEyebrow">{localizeText('Direct UPI & bank gifts')}</p><h1>{localizeText('Guest receipts')}</h1><p>{localizeText('Design the receipt guests receive after they send you a gift directly. Every gift already gets its own receipt number.')}</p></div><Link href={`/events/${eventId}/transactions`}>{localizeText('Payment verification')}</Link></header>;
 if(!design)return <main className="focusPage">{heading}{error?<p className="focusError" role="alert">{localizeText(error)} <button type="button" onClick={()=>void load()}>{localizeText('Retry')}</button></p>:<div className="focusSkeleton" role="status">{localizeText('Loading your receipt design…')}</div>}</main>;
 const locked=!design.platformEnabled;
 const sample:ReceiptFacts={receiptNumber:previewReceiptNumber(design.nextReceiptNumber,design.referencePrefix),amountPaise:250100,guestName:localizeText('Priya Sharma'),giftName:localizeText('Honeymoon fund'),
  eventTitle:design.headerText||localizeText('Your celebration'),eventDate:new Date().toISOString().slice(0,10),method:'UPI',paymentCode:'GW-7K3P9Q',createdAt:new Date().toISOString(),status:'HOST_CONFIRMED',hostDecidedAt:new Date().toISOString()};

 return <main className="focusPage rdPage">{heading}
  {locked&&<p className="rdLocked" role="status">{localizeText('Custom receipts are not switched on for GiftWise yet. You can look around; saving opens up once the platform enables them. Guests still get the standard receipt with a receipt number.')}</p>}
  {error&&<p className="focusError" role="alert">{localizeText(error)}{/another tab/i.test(error)&&<> <button type="button" onClick={()=>void load()}>{localizeText('Reload design')}</button></>}</p>}
  {notice&&<p className="rdNotice" role="status">{localizeText(notice)}</p>}
  <div className="rdGrid">
   <form className="focusForm rdForm" onSubmit={e=>{e.preventDefault();void save()}} aria-busy={busy}>
    <fieldset disabled={locked||busy} className="rdSwitch"><label><input type="checkbox" checked={design.enabled} onChange={e=>set('enabled',e.target.checked)}/><span><strong>{localizeText('Use this design for guest receipts')}</strong><small>{localizeText('When off, guests see the standard GiftWise receipt.')}</small></span></label></fieldset>

    <fieldset disabled={locked||busy}><legend>{localizeText('Style')}</legend>
     <div className="rdLayouts" role="radiogroup" aria-label={localizeText('Layout')}>{RECEIPT_LAYOUTS.map((layout:string)=>{const [name,hint]=LAYOUT_NAMES[layout];
      return <label key={layout} className={`rdLayout rdLayout--${layout.toLowerCase()}`}><input type="radio" name="layout" value={layout} checked={design.layout===layout} onChange={()=>set('layout',layout as Template['layout'])}/><span><strong>{localizeText(name)}</strong><small>{localizeText(hint)}</small></span></label>})}</div>
     <div className="rdColour"><span id="rd-colour">{localizeText('Accent colour')}</span>
      <div role="group" aria-labelledby="rd-colour">{SWATCHES.map(c=><button key={c} type="button" className="rdSwatch" style={{background:c}} aria-label={c} aria-pressed={design.accentColor.toUpperCase()===c} onClick={()=>set('accentColor',c)}/>)}
       <input type="color" aria-label={localizeText('Custom colour')} value={/^#[0-9A-Fa-f]{6}$/.test(design.accentColor)?design.accentColor:'#6336B5'} onChange={e=>set('accentColor',e.target.value.toUpperCase())}/></div></div>
     <div className="rdLogo"><span>{localizeText('Logo or monogram')}</span>
      <div>{design.logoDataUrl?<img src={design.logoDataUrl} alt="" width={56} height={56}/>:<span className="rdLogoEmpty" aria-hidden="true">✦</span>}
       <label className="focusSecondary rdUpload">{localizeText(design.logoDataUrl?'Replace':'Upload')}<input type="file" accept="image/png,image/jpeg,image/webp" onChange={e=>{void uploadLogo(e.target.files?.[0]);e.target.value=''}}/></label>
       {design.logoDataUrl&&<button type="button" className="focusTextLink" onClick={()=>void removeLogo()}>{localizeText('Remove')}</button>}</div>
      <small>{localizeText('PNG, JPEG or WebP up to 5 MB. We resize it and remove hidden photo details. Logo changes save straight away.')}</small></div>
    </fieldset>

    <fieldset disabled={locked||busy}><legend>{localizeText('Wording')}</legend>
     {TEXT_FIELDS.map(([key,label,long])=>{const value=design[key]??'';const max=RECEIPT_TEXT_LIMITS[key];
      return <label key={key}>{localizeText(label)}{long?<textarea rows={3} maxLength={max} value={value} onChange={e=>set(key,e.target.value)}/>:<input maxLength={max} value={value} required={key==='title'} onChange={e=>set(key,e.target.value)}/>}<small>{value.length}/{max}</small></label>})}
    </fieldset>

    <fieldset disabled={locked||busy}><legend>{localizeText('Details on the receipt')}</legend>
     <div className="rdToggles">{TOGGLES.map(([key,label])=><label key={key}><input type="checkbox" checked={design[key]} onChange={e=>set(key,e.target.checked)}/>{localizeText(label)}</label>)}</div>
     <small>{localizeText('Amount, receipt number and date are always shown.')}</small>
     <label>{localizeText('Receipt number prefix')}<input value={design.referencePrefix} maxLength={12} autoCapitalize="characters" pattern="[A-Za-z0-9]{2,12}" onChange={e=>set('referencePrefix',e.target.value.toUpperCase().replace(/[^A-Z0-9]/g,''))}/>
      <small>{localizeText('The next gift will be numbered {{number}}. Numbers already given to guests never change.').replace('{{number}}',previewReceiptNumber(design.nextReceiptNumber,design.referencePrefix))}</small></label>
    </fieldset>

    <div className="rdActions"><button type="submit" className="focusPrimary" disabled={locked||busy||!dirty}>{busy?localizeText('Saving…'):dirty?localizeText('Save receipt design'):localizeText('All changes saved')}</button>
     {dirty&&!busy&&<button type="button" className="focusTextLink" onClick={()=>{setDesign(JSON.parse(saved));setError('');setNotice('')}}>{localizeText('Discard changes')}</button>}</div>
   </form>
   <section className="rdPreview" aria-label={localizeText('Receipt preview')}><p className="focusEyebrow">{localizeText('Live preview · sample guest')}</p><CustomReceipt design={design} facts={sample} preview/></section>
  </div>
 </main>;
}
