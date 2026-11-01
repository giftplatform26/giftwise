'use client';
import {useCallback,useEffect,useRef,useState} from 'react';
import {useParams} from 'next/navigation';
import GroupCollectionReport from '../../../../components/GroupCollectionReport';
import PublishedGuestGifts from '../../../../components/PublishedGuestGifts';
import Link from 'next/link';
import {useExperience} from '../../../../components/ExperienceProvider';
import './contributions.css';
import {ActivityRow} from '../../../../components/focused/ResponsiveTable';
type Entry={id:string;source:string;guestName:string;giftId:string|null;giftName:string|null;contributionType:string;method:string;amountPaise:number;giftPaise:number;walletPaise:number;verificationStatus:string;createdAt:string};
type Gift={id:string;name:string;targetPaise:number};
type Report={eventId:string;pendingPaise:number;verifiedPaise:number;verifiedGiftPaise:number;verifiedWalletPaise:number;entries:Entry[];gifts:Gift[];selectedGiftId:string|null;selectedGiftTargetPaise:number;matchingCount:number;reportedGiftPaise:number;reportedWalletPaise:number;selectedGiftVerifiedPaise:number;selectedGiftReportedPaise:number};
const money=(n:number)=>'₹'+(n/100).toLocaleString('en-IN',{minimumFractionDigits:2,maximumFractionDigits:2});
const labelStatus=(e:Entry)=>e.verificationStatus==='VERIFICATION_PENDING'?(e.source==='GROUP'?'Group collection transfer — receipt not independently verified':e.source==='MANUAL'?'Payment reported by guest — automatic verification pending':'Payment verification pending'):e.verificationStatus==='VERIFIED'?'Payment verified':e.verificationStatus==='FAILED'?'Verification failed':e.verificationStatus==='CANCELLED'?'Payment cancelled':e.verificationStatus;
export default function ContributionsPage(){
 const {mode}=useExperience();const focused=mode==='FOCUSED';const [filterOpen,setFilterOpen]=useState(true);
 const {id}=useParams();const eventId=String(id??'');
 const [report,setReport]=useState<Report|null>(null),[error,setError]=useState(''),[loading,setLoading]=useState(true);
 const [type,setType]=useState('ALL'),[status,setStatus]=useState('ALL'),[giftId,setGiftId]=useState(''),[offset,setOffset]=useState(0);
 const [search,setSearch]=useState(''),[query,setQuery]=useState(''),[pageSize,setPageSize]=useState(20);
 const request=useRef<AbortController|null>(null);
 useEffect(()=>{const timer=setTimeout(()=>{setQuery(search.trim());setOffset(0)},300);return()=>clearTimeout(timer)},[search]);
 const [from,setFrom]=useState(''),[to,setTo]=useState(''),[exporting,setExporting]=useState(false);
 const load=useCallback(async()=>{if(!eventId)return;request.current?.abort();const controller=new AbortController();request.current=controller;setLoading(true);setError('');try{
  const q=new URLSearchParams({type,status,limit:String(pageSize),search:query,offset:String(offset)});
  if(giftId)q.set('giftId',giftId);if(from)q.set('from',from);if(to)q.set('to',to);
  const r=await fetch(`/backend/api/v1/events/${encodeURIComponent(eventId)}/contributions?${q}`,{credentials:'include',cache:'no-store',signal:controller.signal});
  if(!r.ok)throw new Error(`Could not load report (${r.status})`);
  setReport(await r.json() as Report);
 }catch(e){if(controller.signal.aborted)return;setError(e instanceof Error?e.message:'Report unavailable');setReport(null);}finally{if(!controller.signal.aborted)setLoading(false);}},[eventId,type,status,giftId,offset,from,to,query,pageSize]);
 useEffect(()=>{void load();return()=>request.current?.abort()},[load]);
 const selectedGift=report?.gifts?.find(g=>g.id===giftId);
 const exportCsv=async()=>{setExporting(true);setError('');try{
  const q=new URLSearchParams({type,status,search:query});if(giftId)q.set('giftId',giftId);if(from)q.set('from',from);if(to)q.set('to',to);
  const r=await fetch(`/backend/api/v1/events/${encodeURIComponent(eventId)}/contributions/export.csv?${q}`,{credentials:'include',cache:'no-store'});
  if(!r.ok)throw new Error(`Could not export report (${r.status})`);
  const blob=await r.blob();const url=URL.createObjectURL(blob);const a=document.createElement('a');
  a.href=url;a.download=`contributions-${eventId}.csv`;document.body.appendChild(a);a.click();a.remove();URL.revokeObjectURL(url);
 }catch(e){setError(e instanceof Error?e.message:'Export unavailable');}finally{setExporting(false);}};
 return <main className="contributionReport" aria-busy={loading}><GroupCollectionReport eventId={eventId}/><PublishedGuestGifts eventId={eventId}/>{!focused&&<p><Link href="/events">← My events</Link> · <Link href={`/events/${encodeURIComponent(eventId)}/notifications`}>Notifications</Link></p>}
 <h1>Contribution report</h1><p>Gift and celebration-wallet activity. Guest-reported transfers are not confirmed money. Group transfers appear after the creator publishes the collection; they do not confirm money received by the host.</p>
 <label className="reportSearch">Search contributions<input type="search" placeholder="Search guest, gift, or payment method" value={search} maxLength={120} onChange={e=>setSearch(e.target.value)}/></label>
 {focused&&<button type="button" aria-expanded={filterOpen} onClick={()=>setFilterOpen(v=>!v)}>Filter contributions</button>}
 <div className="reportFilters" hidden={focused&&!filterOpen} style={{display:focused&&!filterOpen?'none':'flex',gap:12,marginBottom:16,flexWrap:'wrap'}}>
 <label>Select gift <select aria-label="Select gift" value={giftId} onChange={e=>{setGiftId(e.target.value);setOffset(0);}}><option value="">All contributions</option>{(report?.gifts??[]).map(g=><option key={g.id} value={g.id}>{g.name}</option>)}</select></label>
 <label>Contribution <select aria-label="Contribution" value={type} onChange={e=>{setType(e.target.value);setOffset(0);}}><option value="ALL">All</option><option value="GIFT">Gifts</option><option value="WALLET">Celebration wallet (including excess)</option></select></label>
 <label>Status <select aria-label="Status" value={status} onChange={e=>{setStatus(e.target.value);setOffset(0);}}><option value="ALL">All</option><option value="VERIFICATION_PENDING">Guest reported / verification pending</option><option value="VERIFIED">Verified</option><option value="FAILED">Failed</option><option value="CANCELLED">Cancelled</option></select></label>
 <label>From (UTC) <input aria-label="From date UTC" type="date" value={from} max={to||undefined} onChange={e=>{setFrom(e.target.value);setOffset(0);}}/></label>
 <label>To (UTC) <input aria-label="To date UTC" type="date" value={to} min={from||undefined} onChange={e=>{setTo(e.target.value);setOffset(0);}}/></label>
 <button onClick={()=>void load()} disabled={loading}>Refresh</button>
 <button onClick={()=>void exportCsv()} disabled={loading||exporting||!report||Boolean(from&&to&&from>to)}>{exporting?'Exporting…':'Export CSV'}</button></div>
 {error&&<p role="alert">{error} {error.includes('(401)')&&<Link href="/login">Sign in again</Link>}</p>}{loading&&<p role="status">Loading…</p>}
 {report&&<>{focused&&<div className="focusSummary"><div><span>Verified payments</span><strong>{money(report.verifiedPaise)}</strong></div><div><span>Reported / verification pending</span><strong>{money(report.pendingPaise)}</strong></div></div>}<details><summary>{focused?'Allocation details':'Contribution totals'}</summary><div style={{display:'flex',gap:24,flexWrap:'wrap',margin:'20px 0'}}><div><b>Pending / guest-reported (selected destination)</b><p>{money(report.pendingPaise)}</p></div><div><b>Verified in selection (selected destination)</b><p>{money(report.verifiedPaise)}</p></div><div><b>Verified gift portion</b><p>{money(report.verifiedGiftPaise)}</p></div><div><b>Verified wallet portion</b><p>{money(report.verifiedWalletPaise)}</p></div><div><b>Guest-reported gift portion</b><p>{money(report.reportedGiftPaise)}</p></div><div><b>Guest-reported wallet portion</b><p>{money(report.reportedWalletPaise)}</p></div><div><b>Pending Razorpay checkout (not paid)</b><p>{money(Math.max(0,report.pendingPaise-report.reportedGiftPaise-report.reportedWalletPaise))}</p></div><div><b>Matching records</b><p>{report.matchingCount}</p></div></div></details>
 {selectedGift&&<section style={{padding:14,border:'1px solid #aaa',borderRadius:8,marginBottom:18}}><b>Selected gift: {selectedGift.name}</b><p><b>Target:</b> {money(report.selectedGiftTargetPaise)} · <b>Verified toward gift:</b> {money(report.selectedGiftVerifiedPaise)} · <b>Remaining verified amount:</b> {money(Math.max(0,report.selectedGiftTargetPaise-report.selectedGiftVerifiedPaise))} · <b>Guest-reported toward gift (unverified):</b> {money(report.selectedGiftReportedPaise)}</p><p>Gift progress includes all dates and statuses; matching records ({report.matchingCount}) follow current filters.</p></section>}
 {!loading&&<>{focused&&<div className="focusMobileRows">{report.entries.map(e=><ActivityRow key={e.source+e.id} title={e.guestName} amount={money(e.amountPaise)} status={labelStatus(e)}><p>{e.giftName||'Celebration wallet'} · {e.method}</p><p>{new Date(e.createdAt).toLocaleString()}</p><p>Gift portion: {money(e.giftPaise)} · Celebration: {money(e.walletPaise)}</p></ActivityRow>)}</div>}<div className={focused?'focusDesktopTable':undefined} style={{overflowX:'auto'}}><table style={{width:'100%',borderCollapse:'collapse'}}><thead><tr>{['Date','Guest','Contribution','Method','Payment amount','Gift portion','Wallet portion','Verification'].map(s=><th key={s} style={{textAlign:'left',padding:8,borderBottom:'1px solid #aaa'}}>{s}</th>)}</tr></thead><tbody>{report.entries.map(e=><tr key={e.source+e.id}><td>{new Date(e.createdAt).toLocaleString()}</td><td>{e.guestName}</td><td>{e.giftName||'Celebration wallet'}</td><td>{e.method}</td><td>{money(e.amountPaise)}</td><td>{money(e.giftPaise)}</td><td>{money(e.walletPaise)}</td><td>{labelStatus(e)}</td></tr>)}</tbody></table></div>{report.entries.length===0&&<p>No matching contributions for these filters.</p>}
 <nav className="reportPagination" aria-label="Contribution pages"><label>Rows per page <select value={pageSize} onChange={e=>{setPageSize(Number(e.target.value));setOffset(0)}}>{[10,20,50].map(n=><option key={n} value={n}>{n}</option>)}</select></label><span role="status">{report.matchingCount===0?'0 results':`${offset+1}–${Math.min(offset+report.entries.length,report.matchingCount)} of ${report.matchingCount}`} · Page {Math.floor(offset/pageSize)+1} of {Math.max(1,Math.ceil(report.matchingCount/pageSize))}</span><button disabled={loading||offset===0} onClick={()=>setOffset(Math.max(0,offset-pageSize))}>Previous</button><button disabled={loading||offset+pageSize>=report.matchingCount} onClick={()=>setOffset(offset+pageSize)}>Next</button></nav></>}
 </>}
 </main>;
}
