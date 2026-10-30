'use client';
import type {ReactNode} from 'react';
// Accessible native table layout is kept on desktop; labelled details are rendered by each feature on mobile.
export function ActivityRow({title,amount,status,children}:{title:string;amount:string;status:string;children:ReactNode}){return <details className="focusActivityRow"><summary><span><strong>{title}</strong><small>{status}</small></span><b>{amount}</b></summary><div>{children}</div></details>}
