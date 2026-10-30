'use client';
import {useEffect,useState} from 'react';
import {consumeFlash} from '../../lib/flash.mjs';
export type ToastData={type:'success'|'error'|'warning'|'info';message:string};
export default function HostToast({message:external, onDismiss}:{message?:ToastData|null;onDismiss?:()=>void}) {
 const [flash,setFlash]=useState<ToastData|null>(null);
 useEffect(()=>{const pending=consumeFlash(window.sessionStorage);if(pending)setFlash(pending as ToastData);},[]);
 const active=external||flash;
 useEffect(()=>{if(!active)return;const timer=window.setTimeout(()=>{setFlash(null);onDismiss?.();},6000);return()=>window.clearTimeout(timer);},[active?.message,active?.type]);
 if(!active)return null;
 const role=active.type==='error'?'alert':'status';
 return <div className={`hostToast hostToast--${active.type}`} role={role} aria-live={active.type==='error'?'assertive':'polite'}><span aria-hidden="true" className="hostToastIcon">{active.type==='success'?'✓':active.type==='error'?'!':active.type==='warning'?'⚠':'i'}</span><div className="hostToastBody"><strong>{active.type==='success'?'Success':active.type==='error'?'Action failed':active.type==='warning'?'Please note':'Information'}</strong><span>{active.message}</span></div><button type="button" className="hostToastClose" aria-label="Dismiss notification" onClick={()=>{setFlash(null);onDismiss?.();}}>×</button></div>;
}
