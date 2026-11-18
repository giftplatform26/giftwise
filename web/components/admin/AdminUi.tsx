'use client';
import {useEffect,useRef,type ReactNode} from 'react';
import {localizeText} from '../../lib/localization.mjs';

/** Accessible confirmation built on the native modal <dialog>: focus is trapped, Escape cancels, Cancel gets initial focus. */
export function ConfirmDialog({open,title,children,confirmLabel,tone='primary',busy=false,confirmDisabled=false,onConfirm,onCancel}:{open:boolean;title:string;children?:ReactNode;confirmLabel:string;tone?:'primary'|'danger';busy?:boolean;confirmDisabled?:boolean;onConfirm:()=>void;onCancel:()=>void}){
 const ref=useRef<HTMLDialogElement>(null);
 useEffect(()=>{const dialog=ref.current;if(!dialog)return;
  if(open&&!dialog.open){if(typeof dialog.showModal==='function')dialog.showModal();else dialog.setAttribute('open','');}
  if(!open&&dialog.open)dialog.close();
 },[open]);
 return <dialog ref={ref} className="admDialog" aria-labelledby="adm-dialog-title" onCancel={e=>{e.preventDefault();if(!busy)onCancel();}}>
  {open&&<form method="dialog" onSubmit={e=>{e.preventDefault();if(!confirmDisabled)onConfirm();}}>
   <h2 id="adm-dialog-title">{title}</h2>
   <div className="admDialogBody">{children}</div>
   <div className="admDialogActions">
    <button type="button" className="admButton" autoFocus disabled={busy} onClick={onCancel}>{localizeText('Cancel')}</button>
    <button type="submit" className={tone==='danger'?'acDanger':'acPrimary'} disabled={busy||confirmDisabled}>{busy?localizeText('Saving…'):confirmLabel}</button>
   </div>
  </form>}
 </dialog>;
}

export function StatusPill({on,onLabel='On',offLabel='Off',unknown=false}:{on:boolean;onLabel?:string;offLabel?:string;unknown?:boolean}){
 if(unknown)return <span className="admPill is-unknown">{localizeText('Unavailable')}</span>;
 return <span className={`admPill ${on?'is-on':'is-off'}`}><span aria-hidden="true">{on?'●':'○'}</span> {localizeText(on?onLabel:offLabel)}</span>;
}

export function EmptyState({title,children}:{title:string;children?:ReactNode}){
 return <div className="admEmpty" role="status"><strong>{title}</strong>{children&&<p>{children}</p>}</div>;
}

export function Callout({children}:{children:ReactNode}){
 return <div className="admCallout"><span aria-hidden="true">ⓘ</span><div>{children}</div></div>;
}
