'use client';
import {useCallback,useEffect,useState} from 'react';
import {localize,localizeText} from '../../lib/localization.mjs';
import {useLocalizationRender} from '../LocalizationProvider';
import {commercialApi} from '../CommercePurchase';
import {ConfirmDialog,StatusPill,Callout} from './AdminUi';

type Scope='PLATFORM'|'HOST'|'EVENT';
type Control={channel:string;configured:boolean|null;platformEnabled:boolean;hostEnabled:boolean;eventEnabled:boolean;effectiveEnabled:boolean};
type View={revision:number;scope:Scope;scopeId:string;controls:Control[]};
type RazorpayStatus={configured:boolean;mode:'TEST'|'LIVE'|'NOT_CONFIGURED'|'UNKNOWN';checkoutSupported:boolean;platformEnabled:boolean;message:string};
const base='/api/admin/console/payments';

/** Plain-language name and help text for each payment channel stored in payment_channel_controls. */
export const PAYMENT_METHODS:{channel:string;label:string;help:string}[]=[
 {channel:'UPI',label:'UPI payments',help:'Guests pay the host directly with a UPI app, QR code or UPI ID. Free; the host confirms receipt.'},
 {channel:'BANK',label:'Bank transfers',help:'Guests transfer to the host’s bank account (NEFT/IMPS). Free; the host confirms receipt.'},
 {channel:'RAZORPAY',label:'Razorpay checkout',help:'Card, UPI and netbanking through Razorpay. Provider fees apply and guests see the platform fee before paying.'},
 {channel:'ROUTING',label:'Razorpay Route (pay-out to host)',help:'Razorpay splits each checkout payment and settles the host’s share to their linked account. Needs Razorpay checkout to be on.'},
];

/**
 * One switch per payment method for the whole platform, one host or one event.
 * The platform switch is the master. A host or event switch can only turn a method off for that host or event,
 * so turning it back on clears the override and follows the level above. Every change needs a reason and is kept
 * in the payment control history.
 */
export default function AdminPaymentMethods({scope,scopeId,name,onChanged}:{scope:Scope;scopeId?:string;name?:string;onChanged?:()=>void}){useLocalizationRender();
 const [view,setView]=useState<View|null>(null),[razorpay,setRazorpay]=useState<RazorpayStatus|null>(null);
 const [error,setError]=useState(''),[notice,setNotice]=useState(''),[busy,setBusy]=useState(false);
 const [pending,setPending]=useState<{channel:string;on:boolean}|null>(null),[reason,setReason]=useState('');
 const query=`?scope=${scope}${scope==='PLATFORM'?'':`&scopeId=${encodeURIComponent(scopeId||'')}`}`;
 const load=useCallback(async()=>{setError('');
  try{const [v,r]=await Promise.all([commercialApi<View>(`${base}/controls${query}`),scope==='PLATFORM'?commercialApi<RazorpayStatus>(`${base}/razorpay-status`):Promise.resolve(null)]);setView(v);setRazorpay(r);}
  catch(e){setError(e instanceof Error?e.message:'Could not load payment methods');}},[query,scope]);
 useEffect(()=>{setView(null);setNotice('');void load();},[load]);

 const control=(channel:string)=>view?.controls.find(c=>c.channel===channel);
 /** Whether the switch is on at this level (not whether guests can use it: a level above may still turn it off). */
 const switchOn=(c:Control)=>scope==='PLATFORM'?c.configured===true:c.configured!==false;
 const parentOn=(c:Control)=>scope==='PLATFORM'?true:scope==='HOST'?c.platformEnabled:c.hostEnabled;
 function blockedReason(c:Control):string{
  const on=switchOn(c);
  if(!parentOn(c))return scope==='HOST'?'Off for the whole platform. Turn it on under Payment methods first.':'Off for this host or the whole platform.';
  if(c.channel==='ROUTING'&&!on&&scope==='PLATFORM')return 'Razorpay Route is not connected in this version, so it cannot be turned on yet. Normal Razorpay checkout is not affected.';
  if(c.channel==='ROUTING'&&!on&&!control('RAZORPAY')?.effectiveEnabled)return 'Turn on Razorpay checkout first.';
  if(c.channel==='RAZORPAY'&&scope==='PLATFORM'&&!on&&razorpay&&!razorpay.configured)return 'Add RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET to the backend environment first.';
  return '';
 }
 function status(c:Control):string{
  if(c.effectiveEnabled)return scope==='PLATFORM'?'Guests can use it on every event unless a host or event turns it off.':scope==='HOST'?'Guests can use it on this host’s events.':'Guests can use it on this event.';
  if(c.channel==='ROUTING'&&switchOn(c)&&parentOn(c))return 'Waiting for Razorpay checkout and Route to be on for the platform.';
  if(!parentOn(c))return scope==='HOST'?'Off for the whole platform.':'Off for this host or the whole platform.';
  return scope==='PLATFORM'?'Guests cannot use it anywhere.':scope==='HOST'?'Turned off for this host.':'Turned off for this event.';
 }
 async function apply(){if(!pending||!view||busy||!reason.trim())return;setBusy(true);setError('');setNotice('');
  // At host and event level "on" clears the override (inherit); only "off" is stored.
  const enabled=scope==='PLATFORM'?pending.on:pending.on?null:false;
  try{const saved=await commercialApi<View>(`${base}/controls/${pending.channel}${query}`,{revision:view.revision,enabled,reason:reason.trim()},'PUT');
   setView(saved);const label=PAYMENT_METHODS.find(m=>m.channel===pending.channel)?.label||pending.channel;
   setNotice(localize(pending.on?'admin.payments.savedOn':'admin.payments.savedOff',{defaultValue:pending.on?'{{method}} is now on{{where}}.':'{{method}} is now off{{where}}.',method:localizeText(label),where:scope==='PLATFORM'?'':name?` · ${name}`:''}));
   setPending(null);setReason('');onChanged?.();
   if(scope==='PLATFORM')void load();}
  catch(e){setError(e instanceof Error?e.message:'Could not change the payment method');setPending(null);if(String(e).includes('reload'))void load();}
  finally{setBusy(false);}}

 const titleId=`payment-methods-${scope.toLowerCase()}-title`;
 const pendingMethod=pending?PAYMENT_METHODS.find(m=>m.channel===pending.channel):null;
 return <section className="acPanel" aria-labelledby={titleId} aria-busy={!view&&!error}>
  <div className="admPanelHead"><div><h2 id={titleId}>{localizeText(scope==='PLATFORM'?'Payment methods':scope==='HOST'?'Payment methods for this host':'Payment methods for this event')}</h2>
   {name&&<p className="admHostName">{name}</p>}</div>
   <button type="button" className="admButton" disabled={busy} onClick={()=>void load()}>{localizeText('Refresh')}</button></div>
  <Callout><p>{localizeText(scope==='PLATFORM'?'These are the master switches. Turning a method off here hides it from every host and event. Hosts and events can only turn a method off for themselves. Changes affect new payments only; existing payments still reconcile.':'This host or event follows the platform switches. Turn a method off to hide it here only. Turning it back on follows the platform setting again.')}</p></Callout>
  {notice&&<div role="status" className="admNotice">{notice}</div>}
  {error&&<p role="alert" className="acError">{localizeText(error)} <button type="button" className="admLinkButton" onClick={()=>void load()}>{localizeText('Retry')}</button></p>}
  {!view&&!error&&<p role="status">{localizeText('Loading payment methods…')}</p>}
  {view&&<div className="acRows">{PAYMENT_METHODS.map(m=>{const c=control(m.channel);if(!c)return null;const on=switchOn(c),blocked=blockedReason(c);
   return <label key={m.channel} className="admSwitchRow admPaymentRow">
    <span><strong>{localizeText(m.label)}</strong> <StatusPill on={c.effectiveEnabled} onLabel="Available" offLabel="Not available"/>
     <small>{localizeText(m.help)}</small>
     {parentOn(c)&&<small>{localizeText(status(c))}</small>}
     {m.channel==='RAZORPAY'&&razorpay&&<small className={`razorpayKeys razorpayKeys--${razorpay.mode.toLowerCase()}`}><strong>{localizeText(razorpay.mode==='TEST'?'Test keys':razorpay.mode==='LIVE'?'Live keys':razorpay.mode==='NOT_CONFIGURED'?'No keys':'Unrecognised key')}</strong> · {localizeText(razorpay.message)}</small>}
     {blocked&&<small className="admHint">{localizeText(blocked)}</small>}</span>
    <input type="checkbox" role="switch" checked={on&&parentOn(c)} disabled={busy||!!blocked}
     aria-label={localize('admin.payments.switch',{defaultValue:'{{method}}{{where}}',method:localizeText(m.label),where:scope==='PLATFORM'?' for the whole platform':name?` for ${name}`:''})}
     onChange={e=>{setReason('');setPending({channel:m.channel,on:e.target.checked});}}/>
   </label>;})}</div>}
  <ConfirmDialog open={!!pending} busy={busy} confirmDisabled={!reason.trim()} tone={pending?.on?'primary':'danger'}
   title={pending&&pendingMethod?localize(pending.on?'admin.payments.confirmOn':'admin.payments.confirmOff',{defaultValue:pending.on?'Turn on {{method}}{{where}}?':'Turn off {{method}}{{where}}?',method:localizeText(pendingMethod.label),where:scope==='PLATFORM'?' for everyone':name?` for ${name}`:''}):''}
   confirmLabel={localizeText(pending?.on?'Turn on':'Turn off')} onCancel={()=>{setPending(null);setReason('');}} onConfirm={()=>void apply()}>
   <p>{localizeText(pending?.on?(scope==='PLATFORM'?'Guests on every event will see this payment method, except where a host or event has turned it off.':'This removes the override, so the platform setting applies again.'):(scope==='PLATFORM'?'Guests on every event stop seeing this payment method straight away. Payments already started can still be confirmed.':'Guests stop seeing this payment method here straight away. Payments already started can still be confirmed.'))}</p>
   <label className="admField">{localizeText('Reason (kept in the payment history)')}<input autoComplete="off" required maxLength={500} value={reason} onChange={e=>setReason(e.target.value)} placeholder={localizeText(pending?.on?'e.g. Ready for launch after testing':'e.g. Paused while we look into a problem')}/></label>
  </ConfirmDialog>
 </section>;
}
