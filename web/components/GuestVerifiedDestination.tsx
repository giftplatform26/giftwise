'use client';
import {useEffect,useState} from 'react';
import './guest-upi-qr.css';
import {useExperience} from './ExperienceProvider';
type Destination={id:string;method:'UPI'|'BANK';preferred:boolean;label:string;upiId:string|null;
  qrPngBase64:string|null;beneficiaryName:string|null;accountNumber:string|null;ifsc:string|null};
export default function GuestVerifiedDestination({token,method,destinationId,onSelect,amountPaise}:{
  token:string;method:'upi'|'bank';destinationId:string;onSelect:(id:string)=>void;amountPaise?:number}){
  const {mode}=useExperience();const focused=mode==='FOCUSED';const [copyNotice,setCopyNotice]=useState('');
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
    <h3>{method==='upi'?'Pay with UPI / QR':'Pay by bank transfer'}</h3>
    {loading?<p role="status">Checking eligible payment destinations…</p>:
      error?<p role="alert">{error} <button type="button" onClick={()=>setRetry(v=>v+1)}>Retry</button></p>:eligible.length===0?<p role="status">{method==='upi'?
        'No eligible preferred UPI QR is currently available. Please do not make a transfer yet.':
        'No eligible preferred bank account is currently available. Please do not make a transfer yet.'}</p>:
      <>
        <label>Preferred recipient
          <select value={selected?.id??''} onChange={e=>onSelect(e.target.value)}>
            {eligible.map(d=><option key={d.id} value={d.id}>{d.label}{d.preferred?' (Preferred)':''}</option>)}
          </select>
        </label>
        {selected?.method==='UPI'&&selected.qrPngBase64&&focused&&<div className="focusInlineQr"><p>UPI ID: <strong>{selected.upiId}</strong></p><a className="focusPrimary" href={`upi://pay?pa=${encodeURIComponent(selected.upiId||'')}&cu=INR${amountPaise&&Number.isSafeInteger(amountPaise)&&amountPaise>0?`&am=${(amountPaise/100).toFixed(2)}`:''}`}>Open UPI app</a><details><summary>Pay using another device</summary><img src={`data:image/png;base64,${selected.qrPngBase64}`} alt="Selected recipient UPI payment QR" width={240} height={240}/><a download="payment-qr.png" href={`data:image/png;base64,${selected.qrPngBase64}`}>Save QR image</a></details><button type="button" onClick={()=>navigator.clipboard.writeText(selected.upiId||'').then(()=>setCopyNotice('UPI ID copied.')).catch(()=>setCopyNotice('Select the UPI ID above to copy it.'))}>Copy UPI ID</button>{copyNotice&&<p role="status">{copyNotice}</p>}<p className="finePrint">Check the recipient and amount in your payment app. Return here with your transaction reference.</p></div>}
        {selected?.method==='UPI'&&selected.qrPngBase64&&!focused&&<div>
          <button type="button" onClick={()=>setQrOpen(true)}>Show UPI QR</button>
          {!focused&&qrOpen&&<div className="guestUpiQrOverlay" role="presentation" onClick={()=>setQrOpen(false)}>
            <div role="dialog" aria-modal="true" aria-label="UPI payment QR" className="guestUpiQrDialog" onClick={e=>e.stopPropagation()}>
              <button type="button" onClick={()=>setQrOpen(false)} aria-label="Close QR popup">✕ Close</button>
              <h3>Send your gift with UPI</h3>
              {/* eslint-disable-next-line @next/next/no-img-element */}
              <img src={`data:image/png;base64,${selected.qrPngBase64}`} alt="Eligible recipient UPI payment QR" width={240} height={240}/>
              <p>UPI ID: <strong>{selected.upiId}</strong></p>
              <p>After paying, enter your UPI transaction reference in the form.</p>
            </div>
          </div>}
        </div>}
        {selected?.method==='BANK'&&<div>
          <p>Beneficiary: <strong>{selected.beneficiaryName}</strong></p>
          <p>Account number: <strong>{selected.accountNumber}</strong></p>
          <p>IFSC: <strong>{selected.ifsc}</strong></p>
          <p>Use your banking app to make the transfer. Keep the UTR for confirmation.</p>
        </div>}
      </>}
  </div>;
}
