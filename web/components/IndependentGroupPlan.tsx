'use client';
import {useRef,useState} from 'react';
type Fields={giftName:string;giftDescription:string;giftLink:string;targetRupees:string};
type Call=(path:string,body:unknown)=>Promise<any>;
export function IndependentGroupPlanFields({value,onChange,call,busy,setBusy}:{value:Fields;onChange:(value:Fields)=>void;call:Call;busy:boolean;setBusy:(value:boolean)=>void}){
 const [plan,setPlan]=useState(value.giftName||value.giftLink?'LINK':'GENERAL'),[notice,setNotice]=useState('');
 const fetching=useRef(false),lastLink=useRef('');
 async function lookup(){
  if(fetching.current||!value.giftLink.trim()||lastLink.current===value.giftLink.trim())return;
  fetching.current=true;setBusy(true);setNotice('Reading the product details…');
  try{const result=await call('/gift-metadata',{productUrl:value.giftLink.trim()});lastLink.current=value.giftLink.trim();const price=Number(result.targetAmount);const inr=result.currency==='INR'&&Number.isFinite(price)&&price>0&&price<=1000000000;
   onChange({...value,giftName:String(result.name||value.giftName).slice(0,160),giftDescription:String(result.description||value.giftDescription).slice(0,500),targetRupees:inr?String(price):value.targetRupees});setNotice(inr?'Gift details filled in. Review the details and collection goal.':'Gift details filled in. Review them and enter your collection goal in rupees.');
  }catch(e){setNotice(e instanceof Error?e.message:'Could not read the product page. Enter the details manually.')}finally{fetching.current=false;setBusy(false)}
 }
 return <><label>Collection plan<select disabled={busy} value={plan} onChange={e=>{setPlan(e.target.value);setNotice('');lastLink.current='';if(e.target.value==='GENERAL')onChange({...value,giftName:'',giftDescription:'',giftLink:''})}}><option value="GENERAL">General collection · choose our gift later</option><option value="LINK">Our own gift · add a product link</option></select></label>
 {plan==='GENERAL'?<p>Collect together first. Your group can add its own gift link whenever you decide.</p>:<><label>Our gift’s product link<input required type="url" pattern="https://[^\s]+" maxLength={1000} disabled={busy} placeholder="https://…" value={value.giftLink} onChange={e=>onChange({...value,giftLink:e.target.value})} onBlur={()=>{if(!busy)void lookup()}}/></label><button type="button" className="gc-secondary" disabled={busy||!value.giftLink.trim()} onClick={()=>void lookup()}>Fill gift details from link</button><label>Gift name<input required maxLength={160} disabled={busy} value={value.giftName} onChange={e=>onChange({...value,giftName:e.target.value})}/></label><label>Gift description<textarea maxLength={500} disabled={busy} value={value.giftDescription} onChange={e=>onChange({...value,giftDescription:e.target.value})}/></label></>}
 <label>Collection goal (₹) · optional<input type="number" inputMode="decimal" min="0.01" max="1000000000" step="0.01" disabled={busy} value={value.targetRupees} onChange={e=>onChange({...value,targetRupees:e.target.value})}/></label>{notice&&<p role="status">{notice}</p>}</>;
}
export default function IndependentGroupPlan({group,call,onChange,busy,setBusy}:{group:{id:string;giftName?:string;giftDescription?:string;giftLink?:string;targetPaise?:number};call:Call;onChange:()=>Promise<void>;busy:boolean;setBusy:(value:boolean)=>void}){
 const [value,setValue]=useState<Fields>({giftName:group.giftName||'',giftDescription:group.giftDescription||'',giftLink:group.giftLink||'',targetRupees:group.targetPaise!=null?String(group.targetPaise/100):''}),[notice,setNotice]=useState('');
 async function save(e:React.FormEvent){e.preventDefault();setBusy(true);setNotice('');try{const result=await call(`/gift-plan/${group.id}`,{giftName:value.giftName||null,giftDescription:value.giftDescription||null,giftLink:value.giftLink||null,targetPaise:value.targetRupees?Math.round(Number(value.targetRupees)*100):null});setNotice(result.message);await onChange()}catch(e){setNotice(e instanceof Error?e.message:'Could not save the gift plan.')}finally{setBusy(false)}}
 return <form className="gc-form gc-panel" onSubmit={save}><h3>Your group’s own gift</h3><IndependentGroupPlanFields value={value} onChange={setValue} call={call} busy={busy} setBusy={setBusy}/><button className="gc-secondary" disabled={busy}>Save our gift plan</button>{notice&&<p role="status">{notice}</p>}</form>;
}
