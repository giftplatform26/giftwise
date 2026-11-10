'use client';
import {activeLocale} from '../lib/localization.mjs';

/* localization:instrumented */
import {localize,localizeText} from '../lib/localization.mjs';
import {useLocalizationRender} from './LocalizationProvider';

import {useEffect,useState} from 'react';
type Activity={id:string;action:string;details:string;created_at:string};
export default function GroupActivity({url,pageId,revision=0}:{url:string;pageId?:string;revision?:number}){useLocalizationRender();
 const [rows,setRows]=useState<Activity[]>([]),[error,setError]=useState(''),[offset,setOffset]=useState(0),[more,setMore]=useState(false);
 useEffect(()=>{let active=true;setError('');fetch(url+`?offset=${offset}`,{credentials:'include',cache:'no-store',headers:pageId?{'X-Group-Page-Id':pageId}:{}}).then(async r=>{if(!r.ok)throw Error('Could not load group activity');return r.json()}).then((data:Activity[])=>{if(active){setRows(data);setMore(data.length===100)}}).catch(e=>{if(active)setError(e.message)});return()=>{active=false}},[url,pageId,revision,offset]);
 return <details className="gc-panel"><summary>{localize("ui.85810b7d69681918",{defaultValue:"Group activity & contribution history"})}</summary>{error&&<p role="alert">{localizeText(error)}</p>}{!rows.length&&!error&&<p>{localize("ui.09f1c438851431f3",{defaultValue:"No activity recorded yet."})}</p>}<ol>{rows.map(row=>{let detail:{amountPaise?:number;reference?:string;state?:string}={};try{detail=JSON.parse(row.details)}catch{}return <li key={row.id}><strong>{row.action.toLowerCase().replaceAll('_',' ')}</strong> · {new Date(row.created_at).toLocaleString()}{detail.amountPaise!=null&&<span> · ₹{(detail.amountPaise/100).toLocaleString(activeLocale("en-IN"))}</span>}{detail.reference&&<span> {localize("ui.b6bcc6470d216bc3",{defaultValue:" · Reference: "})}{detail.reference}</span>}{detail.state&&<span> · {detail.state}</span>}</li>})}</ol>{offset>0&&<button type="button" onClick={()=>setOffset(value=>Math.max(0,value-100))}>{localize("ui.c9bca82b557e962e",{defaultValue:"Newer activity"})}</button>}{more&&<button type="button" onClick={()=>setOffset(value=>value+100)}>{localize("ui.0eed19abab86afcc",{defaultValue:"Older activity"})}</button>}</details>;
}
