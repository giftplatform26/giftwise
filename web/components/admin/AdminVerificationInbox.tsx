'use client';
import {useEffect,useState} from 'react';
import {activeLocale,localize,localizeText} from '../../lib/localization.mjs';
import {useLocalizationRender} from '../LocalizationProvider';
import {commercialApi} from '../CommercePurchase';
import {sectionHref} from '../../lib/admin-sections.mjs';
import {EmptyState,Callout} from './AdminUi';

type Item={id:string;subject:string|null;owner:string|null;kind:string;amount_paise:number|null;created_at:string;host_confirmed?:boolean;group_id?:string};
type Queue={key:string;count:number;oldest:string|null;items:Item[]};
// Who acts on each queue and where. Administrators only act on showcase submissions and group switching;
// money and account verification belong to hosts, group creators and provider checks.
export const INBOX_QUEUES:Record<string,{title:string;who:string;action?:{label:string;section:string}}>={
 GUEST_TRANSFERS:{title:'Guest transfers awaiting host confirmation',who:'Guests reported a UPI or bank transfer. Only the host can confirm the money arrived, from their payments page.'},
 GROUP_REPORTS:{title:'Group contributions awaiting the group creator',who:'Guests reported paying into a group gift account. The group creator confirms receipt.'},
 HOST_ACCOUNTS:{title:'Host receiving accounts not yet verified',who:'Verification needs provider evidence. Hosts can confirm their own account; follow up with hosts whose accounts stay unverified.',action:{label:'Find the host in Users',section:'users'}},
 GROUP_ACCOUNTS:{title:'Group gift accounts not yet verified',who:'Only verified accounts can receive group contributions. Whether a group creator may switch accounts is your decision.',action:{label:'Open Group gift payments',section:'groups'}},
 SHOWCASE:{title:'Showcase submissions to review',who:'You approve or reject these. Nothing is public until approved.',action:{label:'Review in Growth programs',section:'programs'}}
};
function waiting(since:string|null){if(!since)return '';const days=Math.floor((Date.now()-new Date(since).getTime())/86400000);return days<1?localizeText('less than a day'):localize('admin.inbox.days',{defaultValue:'{{count}} days',count:days});}
const rupees=(paise:number)=>new Intl.NumberFormat(activeLocale('en-IN'),{style:'currency',currency:'INR'}).format(paise/100);

export default function AdminVerificationInbox(){useLocalizationRender();
 const [queues,setQueues]=useState<Queue[]|null>(null),[error,setError]=useState(''),[reload,setReload]=useState(0);
 useEffect(()=>{let active=true;setError('');commercialApi<{queues:Queue[]}>('/api/admin/console/verification-inbox',undefined,'GET').then(x=>{if(active)setQueues(x.queues);}).catch(e=>{if(active)setError(e instanceof Error?e.message:'Could not load the verification inbox');});return()=>{active=false};},[reload]);
 const total=(queues||[]).reduce((sum,q)=>sum+Number(q.count||0),0);
 return <>
  <Callout><p>{localizeText('Everything on the platform that is waiting for someone to verify it. Payment confirmations stay with the host or group creator, and accounts are verified only with provider evidence, so most queues are for follow-up rather than approval.')}</p></Callout>
  {error&&<p role="alert" className="acError">{localizeText(error)} <button type="button" className="admLinkButton" onClick={()=>setReload(v=>v+1)}>{localizeText('Retry')}</button></p>}
  {!queues&&!error&&<p role="status">{localizeText('Loading the verification inbox…')}</p>}
  {queues&&<>
   <p className="admCount" role="status">{total?localize('admin.inbox.total',{defaultValue:'{{count}} items waiting',count:total}):localizeText('Nothing is waiting for verification.')}</p>
   {queues.map(q=>{const meta=INBOX_QUEUES[q.key]||{title:q.key,who:''};const count=Number(q.count||0);return <section key={q.key} className="acPanel admQueue" aria-labelledby={`queue-${q.key}`}>
    <div className="admPanelHead"><div><h2 id={`queue-${q.key}`}>{localizeText(meta.title)}</h2>{count>0&&q.oldest&&<p className="admHint" style={{margin:'4px 0 0'}}>{localize('admin.inbox.oldest',{defaultValue:'Oldest waiting {{age}}',age:waiting(q.oldest)})}</p>}</div><span className={`admPill ${count?'is-unknown':'is-on'}`}>{count?localize('admin.inbox.count',{defaultValue:'{{count}} waiting',count}):localizeText('All clear')}</span></div>
    <p>{localizeText(meta.who)}</p>
    {meta.action&&count>0&&<p><a href={sectionHref(meta.action.section)}>{localizeText(meta.action.label)} →</a></p>}
    {count===0?<EmptyState title={localizeText('Nothing waiting here.')}/>:<details><summary>{localize('admin.inbox.show',{defaultValue:'Show the oldest {{count}}',count:q.items.length})}</summary>
     <div className="acTable"><table><thead><tr>{['Item','Owner','Type','Amount','Waiting'].map(h=><th key={h} scope="col">{localizeText(h)}</th>)}</tr></thead>
      <tbody>{q.items.map(i=><tr key={i.id}><td>{i.subject||'—'}{i.host_confirmed&&<small style={{display:'block'}}>{localizeText('Confirmed by the host')}</small>}</td><td>{i.owner||'—'}</td><td>{localizeText(i.kind)}</td><td>{i.amount_paise!=null?rupees(Number(i.amount_paise)):'—'}</td><td><time dateTime={i.created_at}>{waiting(i.created_at)}</time></td></tr>)}</tbody></table></div>
     {count>q.items.length&&<p className="admHint">{localize('admin.inbox.more',{defaultValue:'{{count}} more not shown.',count:count-q.items.length})}</p>}
    </details>}
   </section>;})}
  </>}
 </>;
}
