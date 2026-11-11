'use client';
import {localizeText} from '../lib/localization.mjs';
import {useLocalizationRender} from './LocalizationProvider';
import {inr} from '../lib/focused.mjs';
import './custom-receipt.css';

export type ReceiptDesign={layout:'CLASSIC'|'ELEGANT'|'FESTIVE'|'MINIMAL';accentColor:string;title:string;headerText:string|null;headerSubtext:string|null;
  thankYouMessage:string|null;footerText:string|null;signatureName:string|null;showGuestName:boolean;showGiftName:boolean;showEventDate:boolean;
  showPaymentMethod:boolean;showPaymentCode:boolean;logoDataUrl:string|null};
export type ReceiptFacts={receiptNumber:string|null;amountPaise:number;guestName:string;giftName:string|null;eventTitle:string;eventDate:string|null;
  method:'UPI'|'BANK';paymentCode:string|null;createdAt:string;status:string;hostDecidedAt?:string|null};

const date=(value:string|null|undefined,withTime=false)=>{if(!value)return '';const d=new Date(value.length===10?`${value}T12:00:00`:value);
  return Number.isNaN(d.getTime())?'':new Intl.DateTimeFormat(undefined,withTime?{dateStyle:'medium',timeStyle:'short'}:{dateStyle:'long'}).format(d)};
const safeColor=(value:string)=>/^#[0-9A-Fa-f]{6}$/.test(value)?value:'#6336B5';

/** Host-designed receipt. All text is rendered as plain text; the logo is a server re-encoded PNG data URL. */
export default function CustomReceipt({design,facts,preview=false}:{design:ReceiptDesign;facts:ReceiptFacts;preview?:boolean}){useLocalizationRender();
  const confirmed=facts.status==='HOST_CONFIRMED';
  const logo=design.logoDataUrl&&/^data:image\/png;base64,[A-Za-z0-9+/=]+$/.test(design.logoDataUrl)?design.logoDataUrl:null;
  const rows:[string,string][]=[
    ['Receipt no.',facts.receiptNumber||'—'],
    ['Date',date(facts.createdAt,true)],
    ...(design.showGuestName?[['Received from',facts.guestName] as [string,string]]:[]),
    ...(design.showGiftName?[['Toward',facts.giftName||localizeText('The celebration')] as [string,string]]:[]),
    ...(design.showEventDate&&facts.eventDate?[['Celebration date',date(facts.eventDate)] as [string,string]]:[]),
    ...(design.showPaymentMethod?[['Paid by',facts.method==='UPI'?localizeText('UPI transfer'):localizeText('Bank transfer')] as [string,string]]:[]),
    ...(design.showPaymentCode&&facts.paymentCode?[['Payment code',facts.paymentCode] as [string,string]]:[]),
  ];
  return <article className={`customReceipt customReceipt--${design.layout.toLowerCase()}${preview?' customReceipt--preview':''}`} style={{'--receipt-accent':safeColor(design.accentColor)} as React.CSSProperties} aria-label={design.title}>
    <header className="customReceiptHeader">
      {logo&&<img src={logo} alt="" className="customReceiptLogo" width={96} height={96}/>}
      <p className="customReceiptTitle">{design.title}</p>
      {design.headerText&&<h2>{design.headerText}</h2>}
      {design.headerSubtext&&<p className="customReceiptSubtext">{design.headerSubtext}</p>}
    </header>
    {!confirmed&&<p className="customReceiptPending" role="status">{facts.status==='REJECTED'?localizeText('Not yet confirmed by the host'):localizeText('Awaiting host confirmation')}</p>}
    <div className="customReceiptAmount"><span>{localizeText('Amount')}</span><strong>{inr(facts.amountPaise)}</strong></div>
    <dl className="customReceiptRows">{rows.map(([label,value])=><div key={label}><dt>{localizeText(label)}</dt><dd>{value}</dd></div>)}</dl>
    {design.thankYouMessage&&<p className="customReceiptThanks">{design.thankYouMessage}</p>}
    {design.signatureName&&<p className="customReceiptSignature"><span>{design.signatureName}</span></p>}
    <footer className="customReceiptFooter">
      {design.footerText&&<p>{design.footerText}</p>}
      <p className="customReceiptFine">{confirmed?localizeText('Confirmed received by the host on {{date}}.').replace('{{date}}',date(facts.hostDecidedAt,true)):localizeText('This receipt becomes final when the host confirms the payment arrived.')} {localizeText('Sent directly to the host · GiftWise does not hold this money.')}</p>
    </footer>
  </article>;
}
