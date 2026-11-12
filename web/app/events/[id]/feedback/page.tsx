'use client';
import {useCallback,useEffect,useState} from 'react';
import {useParams} from 'next/navigation';
import {localizeText} from '../../../../lib/localization.mjs';
import {useLocalizationRender} from '../../../../components/LocalizationProvider';
import {hostRequest} from '../../../../lib/host-api.mjs';
import '../../../../components/gift-books.css';
import '../../../../components/guest-forms.css';

type Response={id:string;guestName:string|null;overall:number;food:number|null;venue:number|null;arrangements:number|null;hospitality:number|null;comment:string|null;answer:string|null;hidden:boolean;createdAt:string};
type Results={settings:{collecting:boolean;question:string|null};summary:{count:number;overall:number|null;aspects:Record<string,number|null>;stars:number[]};responses:Response[];publicPath:string};
const ASPECT:Record<string,string>={food:'Food',venue:'Venue',arrangements:'Arrangements',hospitality:'Hospitality'};
const stars=(n:number|null)=>n==null?'—':'★'.repeat(Math.round(n))+'☆'.repeat(5-Math.round(n));

/** Guest feedback: open it after the celebration, share the link, and read ratings and comments. */
export default function Feedback(){useLocalizationRender();
 const {id}=useParams();const eventId=String(id??'');const route=`/v1/events/${eventId}/feedback`;
 const [r,setR]=useState<Results|null>(null),[error,setError]=useState(''),[notice,setNotice]=useState(''),[question,setQuestion]=useState('');
 const load=useCallback(async()=>{try{const v=await hostRequest(fetch,route) as Results;setR(v);setQuestion(v.settings.question??'');setError('')}catch(e){setError(e instanceof Error?e.message:'Could not load feedback')}},[route]);
 useEffect(()=>{if(/^[0-9a-f-]{36}$/i.test(eventId))void load()},[eventId,load]);
 async function save(collecting:boolean){try{await hostRequest(fetch,`${route}/settings`,{method:'PUT',payload:{collecting,question:question||null}});setNotice(localizeText(collecting?'Feedback is open. Share the link with your guests.':'Feedback is closed.'));await load()}catch(e){setError(e instanceof Error?e.message:'Could not save')}}
 const link=r&&typeof window!=='undefined'?`${window.location.origin}${r.publicPath}`:'';const s=r?.summary;
 return <main className="focusPage gbPage">
  <header className="focusPageHeading"><div><p className="focusEyebrow">{localizeText('After the celebration')}</p><h1>{localizeText('Guest feedback')}</h1>
   <p>{localizeText('Ask guests how it went: star ratings for food, venue, arrangements and hospitality, plus a comment. Open it when the celebration is over.')}</p></div>
   {r&&r.responses.length>0&&<a className="focusSecondary gbButtonLink" href={`/backend/api${route}/export.csv`} download>{localizeText('Download CSV')}</a>}</header>
  {error&&<p className="focusError" role="alert">{localizeText(error)}</p>}{notice&&<p className="gbNotice" role="status">{notice}</p>}
  {r&&<section className="gbEntry"><label>{localizeText('Your own question (optional)')}<input value={question} maxLength={200} onChange={e=>setQuestion(e.target.value)} placeholder={localizeText('e.g. What was your favourite moment?')}/></label>
   <div className="gbEntryActions">{r.settings.collecting?<><button type="button" className="focusSecondary" onClick={()=>void save(true)}>{localizeText('Save question')}</button><button type="button" className="gbDanger" onClick={()=>void save(false)}>{localizeText('Close feedback')}</button></>
    :<button type="button" className="focusPrimary" onClick={()=>void save(true)}>{localizeText('Open feedback')}</button>}</div>
   {r.settings.collecting&&<p className="gbFine">{localizeText('Link for guests (also shown on the invitation)')}: <code>{link}</code> <button type="button" className="gbPlain" onClick={()=>{void navigator.clipboard?.writeText(link);setNotice(localizeText('Link copied.'))}}>{localizeText('Copy')}</button>
    {' '}<a href={`https://wa.me/?text=${encodeURIComponent(localizeText('Thank you for celebrating with us! Tell us how it went: {{u}}').replace('{{u}}',link))}`} target="_blank" rel="noopener noreferrer">{localizeText('Send on WhatsApp')}</a></p>}</section>}
  {s&&s.count>0&&<section className="gbStats"><div><span>{localizeText('Responses')}</span><strong>{s.count}</strong></div><div><span>{localizeText('Overall')}</span><strong>{s.overall?.toFixed(1)}</strong><small>{stars(s.overall)}</small></div>
   {Object.entries(s.aspects).map(([k,v])=><div key={k}><span>{localizeText(ASPECT[k]??k)}</span><strong>{v==null?'—':v.toFixed(1)}</strong></div>)}</section>}
  {s&&s.count>0&&<p className="gbFine">{[5,4,3,2,1].map(n=>`${n}★ ${s.stars[n-1]}`).join(' · ')}</p>}
  {r&&(r.responses.length===0?<p className="gbEmpty">{localizeText(r.settings.collecting?'No responses yet.':'Open feedback to start collecting responses.')}</p>:
   <ul className="gbRows">{r.responses.map(x=><li key={x.id} style={x.hidden?{opacity:.5}:undefined}><div><span className="gbWho"><strong>{stars(x.overall)} {x.guestName||localizeText('A guest')}</strong>
    <small>{Object.keys(ASPECT).filter(k=>x[k as keyof Response]!=null).map(k=>`${localizeText(ASPECT[k])} ${x[k as keyof Response]}★`).join(' · ')}</small>
    {x.comment&&<span>{x.comment}</span>}{x.answer&&<small>{r.settings.question?`${r.settings.question} — `:''}{x.answer}</small>}</span>
    <button type="button" className="gbPlain" onClick={()=>void hostRequest(fetch,`${route}/${x.id}/hide`,{method:'POST',payload:{hidden:!x.hidden}}).then(load)}>{localizeText(x.hidden?'Count again':'Hide')}</button></div></li>)}</ul>)}
 </main>;
}
