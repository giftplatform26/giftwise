'use client';
import {useEffect,useState} from 'react';
import {localize,localizeText} from '../../lib/localization.mjs';
import {useLocalizationRender} from '../LocalizationProvider';
import {commercialApi} from '../CommercePurchase';
import {ConfirmDialog,EmptyState,Callout} from './AdminUi';

type Group={id:string;name:string;creator_name:string;status:string;created_at:string;event_title:string;switching_enabled:boolean;changed_at:string|null;verified_accounts:number;unverified_accounts:number};
type GroupPage={entries:Group[];total:number;page:number;pageSize:number;totalPages:number};

/** Lists group gifts and controls whether each group creator may switch the account that receives contributions. */
export default function AdminGroupPayments(){useLocalizationRender();
 const [draft,setDraft]=useState(''),[search,setSearch]=useState(''),[page,setPage]=useState(0),[data,setData]=useState<GroupPage|null>(null),[loading,setLoading]=useState(true),[error,setError]=useState(''),[notice,setNotice]=useState(''),[reload,setReload]=useState(0);
 const [pending,setPending]=useState<{group:Group;enabled:boolean}|null>(null),[busy,setBusy]=useState(false);
 useEffect(()=>{let active=true;setLoading(true);setError('');
  commercialApi<GroupPage>(`/api/admin/group-payments?${new URLSearchParams({search,page:String(page)})}`,undefined,'GET').then(x=>{if(active){setData(x);if(x.page!==page)setPage(x.page);}}).catch(e=>{if(active){setError(e instanceof Error?e.message:'Could not load group gifts');setData(null);}}).finally(()=>{if(active)setLoading(false);});
  return()=>{active=false};},[search,page,reload]);
 async function apply(){if(!pending||busy)return;setBusy(true);setError('');setNotice('');
  try{await commercialApi(`/api/admin/group-payments/${pending.group.id}/switching`,{enabled:pending.enabled},'POST');
   setNotice(localize(pending.enabled?'admin.groups.allowed':'admin.groups.blocked',{defaultValue:pending.enabled?'Account switching is now allowed for {{name}}. The change is recorded in the group payment audit.':'Account switching is now blocked for {{name}}. The change is recorded in the group payment audit.',name:pending.group.name}));
   setPending(null);setReload(v=>v+1);}
  catch(e){setError(e instanceof Error?e.message:'Could not change account switching');setPending(null);}
  finally{setBusy(false);}}
 return <section className="acPanel" aria-labelledby="groups-title" aria-busy={loading}>
  <div className="admPanelHead"><h2 id="groups-title">{localizeText('Group gift account switching')}</h2>{data&&<span className="admCount">{localize('admin.groups.total',{defaultValue:'{{count}} group gifts',count:data.total})}</span>}</div>
  <Callout><p>{localizeText('Group creators register UPI or bank accounts to receive contributions. By default they cannot change the selected account. Allow switching only when you trust the request: the creator can then pick another verified account. Unverified accounts can never be selected.')}</p></Callout>
  <form className="admToolbar" onSubmit={e=>{e.preventDefault();setPage(0);setSearch(draft.trim());}}>
   <label className="admField">{localizeText('Find a group gift')}<input type="search" maxLength={100} value={draft} placeholder={localizeText('Group, creator or event')} onChange={e=>setDraft(e.target.value)}/></label>
   <button type="submit" className="admButton" disabled={loading}>{localizeText('Search')}</button>
  </form>
  {notice&&<div role="status" className="admNotice">{notice}</div>}
  {error&&<p role="alert" className="acError">{localizeText(error)} <button type="button" className="admLinkButton" onClick={()=>setReload(v=>v+1)}>{localizeText('Retry')}</button></p>}
  {loading&&!data&&<p role="status">{localizeText('Loading group gifts…')}</p>}
  {data&&(data.entries.length===0?<EmptyState title={localizeText(search?'No group gifts match.':'No group gifts yet.')}/>:<>
   <ul className="admEventList">{data.entries.map(g=><li key={g.id} className="admEventCard">
    <div className="admEventHead"><div><h3>{g.name}</h3><p>{g.event_title} · {localize('admin.groups.creator',{defaultValue:'Created by {{name}}',name:g.creator_name})}</p></div><span className="admPill is-neutral">{localizeText(g.status)}</span></div>
    <label className="admSwitchRow" style={{marginTop:12}}><span><strong>{localizeText('Allow the creator to switch accounts')}</strong><small>{localize('admin.groups.accounts',{defaultValue:'{{verified}} verified · {{unverified}} not verified',verified:g.verified_accounts,unverified:g.unverified_accounts})}{g.changed_at?` · ${localizeText('changed')} ${new Date(g.changed_at).toLocaleDateString()}`:''}</small></span>
     <input type="checkbox" role="switch" checked={g.switching_enabled} disabled={busy} aria-label={localize('admin.groups.switchLabel',{defaultValue:'Allow account switching for {{name}}',name:g.name})} onChange={e=>setPending({group:g,enabled:e.target.checked})}/></label>
   </li>)}</ul>
   <nav aria-label={localizeText('Group gifts pagination')} className="admPager"><button type="button" className="admButton" disabled={loading||data.page===0} onClick={()=>setPage(data.page-1)}>{localizeText('Previous')}</button><span>{localize('admin.pager',{defaultValue:'Page {{page}} of {{pages}}',page:data.page+1,pages:data.totalPages})}</span><button type="button" className="admButton" disabled={loading||data.page+1>=data.totalPages} onClick={()=>setPage(data.page+1)}>{localizeText('Next')}</button></nav>
  </>)}
  <ConfirmDialog open={!!pending} busy={busy} tone={pending?.enabled?'danger':'primary'}
   title={pending?localize(pending.enabled?'admin.groups.confirmOn':'admin.groups.confirmOff',{defaultValue:pending.enabled?'Allow account switching for {{name}}?':'Block account switching for {{name}}?',name:pending.group.name}):''}
   confirmLabel={localizeText(pending?.enabled?'Allow switching':'Block switching')} onCancel={()=>setPending(null)} onConfirm={()=>void apply()}>
   <p>{localizeText(pending?.enabled?'The group creator will be able to choose a different verified account to receive contributions. Guests then pay into the newly selected account.':'The group creator will no longer be able to change the receiving account. The currently selected account stays as it is.')}</p>
   <p>{localizeText('The change is recorded in the group payment audit.')}</p>
  </ConfirmDialog>
 </section>;
}
