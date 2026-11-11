'use client';
import {useCallback,useEffect,useState} from 'react';
import {useParams} from 'next/navigation';
import {localizeText} from '../../../../lib/localization.mjs';
import {useLocalizationRender} from '../../../../components/LocalizationProvider';
import {hostRequest} from '../../../../lib/host-api.mjs';
import '../../../../components/gift-books.css';
import '../../../../components/functions.css';

type Member={userId:string;name:string;role:string;joinedAt:string};
type Invite={id:string;label:string;role:string;createdAt:string;expiresAt:string;status:string;acceptedBy:string|null};
type Team={members:Member[];invites:Invite[]};
const ROLES:[string,string,string[]][]=[
 ['CO_HOST','Co-host',['Gift register (add, edit, export)','Guests, RSVPs and wishes','Live venue wall','Schedule & functions']],
 ['GIFT_DESK','Gift-desk helper',['Add envelopes and gifts to the register','See the register to avoid duplicates']]];
const ROLE_NAME:Record<string,string>={CO_HOST:'Co-host',GIFT_DESK:'Gift-desk helper'};
const date=(v:string)=>new Intl.DateTimeFormat(undefined,{day:'numeric',month:'short'}).format(new Date(v));

export default function CoHosts(){useLocalizationRender();
 const {id}=useParams();const eventId=String(id??'');const route=`/v1/events/${eventId}/co-hosts`;
 const [team,setTeam]=useState<Team|null>(null),[error,setError]=useState(''),[label,setLabel]=useState(''),[role,setRole]=useState('CO_HOST'),[busy,setBusy]=useState(false),[link,setLink]=useState<{url:string;label:string}|null>(null),[notice,setNotice]=useState('');
 const load=useCallback(async()=>{try{setTeam(await hostRequest(fetch,route) as Team);setError('')}catch(e){setError(e instanceof Error?e.message:'Could not load your team')}},[route]);
 useEffect(()=>{if(/^[0-9a-f-]{36}$/i.test(eventId))void load()},[eventId,load]);
 async function create(e:React.FormEvent){e.preventDefault();if(!label.trim()){setError('Add a name for this invite');return}setBusy(true);setError('');
  try{const r=await hostRequest(fetch,`${route}/invites`,{method:'POST',payload:{label,role}}) as {token:string};setLink({url:`${window.location.origin}/join-team/${r.token}`,label});setLabel('');await load()}
  catch(err){setError(err instanceof Error?err.message:'Could not create the invite')}finally{setBusy(false)}}
 async function act(path:string,payload?:unknown,msg?:string){setBusy(true);setError('');try{await hostRequest(fetch,`${route}/${path}`,{method:'POST',payload});if(msg)setNotice(msg);await load()}catch(err){setError(err instanceof Error?err.message:'Could not save')}finally{setBusy(false)}}
 async function copy(){if(!link)return;try{await navigator.clipboard.writeText(link.url);setNotice('Invite link copied.')}catch{setNotice('Select the link and copy it.')}}
 const share=link?`https://wa.me/?text=${encodeURIComponent(localizeText('Please help me with our celebration on GiftWise. Open this link and sign in to join: {{url}}').replace('{{url}}',link.url))}`:'';

 return <main className="focusPage gbPage">
  <header className="focusPageHeading"><div><p className="focusEyebrow">{localizeText('Share the work')}</p><h1>{localizeText('Co-hosts & helpers')}</h1>
   <p>{localizeText('Let family and friends help on the day. Payments, receiving accounts, invitation design and event details always stay with you.')}</p></div></header>
  {error&&<p className="focusError" role="alert">{localizeText(error)}</p>}
  {notice&&<p className="gbNotice" role="status">{localizeText(notice)}</p>}
  <form className="gbEntry" onSubmit={create}>
   <h2>{localizeText('Invite someone')}</h2>
   <label>{localizeText('Who is this for?')}<input value={label} maxLength={80} onChange={e=>setLabel(e.target.value)} placeholder={localizeText('e.g. Cousin Priya')}/><small>{localizeText('Only you see this name. It helps you recognise the invite.')}</small></label>
   <div className="chRoles" role="radiogroup" aria-label={localizeText('Role')}>{ROLES.map(([k,name,can])=><label key={k} className="chRole"><input type="radio" name="role" checked={role===k} onChange={()=>setRole(k)}/><span><strong>{localizeText(name)}</strong><ul>{can.map(c=><li key={c}>{localizeText(c)}</li>)}</ul></span></label>)}</div>
   <div className="gbEntryActions"><button className="focusPrimary" disabled={busy}>{localizeText('Create invite link')}</button></div>
  </form>
  {link&&<section className="focusPanel chLink" aria-live="polite"><h2>{localizeText('Invite link for {{name}}').replace('{{name}}',link.label)}</h2>
   <p>{localizeText('Send it privately. It works once and expires in 7 days. They will need to sign in or create a free account.')}</p>
   <div className="lwCopy"><input readOnly value={link.url} onFocus={e=>e.currentTarget.select()} aria-label={localizeText('Invite link')}/><button type="button" className="focusSecondary" onClick={()=>void copy()}>{localizeText('Copy')}</button>
    <a className="focusSecondary gbButtonLink" href={share} target="_blank" rel="noopener noreferrer">{localizeText('Share on WhatsApp')}</a></div>
   <p className="gbFine">{localizeText('For your security this link is shown only now.')}</p></section>}
  {team&&<section className="gbList"><h2 className="gbSubhead">{localizeText('Your team')}</h2>
   {team.members.length===0?<p className="gbEmpty">{localizeText('No one has joined yet.')}</p>:<ul className="gbRows">{team.members.map(m=><li key={m.userId}><div>
    <span className="gbWho"><strong>{m.name}</strong><small>{localizeText('Joined {{date}}').replace('{{date}}',date(m.joinedAt))}</small></span>
    <span className="chMemberTools"><select aria-label={localizeText('Role for {{name}}').replace('{{name}}',m.name)} value={m.role} disabled={busy} onChange={e=>void act(`${m.userId}/role`,{role:e.target.value},'Role updated.')}>{ROLES.map(([k,n])=><option key={k} value={k}>{localizeText(n)}</option>)}</select>
     <button type="button" className="gbDanger" disabled={busy} onClick={()=>{if(confirm(localizeText('Remove {{name}}? They lose access straight away.').replace('{{name}}',m.name)))void act(`${m.userId}/remove`,undefined,'Removed.')}}>{localizeText('Remove')}</button></span></div></li>)}</ul>}
   {team.invites.length>0&&<><h3 className="gbSubhead">{localizeText('Invites')}</h3><ul className="gbRows">{team.invites.map(i=><li key={i.id}><div>
    <span className="gbWho"><strong>{i.label}</strong><small>{localizeText(ROLE_NAME[i.role])} · {i.status==='ACCEPTED'?localizeText('Joined as {{name}}').replace('{{name}}',i.acceptedBy??''):i.status==='OPEN'?localizeText('Waiting · expires {{date}}').replace('{{date}}',date(i.expiresAt)):localizeText(i.status==='REVOKED'?'Cancelled':'Expired')}</small></span>
    {i.status==='OPEN'&&<button type="button" className="gbPlain" disabled={busy} onClick={()=>void act(`invites/${i.id}/revoke`,undefined,'Invite cancelled.')}>{localizeText('Cancel invite')}</button>}</div></li>)}</ul></>}
  </section>}
 </main>;
}
