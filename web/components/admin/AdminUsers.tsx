'use client';
import {useEffect,useState} from 'react';
import {localize,localizeText} from '../../lib/localization.mjs';
import {useLocalizationRender} from '../LocalizationProvider';
import {commercialApi} from '../CommercePurchase';
import {sectionHref} from '../../lib/admin-sections.mjs';
import {EmptyState} from './AdminUi';

type User={id:string;username:string|null;full_name:string|null;email:string;role:string;enabled:boolean;deleted:boolean;created_at:string;email_verified:boolean;phone_verified:boolean;events:number;published_events:number};
type UserPage={entries:User[];total:number;page:number;pageSize:number;totalPages:number};
const initial={search:'',role:'ALL',status:'ALL'};

/** Read-only directory of every account. Feature access for hosts is changed under Hosts & access. */
export default function AdminUsers(){useLocalizationRender();
 const [draft,setDraft]=useState(initial),[filters,setFilters]=useState(initial),[page,setPage]=useState(0),[data,setData]=useState<UserPage|null>(null),[loading,setLoading]=useState(true),[error,setError]=useState(''),[reload,setReload]=useState(0);
 useEffect(()=>{let active=true;setLoading(true);setError('');
  const query=new URLSearchParams({page:String(page),search:filters.search,role:filters.role,status:filters.status});
  commercialApi<UserPage>(`/api/admin/console/users?${query}`,undefined,'GET').then(x=>{if(active){setData(x);if(x.page!==page)setPage(x.page);}}).catch(e=>{if(active){setError(e instanceof Error?e.message:'Could not load users');setData(null);}}).finally(()=>{if(active)setLoading(false);});
  return()=>{active=false};},[filters,page,reload]);
 const status=(u:User)=>u.deleted?'Deleted':u.enabled?'Active':'Disabled';
 return <section className="acPanel" aria-labelledby="users-title" aria-busy={loading}>
  <div className="admPanelHead"><h2 id="users-title">{localizeText('All accounts')}</h2>{data&&<span className="admCount">{localize('admin.users.total',{defaultValue:'{{count}} accounts',count:data.total})}</span>}</div>
  <p>{localizeText('Hosts and administrators, newest first. Phone numbers and passwords are never shown. To change what a host can use, open their feature access.')}</p>
  <form className="admToolbar" onSubmit={e=>{e.preventDefault();setPage(0);setFilters({...draft,search:draft.search.trim()});}}>
   <label className="admField">{localizeText('Search')}<input type="search" maxLength={100} value={draft.search} placeholder={localizeText('Name, username or email')} onChange={e=>setDraft({...draft,search:e.target.value})}/></label>
   <label className="admField">{localizeText('Role')}<select value={draft.role} onChange={e=>setDraft({...draft,role:e.target.value})}><option value="ALL">{localizeText('All roles')}</option><option value="CUSTOMER">{localizeText('Host')}</option><option value="PLATFORM_ADMIN">{localizeText('Administrator')}</option></select></label>
   <label className="admField">{localizeText('Account status')}<select value={draft.status} onChange={e=>setDraft({...draft,status:e.target.value})}><option value="ALL">{localizeText('All statuses')}</option><option value="ACTIVE">{localizeText('Active')}</option><option value="DISABLED">{localizeText('Disabled')}</option><option value="DELETED">{localizeText('Deleted')}</option></select></label>
   <button type="submit" className="admButton" disabled={loading}>{localizeText('Apply filters')}</button>
   <button type="button" className="admButton" disabled={loading} onClick={()=>{setDraft(initial);setFilters(initial);setPage(0);}}>{localizeText('Clear filters')}</button>
  </form>
  {error&&<p role="alert" className="acError">{localizeText(error)} <button type="button" className="admLinkButton" onClick={()=>setReload(v=>v+1)}>{localizeText('Retry')}</button></p>}
  {loading&&!data&&<p role="status">{localizeText('Loading users…')}</p>}
  {data&&!error&&(data.entries.length===0?<EmptyState title={localizeText('No accounts match these filters.')}/>:<>
   <div className="acTable"><table><thead><tr>{['Account','Role','Status','Verified contacts','Events','Joined',''].map(h=><th key={h} scope="col">{h?localizeText(h):<span className="admVisuallyHidden">{localizeText('Actions')}</span>}</th>)}</tr></thead>
    <tbody>{data.entries.map(u=><tr key={u.id}>
     <td><strong>{u.full_name||u.username||'—'}</strong><small style={{display:'block'}}>{u.username?`@${u.username} · `:''}<span style={{overflowWrap:'anywhere'}}>{u.email}</span></small></td>
     <td><span className="admPill is-neutral">{localizeText(u.role==='PLATFORM_ADMIN'?'Administrator':'Host')}</span></td>
     <td><span className={`admPill ${u.deleted||!u.enabled?'is-off':'is-on'}`}>{localizeText(status(u))}</span></td>
     <td>{localizeText(u.email_verified?'Email ✓':'Email —')} · {localizeText(u.phone_verified?'Mobile ✓':'Mobile —')}</td>
     <td>{u.events}<small style={{display:'block'}}>{localize('admin.users.published',{defaultValue:'{{count}} published',count:u.published_events})}</small></td>
     <td><time dateTime={u.created_at}>{new Date(u.created_at).toLocaleDateString()}</time></td>
     <td>{u.role==='CUSTOMER'&&!u.deleted&&<a href={sectionHref('hosts',u.id)}>{localizeText('Feature access →')}</a>}</td>
    </tr>)}</tbody></table></div>
   <nav aria-label={localizeText('Users pagination')} className="admPager"><button type="button" className="admButton" disabled={loading||data.page===0} onClick={()=>setPage(data.page-1)}>{localizeText('Previous')}</button><span>{localize('admin.pager',{defaultValue:'Page {{page}} of {{pages}}',page:data.page+1,pages:data.totalPages})}</span><button type="button" className="admButton" disabled={loading||data.page+1>=data.totalPages} onClick={()=>setPage(data.page+1)}>{localizeText('Next')}</button></nav>
  </>)}
 </section>;
}
