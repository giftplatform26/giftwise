'use client';
import {useCallback,useEffect,useState} from 'react';
import {useParams,usePathname} from 'next/navigation';
import Link from 'next/link';
import {localizeText} from '../../../../lib/localization.mjs';
import {useLocalizationRender} from '../../../../components/LocalizationProvider';
import {hostRequest} from '../../../../lib/host-api.mjs';
import '../../../../components/gift-books.css';
import '../../../../components/seating.css';

type Pass={id:string;guestName:string;partySize:number;token:string;shortCode:string;phone:string|null;revoked:boolean;arrived:boolean};
type Data={passes:Pass[];withoutPass:number};

export default function GuestPasses(){useLocalizationRender();
 const {id}=useParams();const path=usePathname();const eventId=String(id??'');const route=`/v1/events/${eventId}/passes`;
 const [d,setD]=useState<Data|null>(null),[error,setError]=useState(''),[notice,setNotice]=useState(''),[busy,setBusy]=useState(false),[one,setOne]=useState({name:'',party:'1'});
 const load=useCallback(async()=>{try{setD(await hostRequest(fetch,route) as Data);setError('')}catch(e){setError(e instanceof Error?e.message:'Could not load passes')}},[route]);
 useEffect(()=>{if(/^[0-9a-f-]{36}$/i.test(eventId))void load()},[eventId,load]);
 async function generate(body:object){setBusy(true);setError('');try{const r=await hostRequest(fetch,`${route}/generate`,{method:'POST',payload:body}) as {created:number};setNotice(localizeText('{{n}} passes created.').replace('{{n}}',String(r.created)));await load()}catch(e){setError(e instanceof Error?e.message:'Could not create passes')}finally{setBusy(false)}}
 async function revoke(p:Pass){if(!confirm(localizeText('Cancel the pass for {{n}}?').replace('{{n}}',p.guestName)))return;try{await hostRequest(fetch,`${route}/${p.id}/revoke`,{method:'POST'});await load()}catch(e){setError(e instanceof Error?e.message:'Could not cancel')}}
 const link=(p:Pass)=>typeof window!=='undefined'?`${window.location.origin}/pass/${p.token}`:'';
 const wa=(p:Pass)=>{const text=localizeText('Hi {{n}}! Here is your pass for the celebration — show it at the entrance: {{url}}').replace('{{n}}',p.guestName.split(' ')[0]).replace('{{url}}',link(p));return p.phone?`https://wa.me/${p.phone.replace('+','')}?text=${encodeURIComponent(text)}`:`https://wa.me/?text=${encodeURIComponent(text)}`};
 return <main className="focusPage gbPage">
  <header className="focusPageHeading"><div><p className="focusEyebrow">{localizeText('Faster at the door')}</p><h1>{localizeText('Guest passes')}</h1>
   <p>{localizeText('A personal pass with a QR code for every family. At the entrance, scan it (or type its 6-letter code) in Guest check-in.')}</p></div></header>
  {error&&<p className="focusError" role="alert">{localizeText(error)}</p>}
  {notice&&<p className="gbNotice" role="status">{notice}</p>}
  {d&&<section className="gbEntry"><div className="gbEntryActions"><button type="button" className="focusPrimary" disabled={busy||d.withoutPass===0} onClick={()=>void generate({})}>{d.withoutPass>0?localizeText('Create passes for {{n}} guests').replace('{{n}}',String(d.withoutPass)):localizeText('Everyone expected has a pass')}</button>{d.passes.length>0&&<Link className="focusSecondary gbButtonLink" href={`${path}/print`}>{localizeText('Print all passes')}</Link>}</div>
   <form className="gbEntryRow" onSubmit={e=>{e.preventDefault();if(one.name.trim())void generate({guestName:one.name,partySize:Number(one.party)||1}).then(()=>setOne({name:'',party:'1'}))}}>
    <label className="gbGrow">{localizeText('Or add one guest')}<input value={one.name} maxLength={120} onChange={e=>setOne({...one,name:e.target.value})}/></label>
    <label>{localizeText('People')}<input inputMode="numeric" value={one.party} onChange={e=>setOne({...one,party:e.target.value.replace(/\D/g,'')})}/></label><button className="focusSecondary">{localizeText('Create')}</button></form></section>}
  {d&&(d.passes.length===0?<p className="gbEmpty">{localizeText('Passes are made from your guest list, RSVPs, schedule answers and WhatsApp guest list.')}</p>:
   <ul className="gbRows">{d.passes.map(p=><li key={p.id}><div><span className="gbWho"><strong>{p.guestName}</strong><small>{p.partySize} · {localizeText('code')} <b className="psCode">{p.shortCode}</b>{p.arrived?` · ${localizeText('arrived')}`:''}{p.revoked?` · ${localizeText('cancelled')}`:''}</small></span>
    {!p.revoked&&<span className="chMemberTools"><a className="focusSecondary gbButtonLink" href={wa(p)} target="_blank" rel="noopener noreferrer">{localizeText('Send')}</a><a href={link(p)} target="_blank" rel="noopener noreferrer">{localizeText('View')}</a><button type="button" className="gbPlain" onClick={()=>void revoke(p)}>{localizeText('Cancel')}</button></span>}</div></li>)}</ul>)}
 </main>;
}
