'use client';
import {trackedCopy} from '../lib/interaction-analytics.mjs';
/* localization:instrumented */
import {localize,localizeText} from '../lib/localization.mjs';
import {useLocalizationRender} from './LocalizationProvider';

import {useEffect,useState} from 'react';
import './guest-upi-qr.css';
import {useExperience} from './ExperienceProvider';
type Destination={id:string;method:'UPI'|'BANK';preferred:boolean;label:string;upiId:string|null;
  qrPngBase64:string|null;beneficiaryName:string|null;accountNumber:string|null;ifsc:string|null};
export default function GuestVerifiedDestination({token,method,destinationId,onSelect,amountPaise,inlineQr=false}:{
  token:string;method:'upi'|'bank';destinationId:string;onSelect:(id:string)=>void;amountPaise?:number;inlineQr?:boolean}){useLocalizationRender();
  const {mode}=useExperience();const focused=mode==='FOCUSED'||inlineQr;const [copyNotice,setCopyNotice]=useState('');
  const [rows,setRows]=useState<Destination[]>([]);
  const [loading,setLoading]=useState(true);
  const [error,setError]=useState('');
  const [retry,setRetry]=useState(0);
  const [qrOpen,setQrOpen]=useState(false);
  useEffect(()=>{let active=true;setLoading(true);setError('');
    fetch(`/backend/api/public/payments/verified-destinations/${encodeURIComponent(token)}`,
      {credentials:'same-origin',cache:'no-store'})
      .then(async r=>{if(!r.ok)throw new Error('Unavailable');return r.json() as Promise<Destination[]>})
      .then(data=>{if(active)setRows(data)})
      .catch(()=>{if(active){setRows([]);setError('Could not load payment details.')}})
      .finally(()=>{if(active)setLoading(false)});
    return()=>{active=false};
  },[token,retry]);
  const eligible=rows.filter(d=>d.method===method.toUpperCase() && d.preferred && (method==='bank' || Boolean(d.qrPngBase64 && d.upiId)));
  const selected=eligible.find(d=>d.id===destinationId);
  useEffect(()=>{setQrOpen(method==='upi' && Boolean(selected?.qrPngBase64))},[method,selected?.id,selected?.qrPngBase64]);
  useEffect(()=>{
    const preferred=eligible.find(d=>d.preferred)??eligible[0];
    if(!eligible.some(d=>d.id===destinationId))onSelect(preferred?.id??'');
  // eslint-disable-next-line react-hooks/exhaustive-deps
  },[rows,method,destinationId]);
  return <div>
    <h3>{method==='upi'?localize("ui.41603529daaec42c",{defaultValue:"Pay with UPI / QR"}):localize("ui.1c7c52852a622029",{defaultValue:"Pay by bank transfer"})}</h3>
    {loading?<p role="status">{localize("ui.bd4dee85230b343f",{defaultValue:"Checking eligible payment destinations…"})}</p>:
      error?<p role="alert">{localizeText(error)} <button type="button" onClick={()=>setRetry(v=>v+1)}>{localize("ui.942087cc2d41e013",{defaultValue:"Retry"})}</button></p>:eligible.length===0?<p role="status">{method==='upi'?
        localize("ui.2971c4992c8102c1",{defaultValue:"No eligible preferred UPI QR is currently available. Please do not make a transfer yet."}):
        localize("ui.e2679111c99736c6",{defaultValue:"No eligible preferred bank account is currently available. Please do not make a transfer yet."})}</p>:
      <>
        <label>{localize("ui.6f17cdcd888c485e",{defaultValue:"Preferred recipient"})}<select value={selected?.id??''} onChange={e=>onSelect(e.target.value)}>
            {eligible.map(d=><option key={d.id} value={d.id}>{d.label}{d.preferred?localize("ui.3089404fe2df7d81",{defaultValue:" (Preferred)"}):''}</option>)}
          </select>
        </label>
        {selected?.method==='UPI'&&selected.qrPngBase64&&focused&&<div className="focusInlineQr"><p>{localize("ui.dda0bcdd5e698853",{defaultValue:"UPI ID: "})}<strong>{selected.upiId}</strong></p><a className="focusPrimary" href={`upi://pay?pa=${encodeURIComponent(selected.upiId||'')}&cu=INR${amountPaise&&Number.isSafeInteger(amountPaise)&&amountPaise>0?`&am=${(amountPaise/100).toFixed(2)}`:''}`}>{localize("ui.ea08259d6058041b",{defaultValue:"Open UPI app"})}</a><details><summary>{localize("ui.25092f9e4517c617",{defaultValue:"Pay using another device"})}</summary><img src={`data:image/png;base64,${selected.qrPngBase64}`} alt={localize("ui.975f4798eb2bf5e0",{defaultValue:"Selected recipient UPI payment QR"})} width={240} height={240}/><a download="payment-qr.png" href={`data:image/png;base64,${selected.qrPngBase64}`}>{localize("ui.4f1a8fb78985c377",{defaultValue:"Save QR image"})}</a></details><button type="button" onClick={()=>trackedCopy(selected.upiId||'').then(()=>setCopyNotice('UPI ID copied.')).catch(()=>setCopyNotice('Select the UPI ID above to copy it.'))}>{localize("ui.56cab81c15312e7c",{defaultValue:"Copy UPI ID"})}</button>{copyNotice&&<p role="status">{copyNotice}</p>}<p className="finePrint">{localize("ui.424a9b44f3180dd2",{defaultValue:"Check the recipient and amount in your payment app. Return here with your transaction reference."})}</p></div>}
        {selected?.method==='UPI'&&selected.qrPngBase64&&!focused&&<div>
          <button type="button" onClick={()=>setQrOpen(true)}>{localize("ui.a3c4e7c1a522f3dc",{defaultValue:"Show UPI QR"})}</button>
          {!focused&&qrOpen&&<div className="guestUpiQrOverlay" role="presentation" onClick={()=>setQrOpen(false)}>
            <div role="dialog" aria-modal="true" aria-label={localize("ui.96843d56fa891752",{defaultValue:"UPI payment QR"})} className="guestUpiQrDialog" onClick={e=>e.stopPropagation()}>
              <button type="button" onClick={()=>setQrOpen(false)} aria-label={localize("ui.0d0d05105e22660a",{defaultValue:"Close QR popup"})}>{localize("ui.e61997c6997c5afa",{defaultValue:"✕ Close"})}</button>
              <h3>{localize("ui.fb891e4b3a9d20ae",{defaultValue:"Send your gift with UPI"})}</h3>
              {/* eslint-disable-next-line @next/next/no-img-element */}
              <img src={`data:image/png;base64,${selected.qrPngBase64}`} alt={localize("ui.a9dac5b0bfa6fcbd",{defaultValue:"Eligible recipient UPI payment QR"})} width={240} height={240}/>
              <p>{localize("ui.dda0bcdd5e698853",{defaultValue:"UPI ID: "})}<strong>{selected.upiId}</strong></p>
              <p>{localize("ui.e510fef19c621a5e",{defaultValue:"After paying, enter your UPI transaction reference in the form."})}</p>
            </div>
          </div>}
        </div>}
        {selected?.method==='BANK'&&<div>
          <p>{localize("ui.c869ac7b408d76c2",{defaultValue:"Beneficiary: "})}<strong>{selected.beneficiaryName}</strong></p>
          <p>{localize("ui.883ceeb999b93b8b",{defaultValue:"Account number: "})}<strong>{selected.accountNumber}</strong></p>
          <p>{localize("ui.0e467d616fa1b829",{defaultValue:"IFSC: "})}<strong>{selected.ifsc}</strong></p>
          <p>{localize("ui.50a6acb0ce6978cc",{defaultValue:"Use your banking app to make the transfer. Keep the UTR for confirmation."})}</p>
        </div>}
      </>}
  </div>;
}
