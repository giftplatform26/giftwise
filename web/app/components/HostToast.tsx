'use client';
/* localization:instrumented */
import {localize} from '../../lib/localization.mjs';
import {useLocalizationRender} from '../../components/LocalizationProvider';

import {useEffect,useState} from 'react';
import {consumeFlash} from '../../lib/flash.mjs';
export type ToastData={type:'success'|'error'|'warning'|'info';message:string};
export default function HostToast({message:external, onDismiss}:{message?:ToastData|null;onDismiss?:()=>void}) {useLocalizationRender();
 const [flash,setFlash]=useState<ToastData|null>(null);
 useEffect(()=>{const pending=consumeFlash(window.sessionStorage);if(pending)setFlash(pending as ToastData);},[]);
 const active=external||flash;
 useEffect(()=>{if(!active)return;const timer=window.setTimeout(()=>{setFlash(null);onDismiss?.();},6000);return()=>window.clearTimeout(timer);},[active?.message,active?.type]);
 if(!active)return null;
 const role=active.type==='error'?'alert':'status';
 return <div className={`hostToast hostToast--${active.type}`} role={role} aria-live={active.type==='error'?'assertive':'polite'}><span aria-hidden="true" className="hostToastIcon">{active.type==='success'?'✓':active.type==='error'?'!':active.type==='warning'?'⚠':localize("ui.de7d1b721a1e0632",{defaultValue:"i"})}</span><div className="hostToastBody"><strong>{active.type==='success'?localize("ui.c88a0b907419a70c",{defaultValue:"Success"}):active.type==='error'?localize("ui.6e1704d8eca96bd7",{defaultValue:"Action failed"}):active.type==='warning'?localize("ui.31c7ca8042c29bdb",{defaultValue:"Please note"}):localize("ui.1cb0ba125f84c982",{defaultValue:"Information"})}</strong><span>{active.message}</span></div><button type="button" className="hostToastClose" aria-label={localize("ui.b7bb3f342402da33",{defaultValue:"Dismiss notification"})} onClick={()=>{setFlash(null);onDismiss?.();}}>×</button></div>;
}
