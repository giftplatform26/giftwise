'use client';
import {useCallback,useEffect,useState} from 'react';
import {localizeText} from '../lib/localization.mjs';
import {useLocalizationRender} from './LocalizationProvider';
import {commercialApi} from './CommercePurchase';

type Status={configured:boolean;mode:'TEST'|'LIVE'|'NOT_CONFIGURED'|'UNKNOWN';checkoutSupported:boolean;platformEnabled:boolean;message:string};
type View={revision:number;controls:{channel:string;configured:boolean|null}[]};
const base='/api/admin/console/payments';

/** Platform-wide on/off for Razorpay checkout. Uses the existing audited payment controls (channel RAZORPAY, scope PLATFORM). */
export default function RazorpaySwitch({onChanged}:{onChanged?:()=>void}){useLocalizationRender();
  const [status,setStatus]=useState<Status|null>(null),[view,setView]=useState<View|null>(null);
  const [reason,setReason]=useState(''),[busy,setBusy]=useState(false),[error,setError]=useState(''),[notice,setNotice]=useState('');
  const load=useCallback(async()=>{try{const [s,v]=await Promise.all([commercialApi<Status>(`${base}/razorpay-status`),commercialApi<View>(`${base}/controls?scope=PLATFORM`)]);setStatus(s);setView(v);setError('')}catch(e){setError(e instanceof Error?e.message:'Could not load Razorpay status')}},[]);
  useEffect(()=>{void load()},[load]);
  const on=!!status?.platformEnabled;
  async function toggle(){if(!view||busy)return;if(!reason.trim()){setError(localizeText('Add a short reason so the change is recorded in the history.'));return}
    setBusy(true);setError('');setNotice('');
    try{await commercialApi(`${base}/controls/RAZORPAY?scope=PLATFORM`,{revision:view.revision,enabled:!on,reason:reason.trim()},'PUT');
      setReason('');setNotice(on?localizeText('Razorpay checkout is now off for every event. Guests can still pay by UPI or bank transfer.'):localizeText('Razorpay checkout is now on. Individual hosts or events can still be turned off below.'));
      await load();onChanged?.()}
    catch(e){setError(e instanceof Error?e.message:'Could not change Razorpay checkout')}finally{setBusy(false)}}
  return <section className="acPanel razorpaySwitch" aria-labelledby="razorpay-switch-title">
    <div className="razorpaySwitchHead"><div><h2 id="razorpay-switch-title">{localizeText('Razorpay checkout')}</h2>
      <p>{localizeText('Card, UPI and netbanking through Razorpay. Fees apply; guests see the platform fee before paying.')}</p></div>
      <span className={`razorpayState ${on?'is-on':'is-off'}`} role="status">{status?(on?localizeText('On'):localizeText('Off')):'…'}</span></div>
    {status&&<p className={`razorpayKeys razorpayKeys--${status.mode.toLowerCase()}`}><strong>{status.mode==='TEST'?localizeText('Test keys'):status.mode==='LIVE'?localizeText('Live keys'):status.mode==='NOT_CONFIGURED'?localizeText('No keys'):localizeText('Unrecognised key')}</strong> · {localizeText(status.message)}</p>}
    <label>{localizeText('Reason for this change')}<input value={reason} maxLength={500} onChange={e=>{setReason(e.target.value);setError('')}} placeholder={on?localizeText('e.g. Pausing card payments during review'):localizeText('e.g. Enabling card payments for launch')}/></label>
    <button type="button" className={on?'acDanger':'acPrimary'} disabled={busy||!view||(!on&&!status?.configured)} onClick={()=>void toggle()}>
      {busy?localizeText('Saving…'):on?localizeText('Turn Razorpay off'):localizeText('Turn Razorpay on')}</button>
    {!on&&status&&!status.configured&&<p className="finePrint">{localizeText('Add RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET to the backend environment first.')}</p>}
    {error&&<p role="alert" className="focusError">{localizeText(error)}</p>}{notice&&<p role="status">{notice}</p>}
    <p className="finePrint">{localizeText('Direct UPI and bank transfers to hosts are free and stay available either way. Every change is kept in the payment control history below.')}</p>
  </section>;
}
