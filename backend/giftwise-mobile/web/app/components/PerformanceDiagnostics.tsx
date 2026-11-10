 'use client';
import {useEffect,useRef} from 'react';
import {usePathname} from 'next/navigation';
import {diagnosticsSnapshot,recordMetric} from '../../lib/diagnostics.mjs';
declare global {interface Window {giftDiagnostics?:typeof diagnosticsSnapshot}}
export default function PerformanceDiagnostics(){
 const pathname=usePathname();const currentPath=useRef(pathname);currentPath.current=pathname;
 useEffect(()=>{if(process.env.NEXT_PUBLIC_DIAGNOSTICS_ENABLED!=='true')return;const started=performance.now();const frame=requestAnimationFrame(()=>recordMetric('screen_shell_ms',performance.now()-started,pathname));return()=>cancelAnimationFrame(frame)},[pathname]);
 useEffect(()=>{
  if(process.env.NEXT_PUBLIC_DIAGNOSTICS_ENABLED!=='true')return;
  window.giftDiagnostics=diagnosticsSnapshot;
  if(typeof PerformanceObserver==='undefined')return()=>{delete window.giftDiagnostics};
  const initialPath=currentPath.current;
  const observer=new PerformanceObserver(list=>{
   for(const entry of list.getEntries()){
    if(entry.entryType==='resource'){
     const resource=entry as PerformanceResourceTiming;const url=new URL(resource.name,location.origin);
     if(url.origin===location.origin&&url.pathname.startsWith('/backend/api/'))recordMetric('api_ms',entry.duration,url.pathname);
    }else if(entry.entryType==='largest-contentful-paint')recordMetric('lcp_ms',entry.startTime,initialPath);
    else if(entry.entryType==='event')recordMetric('interaction_ms',entry.duration,currentPath.current);
    else if(entry.entryType==='measure'&&entry.name.startsWith('gift:'))recordMetric(entry.name,entry.duration,currentPath.current);
   }
  });
  for(const type of ['resource','largest-contentful-paint','event','measure']){
   if(PerformanceObserver.supportedEntryTypes.includes(type))try{observer.observe({type,buffered:type==='largest-contentful-paint',...(type==='event'?{durationThreshold:40}:{})});}catch{}
  }
  return()=>{observer.disconnect();delete window.giftDiagnostics};
 },[]);
 return null;
}
